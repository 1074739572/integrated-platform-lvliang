package com.iflytek.integrated.platform.service;

import com.alibaba.fastjson.JSON;
import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.dto.HospitalDto;
import com.iflytek.integrated.platform.entity.TSysConfig;
import com.querydsl.core.types.dsl.StringPath;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.iflytek.integrated.platform.entity.QTSysConfig.qTSysConfig;

/**
 * 系统配置
 * 
 * @author weihe9
 * @date 2020/12/14 10:58
 */
@Service
public class SysConfigService extends BaseService<TSysConfig, String, StringPath> {

	private static final Logger logger = LoggerFactory.getLogger(SysConfigService.class);

	public SysConfigService() {
		super(qTSysConfig, qTSysConfig.id);
	}

	/**
	 * 获取平台-系统配置
	 * 
	 * @param platformId
	 * @param sysId
	 * @return
	 */
	public TSysConfig getRequestConfigByPlatformAndSys(String platformId, String sysId) {
		return sqlQueryFactory.select(qTSysConfig).from(qTSysConfig).where(qTSysConfig.platformId.eq(platformId)
				.and(qTSysConfig.sysId.eq(sysId)).and(qTSysConfig.sysConfigType.eq("1"))).fetchFirst();
	}

	public TSysConfig getConfigByPlatformAndSys(String platformId, String sysId) {
		return sqlQueryFactory.select(qTSysConfig).from(qTSysConfig)
				.where(qTSysConfig.platformId.eq(platformId).and(qTSysConfig.sysId.eq(sysId))).fetchFirst();
	}

	/**
	 * 获取平台-所有系统配置
	 * 
	 * @param platformId
	 * @return
	 */
	public List<TSysConfig> getObjByPlatformId(String platformId) {
		return sqlQueryFactory.select(qTSysConfig).from(qTSysConfig).where(qTSysConfig.platformId.eq(platformId))
				.fetch();
	}

	/**
	 * 根据系统获取所有系统配置信息
	 * 
	 * @param sysId
	 * @return
	 */
	public List<TSysConfig> getObjBySysId(String sysId) {
		List<TSysConfig> list = sqlQueryFactory.select(qTSysConfig).from(qTSysConfig).where(qTSysConfig.sysId.eq(sysId))
				.fetch();
		return list;
	}

	/**
	 * 删除平台下所有系统配置信息
	 * 
	 * @param platformId
	 */
	public long delSysConfigAll(String platformId) {
		return sqlQueryFactory.delete(qTSysConfig).where(qTSysConfig.platformId.eq(platformId)).execute();
	}

	/**
	 * 删除平台下所有系统配置信息
	 * 
	 * @param hospitalId
	 */
	public List<TSysConfig> getSysConfigByHospital(String hospitalId) {
		return sqlQueryFactory.select(qTSysConfig).from(qTSysConfig)
				.where(qTSysConfig.hospitalConfigs.contains(hospitalId)).fetch();
	}

	public long delSysConfigHospital(TSysConfig sysConfig, String hospitalId) {
		if (StringUtils.isBlank(hospitalId)) {
			return sqlQueryFactory.update(qTSysConfig).set(qTSysConfig.hospitalConfigs, "")
					.where(qTSysConfig.id.eq(sysConfig.getId())).execute();
		}
		String hospitals = sysConfig.getHospitalConfigs();
		if (StringUtils.isBlank(hospitals)) {
			return 0;
		}
		List<HospitalDto> hosList = JSON.parseArray(hospitals, HospitalDto.class);
		List<HospitalDto> newHosList = hosList.stream().filter(hd -> !hospitalId.equals(hd.getHospitalId()))
				.collect(Collectors.toList());
		return sqlQueryFactory.update(qTSysConfig).set(qTSysConfig.hospitalConfigs,
				JSON.toJSONString(newHosList))
				.where(qTSysConfig.id.eq(sysConfig.getId())).execute();
	}

}
