package com.iflytek.integrated.platform.service;

import static com.iflytek.integrated.platform.entity.QTSysConfig.qTSysConfig;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.entity.TSysConfig;
import com.querydsl.core.types.dsl.StringPath;

/**
 * 厂商配置
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
		return sqlQueryFactory.select(qTSysConfig).from(qTSysConfig)
				.where(qTSysConfig.platformId.eq(platformId).and(qTSysConfig.sysId.eq(sysId)).and(qTSysConfig.sysConfigType.eq("1"))).fetchFirst();
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
	 * @param vendorId
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
	 * @param platformId
	 */
	public List<TSysConfig> getSysConfigByHospital(String hospitalId) {
		return sqlQueryFactory.select(qTSysConfig).from(qTSysConfig).where(qTSysConfig.hospitalIds.contains(hospitalId))
				.fetch();
	}

}
