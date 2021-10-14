package com.iflytek.integrated.platform.service;

import static com.iflytek.integrated.platform.entity.QTEtlFlow.qTEtlFlow;
import static com.iflytek.integrated.platform.entity.QTEtlGroup.qTEtlGroup;
import static com.iflytek.integrated.platform.entity.QTHospital.qTHospital;
import static com.iflytek.integrated.platform.entity.QTPlatform.qTPlatform;
import static com.iflytek.integrated.platform.entity.QTSys.qTSys;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.common.dto.TableData;
import com.iflytek.integrated.common.intercept.UserLoginIntercept;
import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.platform.dto.EtlFlowDto;
import com.iflytek.integrated.platform.dto.EtlGroupDto;
import com.iflytek.integrated.platform.entity.TEtlFlow;
import com.iflytek.integrated.platform.entity.TPlatform;
import com.iflytek.medicalboot.core.id.BatchUidService;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.dml.SQLUpdateClause;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lsn
 */
@Slf4j
@Api(tags = "etl流程服务")
@RestController
@RequestMapping("/{version}/pt/etlflow")
public class EtlFlowService extends BaseService<TEtlFlow, String, StringPath> {

	private static final Logger logger = LoggerFactory.getLogger(EtlFlowService.class);

	@Autowired
	private BatchUidService batchUidService;

	@Autowired
	private EtlGroupService etlGroupService;

	public EtlFlowService() {
		super(qTEtlFlow, qTEtlFlow.id);
	}

	@ApiOperation(value = "获取流程列表")
	@PostMapping("/getEtlFlows")
	public ResultDto<TableData<TEtlFlow>> getEtlFlows(@RequestBody TEtlFlow queryCondition,
			@ApiParam(value = "页码", example = "1") @RequestParam(value = "pageNo", defaultValue = "1", required = false) Integer pageNo,
			@ApiParam(value = "每页大小", example = "10") @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
		// 查询条件
		ArrayList<Predicate> list = new ArrayList<>();
		if (StringUtils.isNotBlank(queryCondition.getPlatformId())) {
			list.add(qTEtlGroup.platformId.eq(queryCondition.getPlatformId()));
		}
		if(StringUtils.isNotBlank(queryCondition.getStatus())){
			list.add(qTEtlFlow.status.eq(queryCondition.getStatus()));
		}
		if (StringUtils.isNotBlank(queryCondition.getGroupId())) {
			list.add(qTEtlFlow.groupId.eq(queryCondition.getGroupId()));
		}
		if (StringUtils.isNotBlank(queryCondition.getEtlGroupId())) {
			list.add(qTEtlFlow.etlGroupId.eq(queryCondition.getEtlGroupId()));
		}
		if (StringUtils.isNotBlank(queryCondition.getFlowName())) {
			list.add(qTEtlFlow.flowName.like("%" + queryCondition.getFlowName() + "%"));
		}
		if (StringUtils.isNotBlank(queryCondition.getFlowDesp())) {
			list.add(qTEtlFlow.flowDesp.like("%" + queryCondition.getFlowDesp() + "%"));
		}
		QueryResults<TEtlFlow> queryResults = sqlQueryFactory.select(Projections.bean(TEtlFlow.class, qTEtlFlow.id,
				qTEtlFlow.groupId, qTEtlFlow.flowName, qTEtlFlow.etlGroupId, qTEtlFlow.flowConfig, qTEtlFlow.flowDesp,
				qTEtlFlow.flowTplName, qTEtlFlow.funTplNames, qTEtlFlow.status,qTEtlFlow.etlEntryGroupId,qTEtlFlow.parentGroupId,
//				qTProject.projectCode.as("projectCode"),
				qTEtlFlow.etlControlId,qTEtlGroup.hospitalId, qTEtlGroup.sysId, qTHospital.hospitalName.as("hospitalName"),
				qTSys.sysName.as("sysName"))).from(qTEtlFlow).leftJoin(qTEtlGroup)
				.on(qTEtlFlow.groupId.eq(qTEtlGroup.id)).leftJoin(qTPlatform)
				.on(qTEtlGroup.platformId.eq(qTPlatform.id)).leftJoin(qTSys).on(qTEtlGroup.sysId.eq(qTSys.id))
				.leftJoin(qTHospital).on(qTEtlGroup.hospitalId.eq(qTHospital.id))
				.where(list.toArray(new Predicate[list.size()])).limit(pageSize).offset((pageNo - 1) * pageSize)
				.orderBy(qTEtlFlow.createdTime.desc()).fetchResults();

		// 分页
		TableData<TEtlFlow> tableData = new TableData<>(queryResults.getTotal(), queryResults.getResults());
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取流程列表成功", tableData);
	}
	
	@ApiOperation(value = "根据分类ID获取分类下所有流程列表")
	@GetMapping("/getEtlFlowsByPlatform/{platformId}")
	public ResultDto<List<TEtlFlow>> getEtlFlowsByPlatform(@PathVariable String platformId) {
		List<TEtlFlow> queryResults = sqlQueryFactory.select(Projections.bean(TEtlFlow.class, qTEtlFlow.id,
		qTEtlFlow.groupId, qTEtlFlow.flowName, qTEtlFlow.etlGroupId, qTEtlFlow.flowConfig, qTEtlFlow.flowDesp,
		qTEtlFlow.flowTplName, qTEtlFlow.funTplNames, qTEtlFlow.status,qTEtlFlow.etlEntryGroupId,qTEtlFlow.parentGroupId,
		qTEtlFlow.etlControlId, qTEtlGroup.hospitalId, qTEtlGroup.sysId, qTHospital.hospitalName.as("hospitalName"),
		qTSys.sysName.as("sysName"))).from(qTEtlFlow).leftJoin(qTEtlGroup)
		.on(qTEtlFlow.groupId.eq(qTEtlGroup.id)).leftJoin(qTPlatform)
		.on(qTEtlGroup.platformId.eq(qTPlatform.id)).leftJoin(qTSys).on(qTEtlGroup.sysId.eq(qTSys.id))
		.leftJoin(qTHospital).on(qTEtlGroup.hospitalId.eq(qTHospital.id))
		.where(qTEtlGroup.platformId.eq(platformId)).fetch();
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取流程列表成功", queryResults);
	}

	@ApiOperation(value = "获取流程详情")
	@GetMapping("/getEtlFlows/{id}")
	public ResultDto<TEtlFlow> getEtlFlowDetails(@PathVariable("id") String id) {
		TEtlFlow flowDetail = sqlQueryFactory.select(Projections.bean(TEtlFlow.class, qTEtlFlow.id, qTEtlFlow.groupId,
				qTEtlFlow.flowName, qTEtlFlow.etlGroupId, qTEtlFlow.flowConfig, qTEtlFlow.flowDesp,
				qTEtlFlow.flowTplName, qTEtlFlow.funTplNames,qTEtlFlow.status,qTEtlFlow.etlEntryGroupId,qTEtlFlow.parentGroupId,
				qTEtlFlow.etlControlId,
//				qTProject.projectCode.as("projectCode"),
				qTEtlGroup.hospitalId, qTEtlGroup.sysId, qTHospital.hospitalName.as("hospitalName"),
				qTSys.sysName.as("sysName"))).from(qTEtlFlow).leftJoin(qTEtlGroup)
				.on(qTEtlFlow.groupId.eq(qTEtlGroup.id)).leftJoin(qTSys).on(qTEtlGroup.sysId.eq(qTSys.id))
				.leftJoin(qTHospital).on(qTEtlGroup.hospitalId.eq(qTHospital.id)).where(qTEtlFlow.id.eq(id))
				.fetchFirst();

		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取流程列表成功", flowDetail);
	}

	@ApiOperation(value = "保存流程配置")
	@PostMapping(path = "/saveEtlFlow")
	public ResultDto<String> saveEtlFlow(@RequestBody EtlFlowDto flowDto) {

		// 校验是否获取到登录用户
		String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
		if (StringUtils.isBlank(loginUserName)) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!", "没有获取到登录用户!");
		}
		EtlGroupDto groupDto = flowDto.getEtlGroupDto();
		String groupId = "";
		try {
			groupId = etlGroupService.saveEtlGroup(groupDto);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "保存流程配置处理流程组数据异常", "");
		}

		TEtlFlow flowEntity = new TEtlFlow();
		flowEntity.setGroupId(groupId);
		flowEntity.setFlowName(flowDto.getFlowName());
		flowEntity.setFlowConfig(flowDto.getFlowConfig());
		flowEntity.setFlowDesp(flowDto.getFlowDesp());
		flowEntity.setEtlGroupId(flowDto.getEtlGroupId());
		flowEntity.setFlowTplName(flowDto.getFlowTplName());
		flowEntity.setFunTplNames(flowDto.getFunTplNames());
		if(StringUtils.isNotBlank(flowDto.getParentGroupId())) {
			flowEntity.setParentGroupId(flowDto.getParentGroupId());
		}
		if(StringUtils.isNotBlank(flowDto.getEtlControlId())) {
			flowEntity.setEtlControlId(flowDto.getEtlControlId());
		}
		String id = batchUidService.getUid(qTEtlFlow.getTableName()) + "";
		flowEntity.setId(id);
		flowEntity.setCreatedBy(loginUserName != null ? loginUserName : "");
		flowEntity.setCreatedTime(new Date());
		flowEntity.setUpdatedBy(loginUserName != null ? loginUserName : "");
		flowEntity.setUpdatedTime(new Date());
		flowEntity.setStatus(flowDto.getStatus());
		flowEntity.setEtlEntryGroupId(flowDto.getEtlEntryGroupId());
		String result = this.post(flowEntity);
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "保存流程配置成功", result);
	}

	@ApiOperation(value = "修改流程配置")
	@PostMapping(path = "/updateEtlFlow/{id}")
	public ResultDto<String> editEtlFlow(@RequestBody EtlFlowDto flowDto) {

		// 校验是否获取到登录用户
		String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
		if (StringUtils.isBlank(loginUserName)) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!", "没有获取到登录用户!");
		}
		EtlGroupDto groupDto = flowDto.getEtlGroupDto();
		if (groupDto == null) {
			groupDto = new EtlGroupDto();
		}
		String groupId = groupDto.getId();
		if (StringUtils.isBlank(groupId)) {
			groupId = flowDto.getGroupId();
			groupDto.setId(groupId);
		}
		try {
			etlGroupService.editEtlGroup(groupDto);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "修改流程配置处理流程组数据异常", "");
		}

		SQLUpdateClause updateClause = sqlQueryFactory.update(qTEtlFlow);

		// 流程模板
		try {
			if (StringUtils.isNotBlank(flowDto.getFlowName())) {
				updateClause.set(qTEtlFlow.flowName, flowDto.getFlowName());
			}
			if (StringUtils.isNotBlank(flowDto.getFlowConfig())) {
				updateClause.set(qTEtlFlow.flowConfig, flowDto.getFlowConfig());
			}
			if (StringUtils.isNotBlank(flowDto.getFlowDesp())) {
				updateClause.set(qTEtlFlow.flowDesp, flowDto.getFlowDesp());
			}
			if (StringUtils.isNotBlank(flowDto.getEtlGroupId())) {
				updateClause.set(qTEtlFlow.etlGroupId, flowDto.getEtlGroupId());
			}
			if (StringUtils.isNotBlank(flowDto.getParentGroupId())) {
				updateClause.set(qTEtlFlow.parentGroupId, flowDto.getParentGroupId());
			}
			if (StringUtils.isNotBlank(flowDto.getEtlEntryGroupId())) {
				updateClause.set(qTEtlFlow.etlEntryGroupId, flowDto.getEtlEntryGroupId());
			}
			if (StringUtils.isNotBlank(flowDto.getFlowTplName())) {
				updateClause.set(qTEtlFlow.flowTplName, flowDto.getFlowTplName());
			}
			if (StringUtils.isNotBlank(flowDto.getFunTplNames())) {
				updateClause.set(qTEtlFlow.funTplNames, flowDto.getFunTplNames());
			}
			if (StringUtils.isNotBlank(flowDto.getStatus())) {
				updateClause.set(qTEtlFlow.status, flowDto.getStatus());
			}
			if (StringUtils.isNotBlank(loginUserName))
				updateClause.set(qTEtlFlow.updatedBy, loginUserName != null ? loginUserName : "");
			updateClause.set(qTEtlFlow.updatedTime, new Date());
			updateClause.where(qTEtlFlow.id.eq(flowDto.getId()));
			long insertCount = updateClause.execute();
			return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "修改流程配置成功", insertCount + "");
		} catch (Exception e) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "修改流程配置失败", e.getLocalizedMessage());
		}
	}

	@ApiOperation(value = "删除流程")
	@PostMapping(path = "/delEtlFlow/{id}")
	@Transactional(rollbackFor = Exception.class)
	public ResultDto<String> delEtlFlow(@PathVariable String id) {

		// 校验是否获取到登录用户
//		String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
//		if (StringUtils.isBlank(loginUserName)) {
//			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!", "没有获取到登录用户!");
//		}
		long result = 0;
		TEtlFlow etlFlow = this.getOne(id);
		if(etlFlow != null) {
			try {
				etlGroupService.delEtlGroup(etlFlow.getGroupId() , etlFlow.getParentGroupId());
			} catch (Exception e) {
				throw new RuntimeException("刪除ETL服务器流程异常！异常详情：" + e.getLocalizedMessage());
			}
//			long result = sqlQueryFactory.delete(qTEtlFlow).where(qTEtlFlow.id.eq(id)).execute();
			result = this.delete(id);
		}
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "删除流程成功", result + "");
	}

	/**
	 * 根据平台id查找流程
	 * @param platformId
	 * @return
	 */
	public List<TEtlFlow> getTEtlFlowIds(String platformId){
		List<TEtlFlow> etlFlowIds = sqlQueryFactory.select(Projections.bean(TEtlFlow.class, qTEtlFlow.id , qTEtlFlow.parentGroupId , qTEtlFlow.etlGroupId)).from(qTEtlFlow)
				.leftJoin(qTEtlGroup).on(qTEtlFlow.groupId.eq(qTEtlGroup.id))
				.where(qTEtlGroup.platformId.eq(platformId)).fetch();
		return etlFlowIds;
	}
	
	@ApiOperation(value = "清空流程队列")
	@PostMapping(path = "/emptyEtlFlow/{id}")
	@Transactional(rollbackFor = Exception.class)
	public ResultDto<String> emptyEtlFlow(@PathVariable String id) {
		TEtlFlow flow = this.getOne(id);
		if(flow == null) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "清空流程队列失败，流程不存在", "清空流程队列失败，流程不存在");
		}
		TPlatform platform = sqlQueryFactory.select(qTPlatform).from(qTEtlFlow).join(qTEtlGroup)
				.on(qTEtlFlow.groupId.eq(qTEtlGroup.id)).join(qTPlatform).on(qTPlatform.id.eq(qTEtlGroup.platformId))
				.where(qTEtlFlow.id.eq(id)).fetchFirst();
		if(platform == null) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "清空流程队列失败，平台分类不存在", "清空流程队列失败，平台分类不存在");
		}
		Map<String , Object> params = new HashMap<String , Object>();
		params.put("tEtlGroupId", flow.getEtlGroupId());
		params.put("etlServerUrl", platform.getEtlServerUrl());
		params.put("etlUser", platform.getEtlUser());
		params.put("etlPwd", platform.getEtlPwd());
		return etlGroupService.emptyEtlGroupQueues(params);
		
	}
}
