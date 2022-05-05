package com.iflytek.integrated.platform.service;

import static com.iflytek.integrated.platform.entity.QTBusinessInterface.qTBusinessInterface;
import static com.iflytek.integrated.platform.entity.QTDrive.qTDrive;
import static com.iflytek.integrated.platform.entity.QTEtlLog.qTEtlLog;
import static com.iflytek.integrated.platform.entity.QTInterface.qTInterface;
import static com.iflytek.integrated.platform.entity.QTInterfaceMonitor.qTInterfaceMonitor;
import static com.iflytek.integrated.platform.entity.QTLog.qTLog;
import static com.iflytek.integrated.platform.entity.QTPlatform.qTPlatform;
import static com.iflytek.integrated.platform.entity.QTProject.qTProject;
import static com.iflytek.integrated.platform.entity.QTSys.qTSys;
import static com.iflytek.integrated.platform.entity.QTSysConfig.qTSysConfig;

import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.querydsl.core.types.dsl.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.common.dto.TableData;
import com.iflytek.integrated.common.utils.ExceptionUtil;
import com.iflytek.integrated.common.utils.SensitiveUtils;
import com.iflytek.integrated.common.utils.ase.AesUtil;
import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.platform.dto.InterfaceMonitorDto;
import com.iflytek.integrated.platform.entity.QTInterfaceMonitor;
import com.iflytek.integrated.platform.entity.TBusinessInterface;
import com.iflytek.integrated.platform.entity.TLog;
import com.iflytek.integrated.platform.utils.NiFiRequestUtil;
import com.iflytek.integrated.platform.utils.PlatformUtil;
import com.querydsl.core.QueryFlag;
import com.querydsl.core.QueryFlag.Position;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQuery;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

/**
 * @author czzhan
 * @version 1.0
 * @date 2020/12/20 16:46
 */
@Slf4j
@Api(tags = "服务监控")
@RestController
@RequestMapping("/{version}/pt/interfaceMonitor")
public class LogService extends BaseService<TLog, Long, NumberPath<Long>> {
	private static final Logger logger = LoggerFactory.getLogger(LogService.class);

	@Autowired
	private NiFiRequestUtil niFiRequestUtil;
	
	@Value("${server.db}")
	private String dbType;

	@Value("${config.request.nifiapi.readtimeout}")
	private int readTimeout;
	
	public LogService() {
		super(qTLog, qTLog.id);
	}

	/**
	 * 按接口名称来展示服务监控列表
	 * @param projectId
	 * @param platformId
	 * @param sysId
	 * @param status
	 * @param interfaceName
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	@ApiOperation(value = "查看服务监控列表")
	@GetMapping("/getListPage")
	public ResultDto<TableData<InterfaceMonitorDto>> getListPage(String projectId, String platformId, String sysId, String status,
																 @ApiParam(value = "接口名称") @RequestParam(value = "interfaceName", required = false) String interfaceName,
																 @RequestParam(defaultValue = "1") Integer pageNo,
																 @RequestParam(defaultValue = "10") Integer pageSize) {
		try {
			// 查询条件
			ArrayList<Predicate> list = new ArrayList<>();
			// 判断条件是否为空
			if (StringUtils.isNotBlank(sysId)) {
				list.add(qTSys.id.eq(sysId));
			}
			// 先合并t_interface_monitor，再根据三合一结果进行查询
			String q = "queryMonitor";
			StringPath queryLabel = Expressions.stringPath(q);
			StringPath reqInterPath = Expressions.stringPath("REQUEST_INTERFACE_ID");
			QTInterfaceMonitor monitor = new QTInterfaceMonitor(q);
			
//			StringExpression intfId = new CaseBuilder().when(qTInterface.interfaceId.isNull()).then("0").otherwise(monitor.interfaceId).as("interfaceId");
			
			SubQueryExpression query = SQLExpressions
					.select(qTInterfaceMonitor.status.max().as("status"),
							qTInterfaceMonitor.successCount.sum().as("SUCCESS_COUNT"),
							qTInterfaceMonitor.errorCount.sum().as("ERROR_COUNT"), qTInterfaceMonitor.projectId.max().as("PROJECT_ID"),
							qTInterfaceMonitor.platformId, qTInterfaceMonitor.sysId, qTInterfaceMonitor.typeId.max().as("TYPE_ID"),
							qTInterfaceMonitor.createdTime.max().as("CREATED_TIME"), qTInterfaceMonitor.businessInterfaceId.max().as("BUSINESS_INTERFACE_ID"),
							qTBusinessInterface.requestInterfaceId, qTBusinessInterface.replayFlag.max().as("REPLAY_FLAG"))
					.from(qTInterfaceMonitor)
					.leftJoin(qTBusinessInterface).on(qTBusinessInterface.id.eq(qTInterfaceMonitor.businessInterfaceId))
					.leftJoin(qTSysConfig).on(qTSysConfig.id.eq(qTBusinessInterface.requestSysconfigId)
							.and(qTSysConfig.platformId.eq(qTInterfaceMonitor.platformId)))
//					.leftJoin(qTInterface).on(qTInterface.id.eq(qTBusinessInterface.requestInterfaceId))
					.where(qTInterfaceMonitor.projectId.eq("0").or(qTInterfaceMonitor.projectId.notEqualsIgnoreCase("0").and(qTBusinessInterface.requestInterfaceId.isNotNull())))
					.groupBy(qTInterfaceMonitor.platformId, qTInterfaceMonitor.sysId, qTBusinessInterface.requestInterfaceId)
					.orderBy(qTInterfaceMonitor.status.max().desc(), qTInterfaceMonitor.createdTime.max().desc());

			// 按条件筛选
			if (StringUtils.isNotBlank(projectId)) {
				list.add(monitor.projectId.eq(projectId));
			}
			if (StringUtils.isNotBlank(platformId)) {
				list.add(monitor.platformId.eq(platformId));
			}
			if (StringUtils.isNotBlank(status)) {
				list.add(monitor.status.eq(status));
			}
			if (StringUtils.isNotBlank(interfaceName)) {
				list.add(qTInterface.interfaceName.like(PlatformUtil.createFuzzyText(interfaceName)));
			}
			// 根据结果查询
			StringExpression projName = new CaseBuilder().when(qTProject.id.eq("0").or(qTProject.id.isNull())).then("未关联接口配置异常类项目").otherwise(qTProject.projectName).as("projectName");
			StringExpression platName = new CaseBuilder().when(qTPlatform.id.eq("0").or(qTPlatform.id.isNull())).then("未关联接口配置异常类分类").otherwise(qTPlatform.platformName).as("platformName");
			StringExpression sysName = new CaseBuilder().when(qTSys.id.eq("0").or(qTSys.id.isNull())).then("未关联接口配置异常类系统").otherwise(qTSys.sysName).as("sysName");
			StringExpression intfName = new CaseBuilder().when(qTInterface.id.eq("0").or(qTInterface.id.isNull())).then("未关联接口配置异常类接口").otherwise(qTInterface.interfaceName).as("interfaceName");
			StringExpression intfId = new CaseBuilder().when(qTInterface.id.isNull()).then("0").otherwise(qTInterface.id).as("interfaceId");
			QueryResults<InterfaceMonitorDto> queryResults = sqlQueryFactory
					.selectDistinct(Projections.bean(InterfaceMonitorDto.class, monitor.status, monitor.successCount,
							monitor.replayFlag, monitor.errorCount, intfName, intfId,
							projName, platName, sysName))
					.from(query, queryLabel)
					.leftJoin(qTProject).on(qTProject.id.eq(monitor.projectId))
					.leftJoin(qTPlatform).on(qTPlatform.id.eq(monitor.platformId))
					.leftJoin(qTSysConfig).on(qTSysConfig.platformId.eq(qTPlatform.id).and(qTSysConfig.sysConfigType.eq(1)))
					.leftJoin(qTSys).on(qTSys.id.eq(qTSysConfig.sysId))
					.leftJoin(qTInterface).on(qTInterface.sysId.eq(qTSys.id).and(qTInterface.id.eq(reqInterPath)))
					.where(list.toArray(new Predicate[list.size()])).limit(pageSize).offset((pageNo - 1) * pageSize).fetchResults();
			// 分页
			TableData<InterfaceMonitorDto> tableData = new TableData<>(queryResults.getTotal(),
					queryResults.getResults());
			return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "", tableData);
		} catch (Exception e) {
			logger.error("查看服务监控列表失败! MSG:{}", ExceptionUtil.dealException(e));
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "服务监控列表获取失败");
		}
	}

	@ApiOperation(value = "查看监控日志列表")
	@GetMapping("/logInfoList")
	public ResultDto<TableData<TLog>> logInfoList(String interfaceId, String status, String visitAddr,
												   @ApiParam(value = "开始时间") @RequestParam(value = "startTime", required = true) String startTime,
												   @ApiParam(value = "结束时间") @RequestParam(value = "endTime", required = true) String endTime,
												   @ApiParam(value = "请求方请求") @RequestParam(value = "businessReq", required = false) String businessReq,
												   @ApiParam(value = "请求方响应") @RequestParam(value = "businessRep", required = false) String businessRep,
												   @ApiParam(value = "被请求方请求") @RequestParam(value = "venderReq", required = false) String venderReq,
												   @ApiParam(value = "被请求方响应") @RequestParam(value = "venderRep", required = false) String venderRep,
												   @RequestParam(defaultValue = "1") Integer pageNo,
											   @RequestParam(defaultValue = "10") Integer pageSize) {
		
		if (StringUtils.isEmpty(interfaceId)) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "获取日志详细列表，id必传");
		}
		Map<String , Integer> biorderMap = new Hashtable<String , Integer>();
		Map<String , String> biNameMap = new Hashtable<>();
		// 查询条件
		ArrayList<Predicate> list = new ArrayList<>();
		if(!"0".equals(interfaceId)) {
			List<TBusinessInterface> bis = sqlQueryFactory.select(qTBusinessInterface).from(qTBusinessInterface).where(qTBusinessInterface.requestInterfaceId.eq(interfaceId)).fetch();
			bis.forEach(tbis->{
				biorderMap.put(tbis.getId(), tbis.getExcErrOrder());
				biNameMap.put(tbis.getId(), tbis.getBusinessInterfaceName());
			});
			list.add(qTLog.businessInterfaceId.in(biorderMap.keySet()));
		}else {
			list.add(qTLog.businessInterfaceId.eq("0"));
		}
		
		try{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			list.add(qTLog.createdTime.goe(sdf.parse(startTime)));
			list.add(qTLog.createdTime.loe(sdf.parse(endTime)));
		}catch (ParseException e){
			e.printStackTrace();
		}
		
		if (StringUtils.isNotBlank(status)) {
			list.add(qTLog.status.eq(status));
		}
		// 模糊查询接口地址
		if (StringUtils.isNotBlank(visitAddr)) {
			list.add(qTLog.visitAddr.like(PlatformUtil.createFuzzyText(visitAddr)));
		}
		String tplStr = "AES_DECRYPT(from_base64({0}),'w5xv7[Nmc0Z/3U^X')";
		if("postgresql".equals(dbType)) {
			tplStr = "CONVERT_FROM(decrypt(decode({0},'base64') ,'w5xv7[Nmc0Z/3U^X' ,'aes-ecb/pad:pkcs'),'UTF-8')";
		}
		if (StringUtils.isNotBlank(businessReq)) {
			list.add(Expressions.stringTemplate(tplStr, qTLog.businessReq)
					.like(PlatformUtil.createFuzzyText(businessReq)));
		}
		if (StringUtils.isNotBlank(businessRep)) {
			list.add(Expressions.stringTemplate(tplStr, qTLog.businessRep)
					.like(PlatformUtil.createFuzzyText(businessRep)));
		}
		if (StringUtils.isNotBlank(venderReq)) {
			list.add(Expressions.stringTemplate(tplStr, qTLog.venderReq)
					.like(PlatformUtil.createFuzzyText(venderReq)));
		}
		if (StringUtils.isNotBlank(venderRep)) {
			list.add(Expressions.stringTemplate(tplStr, qTLog.venderRep)
					.like(PlatformUtil.createFuzzyText(venderRep)));
		}
		SQLQuery<TLog> tlogQuery = sqlQueryFactory
		.select(Projections.bean(TLog.class, qTLog.id, qTLog.createdTime, qTLog.status, qTLog.venderRepTime,
				qTLog.businessRepTime, qTLog.visitAddr,qTLog.businessInterfaceId, qTLog.debugreplayFlag))
		.from(qTLog);
		if(!"postgresql".equals(dbType)) {
			tlogQuery = tlogQuery.addFlag(new QueryFlag(Position.BEFORE_FILTERS, Expressions.stringTemplate(" FORCE INDEX ( log_query_idx )")));
		}
		QueryResults<TLog> queryResults = tlogQuery
				.where(list.toArray(new Predicate[list.size()])).limit(pageSize).offset((pageNo - 1) * pageSize)
				.orderBy(qTLog.createdTime.desc()).fetchResults();
		
		long l = biorderMap.size();
		List<TLog> tlogList = queryResults.getResults();
		for(TLog log : tlogList){
			if("0".equals(interfaceId)) {
				log.setInterfaceOrder("1/1");
				continue;
			}
			Integer order = biorderMap.get(log.getBusinessInterfaceId());
			if(order == null) {
				order = 0;
			}
			String interfaceOrder = (order + 1)+"/"+ l;
			log.setInterfaceOrder(interfaceOrder);
			log.setBusinessInterfaceName(biNameMap.get(log.getBusinessInterfaceId()));
		}
		// 分页
		TableData<TLog> tableData = new TableData<>(queryResults.getTotal(), queryResults.getResults());
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "日志详细列表获取成功!", tableData);
	}

	@ApiOperation(value = "查看日志详细信息")
	@GetMapping("/logInfo")
	public ResultDto<TLog> logInfo(String id, String interfaceId) {
		if (StringUtils.isEmpty(id)) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "获取日志详细，id必传");
		}
		if (StringUtils.isEmpty(interfaceId)) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "获取被请求方接口条数，interfaceId必传");
		}

		// 查询详情
		TLog tLog = sqlQueryFactory.select(Projections.bean(TLog.class, qTLog.id, qTLog.createdTime, qTLog.status,
				qTLog.venderRepTime, qTLog.businessRepTime, qTLog.visitAddr, qTLog.businessReq, qTLog.venderReq,
				qTLog.businessRep, qTLog.venderRep,qTLog.debugreplayFlag,
				qTBusinessInterface.businessInterfaceName.as("businessInterfaceName"),
				qTBusinessInterface.excErrOrder.add(1).as("excErrOrder"), qTLog.QIResult, qTLog.ipAddress))
				.from(qTLog)
				.leftJoin(qTBusinessInterface).on(qTBusinessInterface.id.eq(qTLog.businessInterfaceId))
						.where(qTLog.id.eq(Long.valueOf(id))).fetchFirst();
		String interfaceOrder = "";
		if("0".equals(interfaceId)) {
			interfaceOrder = "1/1";
		}else {
			long l = sqlQueryFactory.select(qTBusinessInterface).from(qTBusinessInterface)
					.where(qTBusinessInterface.requestInterfaceId.eq(interfaceId)).fetchCount();
			interfaceOrder = tLog.getExcErrOrder()+"/"+ l;
		}
		tLog.setInterfaceOrder(interfaceOrder);

		// 解密，脱敏处理数据
		String businessRep = decryptAndFilterSensitive(tLog.getBusinessRep());
		if (StringUtils.isNotBlank(businessRep)) {
			businessRep = businessRep.length() > 5000 ? businessRep.substring(0, 5000) + "......" : businessRep;
		}
		tLog.setBusinessRep(businessRep);
		String venderRep = decryptAndFilterSensitive(tLog.getVenderRep());
		if (StringUtils.isNotBlank(venderRep)) {
			venderRep = venderRep.length() > 5000 ? venderRep.substring(0, 5000) + "......" : venderRep;
		}
		tLog.setVenderRep(venderRep);

		String QIResult = decryptAndFilterSensitive(tLog.getQIResult());
		if (StringUtils.isNotBlank(QIResult)) {
			QIResult = QIResult.length() > 5000 ? QIResult.substring(0, 5000) + "......" : QIResult;
		}
		tLog.setQIResult(QIResult);

		tLog.setBusinessReq(decryptAndFilterSensitive(tLog.getBusinessReq()));
		tLog.setVenderReq(decryptAndFilterSensitive(tLog.getVenderReq()));
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "日志详细获取成功!", tLog);
	}

	@ApiOperation(value = "获取接口请求状态")
	@GetMapping("/getInterfaceStatus")
	public String getInterfaceStatus(String interfaceId) {
		// 查询条件
		ArrayList<Predicate> list = new ArrayList<>();
		if (StringUtils.isEmpty(interfaceId)) {
			throw new RuntimeException("获取接口请求状态，id必传");
		}
		list.add(qTInterface.id.eq(interfaceId));
		String status = sqlQueryFactory
				.select(qTLog.status)
				.from(qTLog)
				.leftJoin(qTInterfaceMonitor).on(qTInterfaceMonitor.businessInterfaceId.eq(qTLog.businessInterfaceId))
				.leftJoin(qTBusinessInterface).on(qTBusinessInterface.id.eq(qTInterfaceMonitor.businessInterfaceId))
				.leftJoin(qTInterface).on(qTInterface.id.eq(qTBusinessInterface.requestInterfaceId))
				.where(list.toArray(new Predicate[list.size()])).limit(1)
				.orderBy(qTLog.createdTime.desc()).fetchOne();
		return status;
	}

	@ApiOperation(value = "下载详细日志")
	@GetMapping(path = "/downloadLogInfo/{id}")
	public void downloadLogInfo(@PathVariable String id, HttpServletRequest request, HttpServletResponse response) {
		// 校验是否获取到登录用户
//		String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
//		if (StringUtils.isBlank(loginUserName)) {
//			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!", "没有获取到登录用户!");
//		}
		if (StringUtils.isEmpty(id)) {
			throw new RuntimeException("下载日志详细，id必传!");
		}
		String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
		String fileName = "log_info_detail" + dateStr + ".txt";
		// 查询详情
		TLog tLog = sqlQueryFactory.select(Projections.bean(TLog.class, qTLog.id, qTLog.createdTime, qTLog.status,
				qTLog.venderRepTime, qTLog.businessRepTime, qTLog.visitAddr, qTLog.businessReq, qTLog.venderReq,
				qTLog.businessRep, qTLog.venderRep)).from(qTLog).where(qTLog.id.eq(Long.valueOf(id))).fetchFirst();
		// 解密，脱敏处理数据
		String businessRep = decryptAndFilterSensitive(tLog.getBusinessRep());
		tLog.setBusinessRep(businessRep);
		String venderRep = decryptAndFilterSensitive(tLog.getVenderRep());
		tLog.setVenderRep(venderRep);
		tLog.setBusinessReq(decryptAndFilterSensitive(tLog.getBusinessReq()));
		tLog.setVenderReq(decryptAndFilterSensitive(tLog.getVenderReq()));

		StringBuilder data = new StringBuilder();
		data.append("访问接口地址:").append(tLog.getVisitAddr()).append("\r\n");
		data.append("请求方请求:").append(tLog.getBusinessReq()).append("\r\n");
		data.append("请求方响应:").append(tLog.getBusinessRep()).append("\r\n");
		data.append("被请求方请求:").append(tLog.getVenderReq()).append("\r\n");
		data.append("被请求方响应:").append(tLog.getVenderRep()).append("\r\n");
		try {
			OutputStream ouputStream = response.getOutputStream();
			response.setContentType("application/csv;charset=UTF-8");
			request.setCharacterEncoding("UTF-8");
			response.setHeader("Content-disposition", "attachment;filename=" + fileName);
			ouputStream.write(data.toString().getBytes("UTF-8"));
			ouputStream.flush();
			ouputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@ApiOperation(value = "接口调试重放")
	@PostMapping("/interfaceDebugRedo/{authFlag}")
	public ResultDto<String> interfaceDebugRedo(@PathVariable("authFlag") String authFlag,
			@ApiParam(value = "接口转换配置ids") @RequestParam(value = "ids", required = true) String ids) {

		if (StringUtils.isBlank(ids)) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "接口转换配置ids必传");
		}
		Map<String , String> headerMap = new HashMap<>();
		String loginUrlPrefix = niFiRequestUtil.getInterfaceDebugWithAuth();
		if("1".equals(authFlag)) {
			headerMap.putAll(niFiRequestUtil.interfaceAuthLogin(loginUrlPrefix , false));
		}

		String[] idArrays = ids.split(",");
		if(idArrays != null && idArrays.length > 0) {
			try {
				Long[] realIds = new Long[idArrays.length];
				for(int i = 0 ; i < idArrays.length ; i++) {
					realIds[i] = Long.valueOf(idArrays[i]);
				}
				List<TLog> logs = sqlQueryFactory.select(qTLog).from(qTLog).where(qTLog.id.in(realIds)).fetch();
				for(TLog tlog: logs) {
					if(tlog.getDebugreplayFlag() == 0) {
						headerMap.put("Debugreplay-Flag", "2");
					}else {
						headerMap.put("Debugreplay-Flag", "3");
					}
					String format = decryptAndFilterSensitive(tlog.getBusinessReq());
					if("0".equals(tlog.getProjectId()) || "0".equals(tlog.getBusinessInterfaceId())) {
						if(StringUtils.isBlank(tlog.getVisitAddr())){
							continue;
						}
						String wsdlUrl = tlog.getVisitAddr();
						List<String> wsOperationNames = PlatformUtil.getWsdlOperationNames(wsdlUrl);
						if(wsOperationNames == null || wsOperationNames.size() == 0) {
							continue;
						}
						String methodName = wsOperationNames.get(0);
						PlatformUtil.invokeWsServiceWithOrigin(wsdlUrl, methodName, format , headerMap, readTimeout);
						continue;
					}
					niFiRequestUtil.interfaceDebug(format , headerMap , "1".equals(authFlag));
				}
			} catch (Exception e) {
				logger.error("获取接口调试显示数据失败! MSG:{}", ExceptionUtil.dealException(e));
				return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "日志重放失败");
			}
		}
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "日志重放成功");
	}


}
