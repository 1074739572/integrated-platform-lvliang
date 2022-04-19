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

import com.iflytek.integrated.platform.dto.EtlLogInfoDto;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.common.dto.TableData;
import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.platform.entity.QTEtlLog;
import com.iflytek.integrated.platform.entity.QTInterfaceMonitor;
import com.iflytek.integrated.platform.entity.TEtlLog;
import com.iflytek.integrated.platform.utils.PlatformUtil;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.core.types.dsl.StringTemplate;
import com.querydsl.sql.SQLExpressions;

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
public class EtlLogService extends BaseService<TEtlLog, Long, NumberPath<Long>> {

	private static final Logger logger = LoggerFactory.getLogger(EtlLogService.class);

	public EtlLogService() {
		super(qTEtlLog, qTEtlLog.id);
	}
	
	@Value("${server.db}")
	private String dbType;

	@ApiOperation(value = "获取日志列表")
	@GetMapping("/getEtlLogs")
	public ResultDto<TableData<TEtlLog>> getEtlFlows(String projectId, String platformId, String sysId, String status,
													 @ApiParam(value = "流程名称") @RequestParam(value = "flowName", required = false) String flowName,
													 @ApiParam(value = "nifi报错信息") @RequestParam(value = "errorInfo", required = false) String errorInfo,
													 @ApiParam(value = "页码", example = "1") @RequestParam(value = "pageNo", defaultValue = "1", required = false) Integer pageNo,
													 @ApiParam(value = "每页大小", example = "10") @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
		if(StringUtils.isNotBlank(projectId) || StringUtils.isNotBlank(platformId) || StringUtils.isNotBlank(sysId)
				|| StringUtils.isNotBlank(status) || StringUtils.isNotBlank(flowName) || StringUtils.isNotBlank(errorInfo)){
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
			return getEtlFlowsByFilter(list,pageNo,pageSize);
		}else{
			return getEtlFlowsByPage(pageNo,pageSize);
		}
	}

	@ApiOperation(value = "获取日志列表")
	@GetMapping("/getEtlLogs2")
	public ResultDto<TableData<TEtlLog>> getEtlFlows2(@ApiParam(value = "页码", example = "1") @RequestParam(value = "pageNo", defaultValue = "1", required = false) Integer pageNo,
													 @ApiParam(value = "每页大小", example = "10") @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {

		return getEtlFlowsByPage(pageNo,pageSize);

	}


	public ResultDto getEtlFlowsByFilter(ArrayList<Predicate> list,Integer pageNo,Integer pageSize){
		QueryResults<TEtlLog> qresults = null;
		if("postgresql".equals(dbType)) {
			StringTemplate st = Expressions.stringTemplate("concat(CONVERT_FROM(decode({0} , 'base64'),'UTF-8') , '|')" , qTEtlLog.errorInfo);
			StringTemplate stg = Expressions.stringTemplate("concat(string_agg ( errorinfo, ',' :: TEXT ))");

			StringTemplate qistg = Expressions.stringTemplate("concat(string_agg ( qi_result, ',' :: TEXT ))");

			String q = "etllog";
			StringPath queryLabel = Expressions.stringPath(q);
			QTEtlLog qtetllogalias = new QTEtlLog(q);
			SubQueryExpression query = SQLExpressions.select(qTEtlLog.etlGroupId.as("ETL_GROUP_ID") , qTEtlLog.exeJobId.as("EXE_JOB_ID") , st.as("errorinfo"))
					.from(qTEtlLog).orderBy(qTEtlLog.jobTime.desc());
			qresults = sqlQueryFactory.select(Projections.bean(TEtlLog.class,
					qTEtlLog.id.max().as("id"),qTEtlLog.etlGroupId, qTEtlLog.exeJobId, qTEtlLog.flowName.max().as("flowName"),
					qTEtlLog.createdTime.max().as("createdTime"),
					qTEtlLog.jobTime.max().as("jobTime"),
					qTEtlLog.status.sum().as("statusCode"),
					qTEtlLog.batchReadCount.max().as("allReadCount"),
					qTEtlLog.batchWriteErrorcount.sum().as("allWriteErrorcount"),
					stg.as("errorInfo"),qistg.as("QIResult"),qTProject.projectName.max().as("projectName"), qTPlatform.platformName.max().as("platformName"), qTHospital.hospitalName.max().as("hospitalName"),
					qTSys.sysName.max().as("sysName")))
					.from(query ,queryLabel).leftJoin(qTEtlLog)
					.on(qtetllogalias.etlGroupId.eq(qTEtlLog.etlGroupId).and(qtetllogalias.exeJobId.eq(qTEtlLog.exeJobId)))
					.leftJoin(qTEtlGroup).on(qTEtlLog.etlGroupId.eq(qTEtlGroup.etlGroupId))
					.leftJoin(qTProject).on(qTProject.id.eq(qTEtlGroup.projectId))
					.leftJoin(qTPlatform).on(qTPlatform.id.eq(qTEtlGroup.platformId))
					.leftJoin(qTSys).on(qTSys.id.eq(qTEtlGroup.sysId))
					.leftJoin(qTHospital).on(qTHospital.id.eq(qTEtlGroup.hospitalId))
					.groupBy(qTEtlLog.etlGroupId , qTEtlLog.exeJobId)
					.where(list.toArray(new Predicate[list.size()])).limit(pageSize).offset((pageNo - 1) * pageSize)
					.fetchResults();
		}else {
			qresults = sqlQueryFactory.select(Projections.bean(TEtlLog.class,
					qTEtlLog.id.max().as("id"),qTEtlLog.etlGroupId, qTEtlLog.exeJobId, qTEtlLog.flowName,
					qTEtlLog.createdTime.max().as("createdTime"),
					qTEtlLog.jobTime.max().as("jobTime"),
					qTEtlLog.status.sum().as("statusCode"),
					qTEtlLog.batchReadCount.max().as("allReadCount"),
					qTEtlLog.batchWriteErrorcount.sum().as("allWriteErrorcount"),
					Expressions.stringTemplate("group_concat(from_base64({0}))" , qTEtlLog.errorInfo).concat("|").as("errorInfo") ,
					Expressions.stringTemplate("group_concat({0},'|')" , qTEtlLog.QIResult).as("QIResult") ,
					qTProject.projectName.as("projectName"), qTPlatform.platformName.as("platformName"), qTHospital.hospitalName.as("hospitalName"),
					qTSys.sysName.as("sysName")))
					.from(qTEtlLog)
					.leftJoin(qTEtlGroup).on(qTEtlLog.etlGroupId.eq(qTEtlGroup.etlGroupId))
					.leftJoin(qTProject).on(qTProject.id.eq(qTEtlGroup.projectId))
					.leftJoin(qTPlatform).on(qTPlatform.id.eq(qTEtlGroup.platformId))
					.leftJoin(qTSys).on(qTSys.id.eq(qTEtlGroup.sysId))
					.leftJoin(qTHospital).on(qTHospital.id.eq(qTEtlGroup.hospitalId))
					.groupBy(qTEtlLog.etlGroupId , qTEtlLog.exeJobId)
					.where(list.toArray(new Predicate[list.size()])).limit(pageSize).offset((pageNo - 1) * pageSize)
					.orderBy(qTEtlLog.jobTime.desc()).fetchResults();
		}
		
		List<TEtlLog> results = null;
		if(qresults != null) {
			results = qresults.getResults();
			for(TEtlLog etllog : results) {
				long endtime = etllog.getCreatedTime().getTime();
				long starttime = etllog.getJobTime().getTime();
				long execTimeSeconds = (endtime - starttime)/1000;
				etllog.setExecTime(PlatformUtil.secondsToFormat(execTimeSeconds));
				etllog.setStatus(etllog.getStatusCode() == 1 ? "成功" : "失败");

				String QIResult = decryptAndFilterSensitive((etllog.getQIResult()));
				if (org.apache.commons.lang.StringUtils.isNotBlank(QIResult)) {
					QIResult = QIResult.length() > 5000 ? QIResult.substring(0, 5000) + "......" : QIResult;
				}
				etllog.setQIResult(QIResult);
			}
		}
		// 分页
		TableData<TEtlLog> tableData = new TableData<>(qresults.getTotal(), results);
//		TableData<TEtlLog> tableData = new TableData<>(size, results);
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取日志列表成功", tableData);
	}


	public ResultDto getEtlFlowsByPage(Integer pageNo,Integer pageSize){
		QueryResults<TEtlLog> qresults = null;
		if("postgresql".equals(dbType)) {
			StringTemplate st = Expressions.stringTemplate("concat(CONVERT_FROM(decode({0} , 'base64'),'UTF-8') , '|')" , qTEtlLog.errorInfo);
			StringTemplate stg = Expressions.stringTemplate("concat(string_agg ( errorinfo, ',' :: TEXT ))");

			StringTemplate qistg = Expressions.stringTemplate("concat(string_agg ( qi_result, ',' :: TEXT ))");

			String q = "etllog";
			StringPath queryLabel = Expressions.stringPath(q);
			QTEtlLog qtetllogalias = new QTEtlLog(q);
			SubQueryExpression query = SQLExpressions.select(qTEtlLog.etlGroupId.as("ETL_GROUP_ID") , qTEtlLog.exeJobId.as("EXE_JOB_ID") , st.as("errorinfo"))
					.from(qTEtlLog).orderBy(qTEtlLog.jobTime.desc());
			qresults = sqlQueryFactory.select(Projections.bean(TEtlLog.class,
					qTEtlLog.id.max().as("id"),qTEtlLog.etlGroupId, qTEtlLog.exeJobId, qTEtlLog.flowName.max().as("flowName"),
					qTEtlLog.createdTime.max().as("createdTime"),
					qTEtlLog.jobTime.max().as("jobTime"),
					qTEtlLog.status.sum().as("statusCode"),
					qTEtlLog.batchReadCount.max().as("allReadCount"),
					qTEtlLog.batchWriteErrorcount.sum().as("allWriteErrorcount"),
					stg.as("errorInfo"),qistg.as("QIResult")))
					.from(query ,queryLabel).leftJoin(qTEtlLog)
					.on(qtetllogalias.etlGroupId.eq(qTEtlLog.etlGroupId).and(qtetllogalias.exeJobId.eq(qTEtlLog.exeJobId)))
					.groupBy(qTEtlLog.etlGroupId , qTEtlLog.exeJobId)
					.limit(pageSize).offset((pageNo - 1) * pageSize).fetchResults();
		}else {
			qresults = sqlQueryFactory.select(Projections.bean(TEtlLog.class,
					qTEtlLog.id.max().as("id"),qTEtlLog.etlGroupId, qTEtlLog.exeJobId, qTEtlLog.flowName,
					qTEtlLog.createdTime.max().as("createdTime"),
					qTEtlLog.jobTime.max().as("jobTime"),
					qTEtlLog.status.sum().as("statusCode"),
					qTEtlLog.batchReadCount.max().as("allReadCount"),
					qTEtlLog.batchWriteErrorcount.sum().as("allWriteErrorcount"),
					Expressions.stringTemplate("group_concat(from_base64({0}))" , qTEtlLog.errorInfo).concat("|").as("errorInfo"),
					Expressions.stringTemplate("group_concat({0},'|')" , qTEtlLog.QIResult).as("QIResult")))
					.from(qTEtlLog)
					.groupBy(qTEtlLog.etlGroupId , qTEtlLog.exeJobId)
					.limit(pageSize).offset((pageNo - 1) * pageSize)
					.orderBy(qTEtlLog.jobTime.max().desc()).fetchResults();
		}
		
		List<TEtlLog> results = null;
		if(qresults != null) {
			results = qresults.getResults();
			for(TEtlLog etllog : results) {
				long endtime = etllog.getCreatedTime().getTime();
				long starttime = etllog.getJobTime().getTime();
				long execTimeSeconds = (endtime - starttime)/1000;
				etllog.setExecTime(PlatformUtil.secondsToFormat(execTimeSeconds));
				etllog.setStatus(etllog.getStatusCode() == 1 ? "成功" : "失败");
				String QIResult = decryptAndFilterSensitive((etllog.getQIResult()));
				if (org.apache.commons.lang.StringUtils.isNotBlank(QIResult)) {
					QIResult = QIResult.length() > 5000 ? QIResult.substring(0, 5000) + "......" : QIResult;
				}
				etllog.setQIResult(QIResult);
				//
				EtlLogInfoDto info = sqlQueryFactory
						.select(Projections.bean(EtlLogInfoDto.class,
								qTProject.projectName.as("projectName"),
								qTPlatform.platformName.as("platformName"),
								qTHospital.hospitalName.as("hospitalName"),
								qTSys.sysName.as("sysName")))
						.from(qTEtlGroup)
						.leftJoin(qTProject).on(qTProject.id.eq(qTEtlGroup.projectId))
						.leftJoin(qTPlatform).on(qTPlatform.id.eq(qTEtlGroup.platformId))
						.leftJoin(qTSys).on(qTSys.id.eq(qTEtlGroup.sysId))
						.leftJoin(qTHospital).on(qTHospital.id.eq(qTEtlGroup.hospitalId))
						.where(qTEtlGroup.etlGroupId.eq(etllog.getEtlGroupId()))
						.fetchOne();
				if(info != null){
					etllog.setProjectName(info.getProjectName());
					etllog.setPlatformName(info.getPlatformName());
					etllog.setHospitalName(info.getHospitalName());
					etllog.setSysName(info.getSysName());
				}
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
		StringTemplate st = Expressions.stringTemplate("from_base64({0})" , qTEtlLog.errorInfo);
		StringTemplate qist = Expressions.stringTemplate("from_base64({0})" , qTEtlLog.QIResult);
		if("postgresql".equals(dbType)) {
			st = Expressions.stringTemplate("CONVERT_FROM(decode({0},'base64'),'UTF-8')" , qTEtlLog.errorInfo);
			qist = Expressions.stringTemplate("CONVERT_FROM(decode({0},'base64'),'UTF-8')" , qTEtlLog.QIResult);

		}
		TEtlLog logDetail = sqlQueryFactory.select(Projections.bean(TEtlLog.class, qTEtlLog.etlGroupId , qTEtlLog.exeJobId,
				qTProject.projectName.as("projectName"), qTPlatform.platformName.as("platformName"), qTHospital.hospitalName.as("hospitalName"),
				qTSys.sysName.as("sysName") , st.as("errorInfo"),qist.as("QIResult") )).from(qTEtlLog).leftJoin(qTEtlGroup)
				.on(qTEtlLog.etlGroupId.eq(qTEtlGroup.etlGroupId))
				.leftJoin(qTProject).on(qTProject.id.eq(qTEtlGroup.projectId))
				.leftJoin(qTPlatform).on(qTPlatform.id.eq(qTEtlGroup.platformId))
				.leftJoin(qTSys).on(qTSys.id.eq(qTEtlGroup.sysId))
				.leftJoin(qTHospital).on(qTHospital.id.eq(qTEtlGroup.hospitalId))
				.where(qTEtlLog.id.eq(Long.valueOf(id))).fetchFirst();
		
		String etlGroupId = logDetail.getEtlGroupId();
		String exeJobId = logDetail.getExeJobId();
		List<TEtlLog> batchErrorLogs = sqlQueryFactory.select(Projections.bean(TEtlLog.class, 
				qTEtlLog.id,qTEtlLog.etlGroupId, qTEtlLog.exeJobId, qTEtlLog.flowName, 
				qTEtlLog.createdTime, qTEtlLog.jobTime,qTEtlLog.status.as("statusCode"),
				qTEtlLog.batchReadCount, qTEtlLog.batchWriteErrorcount,qTEtlLog.exeBatchNo,
				st.as("errorInfo"))).from(qTEtlLog)
				.where(qTEtlLog.etlGroupId.eq(etlGroupId).and(qTEtlLog.exeJobId.eq(exeJobId)).and(qTEtlLog.status.eq(2))).fetch();
		logDetail.setBatchErrorLogs(batchErrorLogs);
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取日志详情成功", logDetail);
		
	}
}
