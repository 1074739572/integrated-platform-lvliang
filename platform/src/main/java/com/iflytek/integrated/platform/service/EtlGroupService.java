package com.iflytek.integrated.platform.service;

import static com.iflytek.integrated.platform.entity.QTEtlGroup.qTEtlGroup;
import static com.iflytek.integrated.platform.entity.QTPlatform.qTPlatform;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.common.intercept.UserLoginIntercept;
import com.iflytek.integrated.common.utils.JackSonUtils;
import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.platform.dto.EtlGroupDto;
import com.iflytek.integrated.platform.entity.TEtlGroup;
import com.iflytek.integrated.platform.entity.TPlatform;
import com.iflytek.integrated.platform.utils.NiFiRequestUtil;
import com.iflytek.medicalboot.core.id.BatchUidService;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.dml.SQLUpdateClause;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author lsn
 */
@Slf4j
@Service
public class EtlGroupService extends BaseService<TEtlGroup, String, StringPath> {

	private static final Logger logger = LoggerFactory.getLogger(EtlGroupService.class);

	@Autowired
	private BatchUidService batchUidService;
	@Autowired
	private NiFiRequestUtil niFiRequestUtil;

	public EtlGroupService() {
		super(qTEtlGroup, qTEtlGroup.id);
	}

	public ResultDto<List<EtlGroupDto>> getEtlGroups(@RequestBody TEtlGroup queryCondition) {
		// 查询条件
		ArrayList<Predicate> list = new ArrayList<>();
		if (StringUtils.isNotBlank(queryCondition.getProjectId())) {
			list.add(qTEtlGroup.projectId.eq(queryCondition.getProjectId()));
		}
		if (StringUtils.isNotBlank(queryCondition.getPlatformId())) {
			list.add(qTEtlGroup.platformId.eq(queryCondition.getPlatformId()));
		}
		if (StringUtils.isNotBlank(queryCondition.getHospitalId())) {
			list.add(qTEtlGroup.hospitalId.eq(queryCondition.getHospitalId()));
		}
		if (StringUtils.isNotBlank(queryCondition.getSysId())) {
			list.add(qTEtlGroup.sysId.eq(queryCondition.getSysId()));
		}
		if (StringUtils.isNotBlank(queryCondition.getEtlGroupId())) {
			list.add(qTEtlGroup.etlGroupId.eq(queryCondition.getEtlGroupId()));
		}
		List<TEtlGroup> groupList = sqlQueryFactory.select(qTEtlGroup).from(qTEtlGroup)
				.where(list.toArray(new Predicate[list.size()])).fetch();

		List<EtlGroupDto> groupDtoList = JackSonUtils.jsonToTransferList(groupList, EtlGroupDto.class);
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "", groupDtoList);
	}

	public String saveEtlGroup(EtlGroupDto groupDto,String loginUserName) throws Exception {

		TEtlGroup groupEntity = JackSonUtils.jsonToTransfer(JackSonUtils.transferToJson(groupDto), TEtlGroup.class);
		ResultDto<List<EtlGroupDto>> queryResults = this.getEtlGroups(groupEntity);
		if (queryResults != null && queryResults.getData() != null && queryResults.getData().size() > 0) {
			return queryResults.getData().get(0).getId();
		}
		String id = batchUidService.getUid(qTEtlGroup.getTableName()) + "";
		groupEntity.setId(id);
		groupEntity.setCreatedBy(loginUserName != null ? loginUserName : "");
		groupEntity.setCreatedTime(new Date());
		groupEntity.setUpdatedBy(loginUserName != null ? loginUserName : "");
		groupEntity.setUpdatedTime(new Date());
		this.post(groupEntity);
		return id;
	}

	public ResultDto<String> editEtlGroup(EtlGroupDto groupDto,String loginUserName) {
		String etlGroupName = groupDto.getEtlGroupName();
		String etlGroupId = groupDto.getEtlGroupId();
		String sysId = groupDto.getSysId();
		String hospitalId = groupDto.getHospitalId();
		SQLUpdateClause updateClause = sqlQueryFactory.update(qTEtlGroup);
		// 流程模板
		try {
			if (StringUtils.isNotBlank(etlGroupName)) {
				updateClause.set(qTEtlGroup.etlGroupName, etlGroupName);
			}
			if (StringUtils.isNotBlank(etlGroupId)) {
				updateClause.set(qTEtlGroup.etlGroupId, etlGroupId);
			}
			if (StringUtils.isNotBlank(sysId)) {
				updateClause.set(qTEtlGroup.sysId, sysId);
			}
			if (StringUtils.isNotBlank(hospitalId)) {
				updateClause.set(qTEtlGroup.hospitalId, hospitalId);
			}
			updateClause.set(qTEtlGroup.updatedBy, loginUserName != null ? loginUserName : "");
			updateClause.set(qTEtlGroup.updatedTime, new Date()).addBatch();
			updateClause.where(qTEtlGroup.id.eq(groupDto.getId()));
			long insertCount = updateClause.execute();
			return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "修改流程组名称成功", insertCount + "");
		} catch (Exception e) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "修改流程组名称失败", e.getLocalizedMessage());
		}
	}

	@Transactional
	public ResultDto<String> delEtlGroup(String id , String parentGroupId) throws Exception {
		TEtlGroup etlGroup = this.getOne(id);
		long result = 0;
		if(etlGroup != null) {
			TPlatform platform = sqlQueryFactory.select(qTPlatform).from(qTPlatform).where(qTPlatform.id.eq(etlGroup.getPlatformId())).fetchOne();
			result = this.delete(id);
			niFiRequestUtil.deleteNifiEtlFlow(platform, etlGroup.getEtlGroupId() , parentGroupId);
//			long result = sqlQueryFactory.delete(qTEtlGroup).where(qTEtlGroup.id.eq(id)).execute();
		}
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "删除流程组成功", result + "");
	}
	public ResultDto<String> stopEtlGroup(String id) throws Exception {
		TEtlGroup etlGroup = this.getOne(id);
		if(etlGroup != null) {
			TPlatform platform = sqlQueryFactory.select(qTPlatform).from(qTPlatform).where(qTPlatform.id.eq(etlGroup.getPlatformId())).fetchOne();
			niFiRequestUtil.stopNifiEtlFlow(platform, etlGroup.getEtlGroupId());
		}
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "停止流程成功", "success");
	}
	/**
	 * 根据平台id查找流程组
	 * @param platformId
	 * @return
	 */
	public List<String> getTEtlGroupIds(String platformId){
		List<String> etlGroupIds = sqlQueryFactory.select(qTEtlGroup.etlGroupId).from(qTEtlGroup)
				.where(qTEtlGroup.platformId.eq(platformId)).fetch();
		return etlGroupIds;
	}
	
	/**
	 * 清空etl组队列信息
	 * @param
	 * @return
	 * @throws Exception
	 */
	public ResultDto<String> emptyEtlGroupQueues(Map<String , Object> params){
		try {
			niFiRequestUtil.emptyNifiCollections(params);
			return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "清空流程组队列成功", "");
		}catch(Exception e) {
			logger.error("清空流程组队列失败" , e);
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "清空流程组队列失败:" + e.getLocalizedMessage(), e.getLocalizedMessage());
		}
	}
}
