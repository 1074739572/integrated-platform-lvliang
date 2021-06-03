package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.entity.TSysDriveLink;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.StringPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.iflytek.integrated.platform.entity.QTDrive.qTDrive;
import static com.iflytek.integrated.platform.entity.QTSysDriveLink.qTSysDriveLink;

/**
 * 系统与驱动关联
 * 
 * @author weihe9
 * @date 2020/12/12 14:16
 */
@Service
public class SysDriveLinkService extends BaseService<TSysDriveLink, String, StringPath> {

	private static final Logger logger = LoggerFactory.getLogger(SysDriveLinkService.class);

	public SysDriveLinkService() {
		super(qTSysDriveLink, qTSysDriveLink.id);
	}

	/**
	 * 根据系统id删除系统与驱动关联信息
	 * 
	 * @param sysId
	 */
	public long deleteSysDriveLinkBySysId(String sysId) {
		return sqlQueryFactory.delete(qTSysDriveLink).where(qTSysDriveLink.sysId.eq(sysId)).execute();
	}

	/**
	 * 根据系统id获取系统驱动关联
	 * 
	 * @param sysId
	 * @return
	 */
	public List<TSysDriveLink> getSysDriveLinkBySysId(String sysId) {
		return sqlQueryFactory.select(Projections.bean(qTSysDriveLink, qTDrive.driveName.as("driveName")))
				.from(qTSysDriveLink).leftJoin(qTDrive).on(qTDrive.id.eq(qTSysDriveLink.driveId))
				.where(qTSysDriveLink.sysId.eq(sysId)).fetch();
	}

	/**
	 * 根据驱动id获取系统驱动关联
	 * 
	 * @param driveId
	 * @return
	 */
	public List<TSysDriveLink> getSysDriveLinkByDriveId(String driveId) {
		return sqlQueryFactory.select(qTSysDriveLink).from(qTSysDriveLink).where(qTSysDriveLink.driveId.eq(driveId))
				.fetch();
	}

}
