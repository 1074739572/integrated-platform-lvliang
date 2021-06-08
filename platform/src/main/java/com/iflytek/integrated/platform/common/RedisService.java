package com.iflytek.integrated.platform.common;

import static com.iflytek.integrated.platform.entity.QTBusinessInterface.qTBusinessInterface;
import static com.iflytek.integrated.platform.entity.QTDrive.qTDrive;
import static com.iflytek.integrated.platform.entity.QTHospital.qTHospital;
import static com.iflytek.integrated.platform.entity.QTInterface.qTInterface;
import static com.iflytek.integrated.platform.entity.QTPlatform.qTPlatform;
import static com.iflytek.integrated.platform.entity.QTProject.qTProject;
import static com.iflytek.integrated.platform.entity.QTSys.qTSys;
import static com.iflytek.integrated.platform.entity.QTSysConfig.qTSysConfig;
import static com.iflytek.integrated.platform.entity.QTSysDriveLink.qTSysDriveLink;
import static com.iflytek.integrated.platform.entity.QTSysHospitalConfig.qTSysHospitalConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.common.utils.JackSonUtils;
import com.iflytek.integrated.common.utils.RedisUtil;
import com.iflytek.integrated.platform.dto.RedisDto;
import com.iflytek.integrated.platform.dto.RedisKeyDto;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.SQLQueryFactory;

import springfox.documentation.annotations.ApiIgnore;

/**
 * redis缓存操作
 * 
 * @author weihe9
 * @date 2021/1/22 10:01
 */
@Service
@ApiIgnore
public class RedisService {
	private static final Logger logger = LoggerFactory.getLogger(RedisService.class);

	@Autowired
	private RedisUtil redisUtil;
	@Autowired
	protected SQLQueryFactory sqlQueryFactory;

	/**
	 * 开启新线程执行清除缓存
	 */
	private static final ThreadPoolExecutor THREAD_POOL_CLEAR_REDIS = new ThreadPoolExecutor(4, 8, 10, TimeUnit.MINUTES,
			new ArrayBlockingQueue<>(8), new ThreadPoolExecutor.AbortPolicy());

	/**
	 * 删除key
	 * 
	 * @param resultDto
	 * @param keyName
	 */
	public void delRedisKey(ResultDto resultDto, String keyName) {
		// 调用新线程处理redis缓存
		if (Constant.ResultCode.SUCCESS_CODE == resultDto.getCode()) {
			THREAD_POOL_CLEAR_REDIS.execute(() -> {
				try {
					threadDelRedisKey(resultDto.getData().toString(), keyName);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}
	}

	/**
	 * 删除key操作
	 * 
	 * @param redisDtoStr
	 * @param keyName
	 */
	private void threadDelRedisKey(String redisDtoStr, String keyName) throws IOException {
		RedisDto redisDto = JackSonUtils.jsonToTransfer(redisDtoStr, RedisDto.class);
		List<RedisKeyDto> list = redisDto.getRedisKeyDtoList();

		if (CollectionUtils.isEmpty(list)) {
			String ids = redisDto.getIds();
			if (StringUtils.isBlank(ids) || StringUtils.isBlank(keyName)) {
				logger.error("删除redis-key操作,没有取到ids或keyName");
				return;
			}
			List<String> conditionList = Arrays.asList(ids.split(","));
			ArrayList<Predicate> arr = new ArrayList<>();

			// 调取枚举，处理返回结果
			StringPath sqlId = Constant.RedisKeyEnum.idByKey(keyName);
			if (sqlId == null) {
				logger.error("删除redis-key操作,没有取到有效的key");
				return;
			}
			arr.add(sqlId.in(conditionList));

			list = this.getRedisKeyDtoList(arr);
		}
		if (CollectionUtils.isNotEmpty(list)) {
			this.delRedisKey(list);
		}

	}

	/**
	 * 删除key操作
	 * 
	 * @param arr
	 */
	public List<RedisKeyDto> getRedisKeyDtoList(ArrayList<Predicate> arr) {
		arr.add(qTSysHospitalConfig.hospitalCode.isNotNull()
				.and(qTSys.sysCode.isNotNull().and(qTInterface.interfaceUrl.isNotNull())));

		List<RedisKeyDto> list = null;
		sqlQueryFactory
				.select(Projections.bean(RedisKeyDto.class, qTSysHospitalConfig.hospitalCode.as("orgId"),
						qTSys.sysCode.as("sysCode"), qTInterface.interfaceUrl.as("funCode")))
				.from(qTProject).leftJoin(qTPlatform).on(qTPlatform.projectId.eq(qTProject.id)).leftJoin(qTSysConfig)
				.on(qTSysConfig.platformId.eq(qTPlatform.id)).leftJoin(qTSys).on(qTSysConfig.sysId.eq(qTSys.id))
				.leftJoin(qTInterface).on(qTSys.id.eq(qTInterface.sysId)).leftJoin(qTBusinessInterface)
				.on(qTBusinessInterface.requestInterfaceId.eq(qTInterface.id)
						.and(qTBusinessInterface.requestSysconfigId.eq(qTSysConfig.id)
								.or(qTBusinessInterface.requestedSysconfigId.eq(qTSysConfig.id))))
				.leftJoin(qTSysHospitalConfig).on(qTSysConfig.id.eq(qTSysHospitalConfig.sysConfigId))
				.leftJoin(qTHospital).on(qTSysHospitalConfig.hospitalId.eq(qTHospital.id)).leftJoin(qTSysDriveLink)
				.on(qTSysDriveLink.sysId.eq(qTSys.id)).leftJoin(qTDrive).on(qTDrive.id.eq(qTSysDriveLink.driveId))
				.where(arr.toArray(new Predicate[arr.size()]))
				.groupBy(qTSys.sysCode, qTSysHospitalConfig.hospitalCode, qTInterface.interfaceUrl).fetch();
		return list;
	}

	/**
	 * 清除缓存
	 * 
	 * @param list
	 */
	public void delRedisKey(List<RedisKeyDto> list) {
		for (RedisKeyDto obj : list) {
			String key = "IntegratedPlatform:Configs:_" + obj.getOrgId() + "_" + obj.getSysCode() + "_"
					+ obj.getFunCode();
			redisUtil.del(key);
		}
		logger.info("删除缓存{}条", list.size());
	}

}
