package com.iflytek.integrated.platform.service;

import static com.iflytek.integrated.platform.entity.QTEtlGroup.qTEtlGroup;
import static com.iflytek.integrated.platform.entity.QTEtlLog.qTEtlLog;
import static com.iflytek.integrated.platform.entity.QTHospital.qTHospital;
import static com.iflytek.integrated.platform.entity.QTPlatform.qTPlatform;
import static com.iflytek.integrated.platform.entity.QTProject.qTProject;
import static com.iflytek.integrated.platform.entity.QTSys.qTSys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
import com.querydsl.core.types.dsl.Expressions;
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
	@GetMapping("/getEtlLogs")
	public ResultDto<TableData<TEtlLog>> getEtlFlows(String projectId, String platformId, String sysId, String status,
													 @ApiParam(value = "流程名称") @RequestParam(value = "flowName", required = false) String flowName,
													 @ApiParam(value = "nifi报错信息") @RequestParam(value = "errorInfo", required = false) String errorInfo,
													 @ApiParam(value = "页码", example = "1") @RequestParam(value = "pageNo", defaultValue = "1", required = false) Integer pageNo,
													 @ApiParam(value = "每页大小", example = "10") @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
		// 查询条件
		ArrayList<Predicate> list = new ArrayList<>();
		if (StringUtils.isNotBlank(projectId)) {
			list.add(qTProject.id.eq(projectId));
		}
		if (StringUtils.isNotBlank(platformId)) {
			list.add(qTPlatform.id.eq(platformId));
		}
		if (StringUtils.isNotBlank(sysId)) {
			list.add(qTSys.id.eq(sysId));
		}
		if(StringUtils.isNotBlank(status)){
			list.add(qTEtlLog.status.eq(Integer.valueOf(status)));
		}
		if (StringUtils.isNotBlank(flowName)) {
			list.add(qTEtlLog.flowName.like("%" + flowName + "%"));
		}
		if (StringUtils.isNotBlank(errorInfo)) {
			list.add(qTEtlLog.errorInfo.like("%" + errorInfo + "%"));
		}

		QueryResults<TEtlLog> qresults = sqlQueryFactory.select(Projections.bean(TEtlLog.class,
				qTEtlLog.id.max().as("id"),qTEtlLog.etlGroupId, qTEtlLog.exeJobId, qTEtlLog.flowName,
				qTEtlLog.createdTime.max().as("createdTime"), qTEtlLog.jobTime,qTEtlLog.status.sum().as("statusCode"),
				qTEtlLog.batchReadCount.max().as("allReadCount"), qTEtlLog.batchWriteErrorcount.sum().as("allWriteErrorcount"),
				Expressions.stringTemplate("group_concat(from_base64({0}))" , qTEtlLog.errorInfo).concat("|").as("errorInfo") ,
				qTProject.projectName.as("projectName"), qTPlatform.platformName.as("platformName"), qTHospital.hospitalName.as("hospitalName"),
				qTSys.sysName.as("sysName")))
				.from(qTEtlLog)
				.leftJoin(qTEtlGroup)
				.on(qTEtlLog.etlGroupId.eq(qTEtlGroup.etlGroupId))
				.leftJoin(qTProject).on(qTProject.id.eq(qTEtlGroup.projectId))
				.leftJoin(qTPlatform).on(qTPlatform.id.eq(qTEtlGroup.platformId))
				.leftJoin(qTSys).on(qTSys.id.eq(qTEtlGroup.sysId))
				.leftJoin(qTHospital).on(qTHospital.id.eq(qTEtlGroup.hospitalId))
				.groupBy(qTEtlLog.etlGroupId , qTEtlLog.exeJobId)
				.where(list.toArray(new Predicate[list.size()])).limit(pageSize).offset((pageNo - 1) * pageSize)
				.orderBy(qTEtlLog.jobTime.desc()).fetchResults();
		List<TEtlLog> results = null;
		if(qresults != null) {
			results = qresults.getResults();
			for(TEtlLog etllog : results) {
				long endtime = etllog.getCreatedTime().getTime();
				long starttime = etllog.getJobTime().getTime();
				long execTimeSeconds = (endtime - starttime)/1000;
				etllog.setExecTime(PlatformUtil.secondsToFormat(execTimeSeconds));
				etllog.setStatus(etllog.getStatusCode() == 1 ? "成功" : "失败");
			}
		}
		// 分页
		TableData<TEtlLog> tableData = new TableData<>(qresults.getTotal(), results);
//		TableData<TEtlLog> tableData = new TableData<>(size, results);
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取日志列表成功", tableData);
	}

	@ApiOperation(value = "获取日志详情")
	@GetMapping("/getEtlLogs/{id}")
	public ResultDto<TEtlLog> getEtlLogDetails(@PathVariable("id") String id) {
		TEtlLog logDetail = sqlQueryFactory.select(Projections.bean(TEtlLog.class, qTEtlLog.etlGroupId , qTEtlLog.exeJobId,
				qTProject.projectName.as("projectName"), qTPlatform.platformName.as("platformName"), qTHospital.hospitalName.as("hospitalName"),
				qTSys.sysName.as("sysName") , Expressions.stringTemplate("from_base64({0})" , qTEtlLog.errorInfo).as("errorInfo") )).from(qTEtlLog).leftJoin(qTEtlGroup)
				.on(qTEtlLog.etlGroupId.eq(qTEtlGroup.etlGroupId))
				.leftJoin(qTProject).on(qTProject.id.eq(qTEtlGroup.projectId))
				.leftJoin(qTPlatform).on(qTPlatform.id.eq(qTEtlGroup.platformId))
				.leftJoin(qTSys).on(qTSys.id.eq(qTEtlGroup.sysId))
				.leftJoin(qTHospital).on(qTHospital.id.eq(qTEtlGroup.hospitalId))
				.where(qTEtlLog.id.eq(id)).fetchFirst();
		
		String etlGroupId = logDetail.getEtlGroupId();
		String exeJobId = logDetail.getExeJobId();
		List<TEtlLog> batchErrorLogs = sqlQueryFactory.select(Projections.bean(TEtlLog.class, 
				qTEtlLog.id,qTEtlLog.etlGroupId, qTEtlLog.exeJobId, qTEtlLog.flowName, 
				qTEtlLog.createdTime, qTEtlLog.jobTime,qTEtlLog.status.as("statusCode"),
				qTEtlLog.batchReadCount, qTEtlLog.batchWriteErrorcount,qTEtlLog.exeBatchNo,
				Expressions.stringTemplate("from_base64({0})" , qTEtlLog.errorInfo).as("errorInfo"))).from(qTEtlLog)
				.where(qTEtlLog.etlGroupId.eq(etlGroupId).and(qTEtlLog.exeJobId.eq(exeJobId)).and(qTEtlLog.status.eq(2))).fetch();
		logDetail.setBatchErrorLogs(batchErrorLogs);
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取日志详情成功", logDetail);
	}
}
