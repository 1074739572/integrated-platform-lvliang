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
import java.util.Set;
import java.util.stream.Collectors;

import com.iflytek.integrated.platform.dto.BatchEtlFlowDto;
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
@Api(tags = "etl????????????")
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

	@ApiOperation(value = "??????????????????")
	@PostMapping("/getEtlFlows")
	public ResultDto<TableData<TEtlFlow>> getEtlFlows(@RequestBody TEtlFlow queryCondition,
			@ApiParam(value = "??????", example = "1") @RequestParam(value = "pageNo", defaultValue = "1", required = false) Integer pageNo,
			@ApiParam(value = "????????????", example = "10") @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
		// ????????????
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
		list.add(qTEtlFlow.parentEtlGroupId.eq("0"));
		QueryResults<TEtlFlow> queryResults = sqlQueryFactory.select(Projections.bean(TEtlFlow.class, qTEtlFlow.id,
				qTEtlFlow.groupId, qTEtlFlow.flowName, qTEtlFlow.etlGroupId, qTEtlFlow.flowConfig, qTEtlFlow.flowDesp,
				qTEtlFlow.flowTplName, qTEtlFlow.funTplNames, qTEtlFlow.status,qTEtlFlow.etlEntryGroupId,qTEtlFlow.parentGroupId,
				qTEtlFlow.parentEtlGroupId,qTEtlFlow.alertDuration,
				qTEtlFlow.maxDuration,qTEtlFlow.lastDebugTime,qTEtlFlow.etlJobcontrolId,
				qTEtlFlow.etlControlId,qTEtlGroup.hospitalId, qTEtlGroup.sysId, qTHospital.hospitalName.as("hospitalName"),
				qTSys.sysName.as("sysName"))).from(qTEtlFlow).leftJoin(qTEtlGroup)
				.on(qTEtlFlow.groupId.eq(qTEtlGroup.id)).leftJoin(qTPlatform)
				.on(qTEtlGroup.platformId.eq(qTPlatform.id)).leftJoin(qTSys).on(qTEtlGroup.sysId.eq(qTSys.id))
				.leftJoin(qTHospital).on(qTEtlGroup.hospitalId.eq(qTHospital.id))
				.where(list.toArray(new Predicate[list.size()])).limit(pageSize).offset((pageNo - 1) * pageSize)
				.orderBy(qTEtlFlow.createdTime.desc()).fetchResults();

		List<TEtlFlow> etlFlows = queryResults.getResults();
		Set<String> flowids = etlFlows.stream().collect(Collectors.groupingBy(TEtlFlow::getEtlGroupId)).keySet();
		List<TEtlFlow> childFlows = sqlQueryFactory.select(Projections.bean(TEtlFlow.class, qTEtlFlow.id,
				qTEtlFlow.groupId, qTEtlFlow.flowName, qTEtlFlow.etlGroupId, qTEtlFlow.flowConfig, qTEtlFlow.flowDesp,
				qTEtlFlow.flowTplName, qTEtlFlow.funTplNames, qTEtlFlow.status,qTEtlFlow.etlEntryGroupId,qTEtlFlow.parentGroupId,
				qTEtlFlow.parentEtlGroupId,qTEtlFlow.alertDuration, 
				qTEtlFlow.maxDuration,qTEtlFlow.lastDebugTime,qTEtlFlow.etlJobcontrolId,
				qTEtlFlow.etlControlId,qTEtlGroup.hospitalId, qTEtlGroup.sysId, qTHospital.hospitalName.as("hospitalName"),
				qTSys.sysName.as("sysName"))).from(qTEtlFlow).leftJoin(qTEtlGroup)
				.on(qTEtlFlow.groupId.eq(qTEtlGroup.id)).leftJoin(qTPlatform)
				.on(qTEtlGroup.platformId.eq(qTPlatform.id)).leftJoin(qTSys).on(qTEtlGroup.sysId.eq(qTSys.id))
				.leftJoin(qTHospital).on(qTEtlGroup.hospitalId.eq(qTHospital.id))
				.where(qTEtlFlow.parentEtlGroupId.in(flowids)).fetch();
		Map<String, List<TEtlFlow>> childflowsMap = childFlows.stream().collect(Collectors.groupingBy(TEtlFlow::getParentEtlGroupId));
		for(TEtlFlow flow : etlFlows) {
			String topleveletlgroupid = flow.getEtlGroupId();
			if(childflowsMap.containsKey(topleveletlgroupid)) {
				flow.setChildrenFlows(childflowsMap.get(topleveletlgroupid));
			}else {
				flow.setChildrenFlows(null);
			}
		}
		// ??????
		TableData<TEtlFlow> tableData = new TableData<>(queryResults.getTotal(), queryResults.getResults());
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "????????????????????????", tableData);
	}
	
	@ApiOperation(value = "????????????ID?????????????????????????????????")
	@GetMapping("/getEtlFlowsByPlatform/{platformId}")
	public ResultDto<List<TEtlFlow>> getEtlFlowsByPlatform(@PathVariable String platformId) {
		List<TEtlFlow> queryResults = sqlQueryFactory.select(Projections.bean(TEtlFlow.class, qTEtlFlow.id,
		qTEtlFlow.groupId, qTEtlFlow.flowName, qTEtlFlow.etlGroupId, qTEtlFlow.flowConfig, qTEtlFlow.flowDesp,qTEtlFlow.maxDuration,qTEtlFlow.alertDuration,
		qTEtlFlow.flowTplName, qTEtlFlow.funTplNames, qTEtlFlow.status,qTEtlFlow.etlEntryGroupId,qTEtlFlow.parentGroupId,qTEtlFlow.lastDebugTime,qTEtlFlow.etlJobcontrolId,
		qTEtlFlow.etlControlId, qTEtlGroup.hospitalId, qTEtlGroup.sysId, qTHospital.hospitalName.as("hospitalName"),
		qTSys.sysName.as("sysName"))).from(qTEtlFlow).leftJoin(qTEtlGroup)
		.on(qTEtlFlow.groupId.eq(qTEtlGroup.id)).leftJoin(qTPlatform)
		.on(qTEtlGroup.platformId.eq(qTPlatform.id)).leftJoin(qTSys).on(qTEtlGroup.sysId.eq(qTSys.id))
		.leftJoin(qTHospital).on(qTEtlGroup.hospitalId.eq(qTHospital.id))
		.where(qTEtlGroup.platformId.eq(platformId)).fetch();
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "????????????????????????", queryResults);
	}
	
	@ApiOperation(value = "????????????ID?????????????????????????????????")
	@GetMapping("/getChildEtlFlows/{parentEtlGroupId}")
	public ResultDto<List<TEtlFlow>> getChildEtlFlows(@PathVariable String parentEtlGroupId) {
		List<TEtlFlow> queryResults = sqlQueryFactory.select(Projections.bean(TEtlFlow.class, qTEtlFlow.id,
		qTEtlFlow.groupId, qTEtlFlow.flowName, qTEtlFlow.etlGroupId, qTEtlFlow.flowConfig, qTEtlFlow.flowDesp,qTEtlFlow.maxDuration,
		qTEtlFlow.flowTplName, qTEtlFlow.funTplNames, qTEtlFlow.status,qTEtlFlow.etlEntryGroupId,qTEtlFlow.parentGroupId,qTEtlFlow.lastDebugTime,
		qTEtlFlow.etlControlId, qTEtlGroup.hospitalId, qTEtlGroup.sysId, qTHospital.hospitalName.as("hospitalName"),
		qTSys.sysName.as("sysName"))).from(qTEtlFlow).leftJoin(qTEtlGroup)
		.on(qTEtlFlow.groupId.eq(qTEtlGroup.id)).leftJoin(qTPlatform)
		.on(qTEtlGroup.platformId.eq(qTPlatform.id)).leftJoin(qTSys).on(qTEtlGroup.sysId.eq(qTSys.id))
		.leftJoin(qTHospital).on(qTEtlGroup.hospitalId.eq(qTHospital.id))
		.where(qTEtlFlow.parentEtlGroupId.eq(parentEtlGroupId)).fetch();
		
		Set<String> flowids = queryResults.stream().collect(Collectors.groupingBy(TEtlFlow::getEtlGroupId)).keySet();
		List<TEtlFlow> childFlows = sqlQueryFactory.select(Projections.bean(TEtlFlow.class, qTEtlFlow.id,
				qTEtlFlow.groupId, qTEtlFlow.flowName, qTEtlFlow.etlGroupId, qTEtlFlow.flowConfig, qTEtlFlow.flowDesp,
				qTEtlFlow.flowTplName, qTEtlFlow.funTplNames, qTEtlFlow.status,qTEtlFlow.etlEntryGroupId,qTEtlFlow.parentGroupId,
				qTEtlFlow.parentEtlGroupId,qTEtlFlow.alertDuration, 
				qTEtlFlow.maxDuration,qTEtlFlow.lastDebugTime,qTEtlFlow.etlJobcontrolId,
				qTEtlFlow.etlControlId,qTEtlGroup.hospitalId, qTEtlGroup.sysId, qTHospital.hospitalName.as("hospitalName"),
				qTSys.sysName.as("sysName"))).from(qTEtlFlow).leftJoin(qTEtlGroup)
				.on(qTEtlFlow.groupId.eq(qTEtlGroup.id)).leftJoin(qTPlatform)
				.on(qTEtlGroup.platformId.eq(qTPlatform.id)).leftJoin(qTSys).on(qTEtlGroup.sysId.eq(qTSys.id))
				.leftJoin(qTHospital).on(qTEtlGroup.hospitalId.eq(qTHospital.id))
				.where(qTEtlFlow.parentEtlGroupId.in(flowids)).fetch();
		Map<String, List<TEtlFlow>> childflowsMap = childFlows.stream().collect(Collectors.groupingBy(TEtlFlow::getParentEtlGroupId));
		for(TEtlFlow flow : queryResults) {
			String topleveletlgroupid = flow.getEtlGroupId();
			if(childflowsMap.containsKey(topleveletlgroupid)) {
				flow.setChildrenFlows(childflowsMap.get(topleveletlgroupid));
			}else {
				flow.setChildrenFlows(null);
			}
		}
		
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "????????????????????????", queryResults);
	}

	@ApiOperation(value = "??????????????????")
	@GetMapping("/getEtlFlows/{id}")
	public ResultDto<TEtlFlow> getEtlFlowDetails(@PathVariable("id") String id) {
		TEtlFlow flowDetail = sqlQueryFactory.select(Projections.bean(TEtlFlow.class, qTEtlFlow.id, qTEtlFlow.groupId,
				qTEtlFlow.flowName, qTEtlFlow.etlGroupId, qTEtlFlow.flowConfig, qTEtlFlow.flowDesp,
				qTEtlFlow.flowTplName, qTEtlFlow.funTplNames,qTEtlFlow.status,qTEtlFlow.etlEntryGroupId,qTEtlFlow.parentGroupId,
				qTEtlFlow.etlControlId,qTEtlFlow.maxDuration,qTEtlFlow.alertDuration, qTEtlFlow.lastDebugTime,qTEtlFlow.etlJobcontrolId,
//				qTProject.projectCode.as("projectCode"),
				qTEtlGroup.hospitalId, qTEtlGroup.sysId, qTHospital.hospitalName.as("hospitalName"),
				qTSys.sysName.as("sysName"), qTEtlFlow.parentEtlGroupId)).from(qTEtlFlow).leftJoin(qTEtlGroup)
				.on(qTEtlFlow.groupId.eq(qTEtlGroup.id)).leftJoin(qTSys).on(qTEtlGroup.sysId.eq(qTSys.id))
				.leftJoin(qTHospital).on(qTEtlGroup.hospitalId.eq(qTHospital.id)).where(qTEtlFlow.id.eq(id))
				.fetchFirst();

		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "????????????????????????", flowDetail);
	}

	@ApiOperation(value = "??????????????????")
	@PostMapping(path = "/saveEtlFlow")
	public ResultDto<String> saveEtlFlow(@RequestBody EtlFlowDto flowDto, @RequestParam("loginUserName") String loginUserName) {

		// ?????????????????????????????????
		if (StringUtils.isBlank(loginUserName)) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "???????????????????????????!", "???????????????????????????!");
		}
		return this.doSaveEtlFLow(flowDto, loginUserName);

	}

	/**
	 * ??????
	 * @param flowDto
	 * @param loginUserName
	 * @return
	 */
	public ResultDto doSaveEtlFLow(EtlFlowDto flowDto, String loginUserName){
		EtlGroupDto groupDto = flowDto.getEtlGroupDto();
		String groupId = "";
		try {
			groupId = etlGroupService.saveEtlGroup(groupDto,loginUserName);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "?????????????????????????????????????????????", "");
		}

		TEtlFlow flowEntity = new TEtlFlow();
		flowEntity.setGroupId(groupId);
		flowEntity.setFlowName(flowDto.getFlowName());
		flowEntity.setFlowConfig(flowDto.getFlowConfig());
		flowEntity.setFlowDesp(flowDto.getFlowDesp());
		flowEntity.setEtlGroupId(flowDto.getEtlGroupId());
		flowEntity.setFlowTplName(flowDto.getFlowTplName());
		flowEntity.setFunTplNames(flowDto.getFunTplNames());
		if(flowDto.getMaxDuration() != null) {
			flowEntity.setMaxDuration(flowDto.getMaxDuration());
		}
		if(flowDto.getAlertDuration() != null) {
			flowEntity.setAlertDuration(flowDto.getAlertDuration());
		}
		if(StringUtils.isNotBlank(flowDto.getParentGroupId())) {
			flowEntity.setParentGroupId(flowDto.getParentGroupId());
		}
		if(StringUtils.isNotBlank(flowDto.getEtlControlId())) {
			flowEntity.setEtlControlId(flowDto.getEtlControlId());
		}
		if(StringUtils.isNotBlank(flowDto.getEtlJobcontrolId())) {
			flowEntity.setEtlJobcontrolId(flowDto.getEtlJobcontrolId());
		}
		String id = batchUidService.getUid(qTEtlFlow.getTableName()) + "";
		flowEntity.setId(id);
		flowEntity.setCreatedBy(loginUserName != null ? loginUserName : "");
		flowEntity.setCreatedTime(new Date());
		flowEntity.setUpdatedBy(loginUserName != null ? loginUserName : "");
		flowEntity.setUpdatedTime(new Date());
		flowEntity.setStatus(flowDto.getStatus());
		flowEntity.setEtlEntryGroupId(flowDto.getEtlEntryGroupId());
		flowEntity.setParentEtlGroupId(flowDto.getParentEtlGroupId());
		long l = this.post(flowEntity);
		if(l > 0){
			return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "????????????????????????", id);
		}
		return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "????????????????????????", "???????????????");
	}

	/**
	 * ??????
	 * @param flowDto
	 * @param loginUserName
	 * @return
	 */
	public void doSaveEtlFLow2(EtlFlowDto flowDto, String loginUserName){
		EtlGroupDto groupDto = flowDto.getEtlGroupDto();
		String groupId = "";
		try {
			groupId = etlGroupService.saveEtlGroup(groupDto,loginUserName);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("?????????????????????????????????????????????");
		}

		TEtlFlow flowEntity = new TEtlFlow();
		flowEntity.setGroupId(groupId);
		flowEntity.setFlowName(flowDto.getFlowName());
		flowEntity.setFlowConfig(flowDto.getFlowConfig());
		flowEntity.setFlowDesp(flowDto.getFlowDesp());
		flowEntity.setEtlGroupId(flowDto.getEtlGroupId());
		flowEntity.setFlowTplName(flowDto.getFlowTplName());
		flowEntity.setFunTplNames(flowDto.getFunTplNames());
		if(flowDto.getMaxDuration() != null) {
			flowEntity.setMaxDuration(flowDto.getMaxDuration());
		}
		if(flowDto.getAlertDuration() != null) {
			flowEntity.setAlertDuration(flowDto.getAlertDuration());
		}
		if(StringUtils.isNotBlank(flowDto.getParentGroupId())) {
			flowEntity.setParentGroupId(flowDto.getParentGroupId());
		}
		if(StringUtils.isNotBlank(flowDto.getEtlControlId())) {
			flowEntity.setEtlControlId(flowDto.getEtlControlId());
		}
		if(StringUtils.isNotBlank(flowDto.getEtlJobcontrolId())) {
			flowEntity.setEtlJobcontrolId(flowDto.getEtlJobcontrolId());
		}
		flowEntity.setCreatedBy(loginUserName != null ? loginUserName : "");
		flowEntity.setUpdatedBy(loginUserName != null ? loginUserName : "");
		flowEntity.setUpdatedTime(new Date());
		flowEntity.setStatus(flowDto.getStatus());
		flowEntity.setEtlEntryGroupId(flowDto.getEtlEntryGroupId());
		flowEntity.setParentEtlGroupId(flowDto.getParentEtlGroupId());

		long l = 0;
		String id = batchUidService.getUid(qTEtlFlow.getTableName()) + "";
		TEtlFlow flow = sqlQueryFactory.select(qTEtlFlow).from(qTEtlFlow).where(qTEtlFlow.flowName.eq(flowDto.getFlowName())).fetchFirst();
		if(flow != null){
			id = flow.getId();
			flowEntity.setId(id);
			l = this.put(id,flowEntity);
		}else{
			flowEntity.setId(id);
			flowEntity.setCreatedTime(new Date());
			l = this.post(flowEntity);
		}

		if(l <= 0){
			throw new RuntimeException("????????????????????????");
		}
	}


	@ApiOperation(value = "??????????????????")
	@PostMapping(path = "/updateEtlFlow/{id}")
	public ResultDto<String> editEtlFlow(@RequestBody EtlFlowDto flowDto, @RequestParam("loginUserName") String loginUserName) {

		// ?????????????????????????????????
		if (StringUtils.isBlank(loginUserName)) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "???????????????????????????!", "???????????????????????????!");
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
			etlGroupService.editEtlGroup(groupDto,loginUserName);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "?????????????????????????????????????????????", "");
		}

		SQLUpdateClause updateClause = sqlQueryFactory.update(qTEtlFlow);

		// ????????????
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
			if (flowDto.getMaxDuration() != null) {
				updateClause.set(qTEtlFlow.maxDuration, flowDto.getMaxDuration());
			}
			if (flowDto.getAlertDuration() != null) {
				updateClause.set(qTEtlFlow.alertDuration, flowDto.getAlertDuration());
			}
			if (StringUtils.isNotBlank(flowDto.getParentEtlGroupId())) {
				updateClause.set(qTEtlFlow.parentEtlGroupId, flowDto.getParentEtlGroupId());
			}
			
			if (StringUtils.isNotBlank(flowDto.getEtlJobcontrolId())) {
				updateClause.set(qTEtlFlow.etlJobcontrolId, flowDto.getEtlJobcontrolId());
			}
			if (StringUtils.isNotBlank(loginUserName))
				updateClause.set(qTEtlFlow.updatedBy, loginUserName != null ? loginUserName : "");
			updateClause.set(qTEtlFlow.updatedTime, new Date());
			updateClause.where(qTEtlFlow.id.eq(flowDto.getId()));
			long insertCount = updateClause.execute();
			return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "????????????????????????", insertCount + "");
		} catch (Exception e) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "????????????????????????", e.getLocalizedMessage());
		}
	}
	
	@ApiOperation(value = "??????????????????")
	@GetMapping(path = "/updateEtlFlowStatus/{id}")
	public ResultDto<String> updateEtlFlowStatus(@PathVariable String id, @RequestParam String status, @RequestParam("loginUserName") String loginUserName) {

		// ?????????????????????????????????
		if (StringUtils.isBlank(loginUserName)) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "???????????????????????????!", "???????????????????????????!");
		}

		SQLUpdateClause updateClause = sqlQueryFactory.update(qTEtlFlow);

		// ????????????
		try {
			if("4".equals(status)) {
				status = "0";
				updateClause.set(qTEtlFlow.lastDebugTime, new Date());
			}
			if (StringUtils.isNotBlank(status)) {
				updateClause.set(qTEtlFlow.status, status);
			}
			if (StringUtils.isNotBlank(loginUserName))
			updateClause.set(qTEtlFlow.updatedBy, loginUserName != null ? loginUserName : "");
			updateClause.set(qTEtlFlow.updatedTime, new Date());
			if("3".equals(status)) {
				updateClause.set(qTEtlFlow.lastDebugTime, new Date());
			}
			updateClause.where(qTEtlFlow.id.eq(id));
			long insertCount = updateClause.execute();
			return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "????????????????????????", insertCount + "");
		} catch (Exception e) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "????????????????????????", e.getLocalizedMessage());
		}
	}

	@ApiOperation(value = "????????????")
	@PostMapping(path = "/delEtlFlow/{id}")
	@Transactional(rollbackFor = Exception.class)
	public ResultDto<String> delEtlFlow(@PathVariable String id) {
		long result = 0;
		TEtlFlow etlFlow = this.getOne(id);
		if(etlFlow != null) {
			try {
				String etlGroupId = etlFlow.getEtlGroupId();
				sqlQueryFactory.update(qTEtlFlow).set(qTEtlFlow.parentEtlGroupId, "0").where(qTEtlFlow.parentEtlGroupId.eq(etlGroupId)).execute();
				etlGroupService.delEtlGroup(etlFlow.getGroupId() , etlFlow.getParentGroupId());
				result = this.delete(id);
			} catch (Exception e) {
				throw new RuntimeException("??????ETL???????????????????????????????????????" + e.getLocalizedMessage());
			}
		}
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "??????????????????", result + "");
	}
	
	
	@ApiOperation(value = "????????????")
	@PostMapping(path = "/stopEtlFlow/{id}")
	@Transactional(rollbackFor = Exception.class)
	public ResultDto<String> stopEtlFlow(@PathVariable String id) {
		try {
			etlGroupService.stopEtlGroup(id);
		} catch (Exception e) {
			throw new RuntimeException("??????ETL???????????????????????????????????????" + e.getLocalizedMessage());
		}
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "??????????????????", "success");
	}

	/**
	 * ????????????id????????????
	 * @param platformId
	 * @return
	 */
	public List<TEtlFlow> getTEtlFlowIds(String platformId){
		List<TEtlFlow> etlFlowIds = sqlQueryFactory.select(Projections.bean(TEtlFlow.class, qTEtlFlow.id , qTEtlFlow.parentGroupId , qTEtlFlow.etlGroupId)).from(qTEtlFlow)
				.leftJoin(qTEtlGroup).on(qTEtlFlow.groupId.eq(qTEtlGroup.id))
				.where(qTEtlGroup.platformId.eq(platformId)).fetch();
		return etlFlowIds;
	}
	
	@ApiOperation(value = "??????????????????")
	@PostMapping(path = "/emptyEtlFlow/{id}")
	@Transactional(rollbackFor = Exception.class)
	public ResultDto<String> emptyEtlFlow(@PathVariable String id) {
		TEtlFlow flow = this.getOne(id);
		if(flow == null) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "??????????????????????????????????????????", "??????????????????????????????????????????");
		}
		TPlatform platform = sqlQueryFactory.select(qTPlatform).from(qTEtlFlow).join(qTEtlGroup)
				.on(qTEtlFlow.groupId.eq(qTEtlGroup.id)).join(qTPlatform).on(qTPlatform.id.eq(qTEtlGroup.platformId))
				.where(qTEtlFlow.id.eq(id)).fetchFirst();
		if(platform == null) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "????????????????????????????????????????????????", "????????????????????????????????????????????????");
		}
		Map<String , Object> params = new HashMap<String , Object>();
		params.put("tEtlGroupId", flow.getEtlGroupId());
		params.put("etlServerUrl", platform.getEtlServerUrl());
		params.put("etlUser", platform.getEtlUser());
		params.put("etlPwd", platform.getEtlPwd());
		return etlGroupService.emptyEtlGroupQueues(params);
		
	}


	@ApiOperation(value = "??????????????????")
	@PostMapping(path = "/batchSaveEtlFlow")
	@Transactional(rollbackFor = Exception.class)
	public ResultDto<String> batchSaveEtlFlow(@RequestBody BatchEtlFlowDto befDto, @RequestParam("loginUserName") String loginUserName) {

		// ?????????????????????????????????
		if (StringUtils.isBlank(loginUserName)) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "???????????????????????????!", "???????????????????????????!");
		}
		if(befDto != null && befDto.getList() != null && befDto.getList().size() > 0){
			befDto.getList().forEach(efDto -> {
				this.doSaveEtlFLow2(efDto, loginUserName);
			});
		}

		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "????????????????????????", "???????????????");
	}
}
