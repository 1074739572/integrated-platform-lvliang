package com.iflytek.integrated.platform.service;

import static com.iflytek.integrated.platform.entity.QTEtlGroup.qTEtlGroup;
import static com.iflytek.integrated.platform.entity.QTEtlLog.qTEtlLog;
import static com.iflytek.integrated.platform.entity.QTHospital.qTHospital;
import static com.iflytek.integrated.platform.entity.QTPlatform.qTPlatform;
import static com.iflytek.integrated.platform.entity.QTProject.qTProject;
import static com.iflytek.integrated.platform.entity.QTSys.qTSys;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.common.dto.TableData;
import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.platform.entity.TEtlLog;
import com.iflytek.integrated.platform.utils.PlatformUtil;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.StringPath;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lsn
 */
@Slf4j
@Api(tags = "etl日志服务")
@RestController
@RequestMapping("/{version}/pt/etllog")
public class EtlLogService extends BaseService<TEtlLog, String, StringPath> {

	private static final Logger logger = LoggerFactory.getLogger(EtlLogService.class);

	public EtlLogService() {
		super(qTEtlLog, qTEtlLog.id);
	}

	@ApiOperation(value = "获取日志列表")
	@PostMapping("/getEtlLogs")
	public ResultDto<TableData<TEtlLog>> getEtlFlows(@RequestBody TEtlLog queryCondition,
			@ApiParam(value = "页码", example = "1") @RequestParam(value = "pageNo", defaultValue = "1", required = false) Integer pageNo,
			@ApiParam(value = "每页大小", example = "10") @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
		// 查询条件
		ArrayList<Predicate> list = new ArrayList<>();
		if (StringUtils.isNotBlank(queryCondition.getProjectName())) {
			list.add(qTProject.projectName.like("%" + queryCondition.getProjectName() + "%"));
		}
		if (StringUtils.isNotBlank(queryCondition.getPlatformName())) {
			list.add(qTPlatform.platformName.like("%" + queryCondition.getPlatformName() + "%"));
		}
		if (StringUtils.isNotBlank(queryCondition.getSysName())) {
			list.add(qTSys.sysName.like("%" + queryCondition.getSysName() + "%"));
		}
		if (StringUtils.isNotBlank(queryCondition.getHospitalName())) {
			list.add(qTHospital.hospitalName.like("%" + queryCondition.getHospitalName() + "%"));
		}
		if(StringUtils.isNotBlank(queryCondition.getStatus())){
			list.add(qTEtlLog.status.eq(queryCondition.getStatus()));
		}
		if (StringUtils.isNotBlank(queryCondition.getFlowName())) {
			list.add(qTEtlLog.flowName.like("%" + queryCondition.getFlowName() + "%"));
		}
		QueryResults<TEtlLog> queryResults = sqlQueryFactory.select(Projections.bean(TEtlLog.class, qTEtlLog.id,
				qTEtlLog.etlGroupId, qTEtlLog.flowName, qTEtlLog.createdTime, qTEtlLog.jobTime,qTEtlLog.status,qTEtlLog.errorInfo,
				qTProject.projectName.as("projectName"), qTPlatform.platformName.as("platformName"), qTHospital.hospitalName.as("hospitalName"),
				qTSys.sysName.as("sysName"))).from(qTEtlLog).leftJoin(qTEtlGroup)
				.on(qTEtlLog.etlGroupId.eq(qTEtlGroup.etlGroupId))
				.leftJoin(qTProject).on(qTProject.id.eq(qTEtlGroup.projectId))
				.leftJoin(qTPlatform).on(qTPlatform.id.eq(qTEtlGroup.platformId))
				.leftJoin(qTSys).on(qTSys.id.eq(qTEtlGroup.sysId))
				.leftJoin(qTHospital).on(qTHospital.id.eq(qTEtlGroup.hospitalId))
				.where(list.toArray(new Predicate[list.size()])).limit(pageSize).offset((pageNo - 1) * pageSize)
				.orderBy(qTEtlGroup.createdTime.desc()).fetchResults();

		if(queryResults.getResults() != null) {
			List<TEtlLog> logs = queryResults.getResults();
			for(TEtlLog etllog : logs) {
				long endtime = etllog.getCreatedTime().getTime();
				long starttime = etllog.getJobTime().getTime();
				long execTimeSeconds = (endtime - starttime)/1000;
				etllog.setExecTime(PlatformUtil.secondsToFormat(execTimeSeconds));
				etllog.setStatus("1".equals(etllog.getStatus()) ? "成功" : "失败");
			}
		}
		// 分页
		TableData<TEtlLog> tableData = new TableData<>(queryResults.getTotal(), queryResults.getResults());
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取日志列表成功", tableData);
	}

	@ApiOperation(value = "获取日志详情")
	@GetMapping("/getEtlLogs/{id}")
	public ResultDto<TEtlLog> getEtlLogDetails(@PathVariable("id") String id) {
		TEtlLog logDetail = sqlQueryFactory.select(Projections.bean(TEtlLog.class, 
				qTProject.projectName.as("projectName"), qTPlatform.platformName.as("platformName"), qTHospital.hospitalName.as("hospitalName"),
				qTSys.sysName.as("sysName"))).from(qTEtlLog).leftJoin(qTEtlGroup)
				.on(qTEtlLog.etlGroupId.eq(qTEtlGroup.etlGroupId))
				.leftJoin(qTProject).on(qTProject.id.eq(qTEtlGroup.projectId))
				.leftJoin(qTPlatform).on(qTPlatform.id.eq(qTEtlGroup.platformId))
				.leftJoin(qTSys).on(qTSys.id.eq(qTEtlGroup.sysId))
				.leftJoin(qTHospital).on(qTHospital.id.eq(qTEtlGroup.hospitalId))
				.where(qTEtlLog.id.eq(id)).fetchFirst();
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取日志详情成功", logDetail);
	}
}
