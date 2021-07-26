package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.common.dto.TableData;
import com.iflytek.integrated.common.utils.ExceptionUtil;
import com.iflytek.integrated.common.utils.SensitiveUtils;
import com.iflytek.integrated.common.utils.ase.AesUtil;
import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.platform.dto.InterfaceDebugDto;
import com.iflytek.integrated.platform.dto.InterfaceMonitorDto;
import com.iflytek.integrated.platform.entity.QTInterfaceMonitor;
import com.iflytek.integrated.platform.entity.TBusinessInterface;
import com.iflytek.integrated.platform.entity.TLog;
import com.iflytek.integrated.platform.utils.NiFiRequestUtil;
import com.iflytek.integrated.platform.utils.PlatformUtil;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.SQLExpressions;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.iflytek.integrated.platform.entity.QTBusinessInterface.qTBusinessInterface;
import static com.iflytek.integrated.platform.entity.QTHospital.qTHospital;
import static com.iflytek.integrated.platform.entity.QTInterface.qTInterface;
import static com.iflytek.integrated.platform.entity.QTInterfaceMonitor.qTInterfaceMonitor;
import static com.iflytek.integrated.platform.entity.QTLog.qTLog;
import static com.iflytek.integrated.platform.entity.QTPlatform.qTPlatform;
import static com.iflytek.integrated.platform.entity.QTProject.qTProject;
import static com.iflytek.integrated.platform.entity.QTSys.qTSys;
import static com.iflytek.integrated.platform.entity.QTSysConfig.qTSysConfig;
import static com.iflytek.integrated.platform.entity.QTSysHospitalConfig.qTSysHospitalConfig;

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

	public LogService() {
		super(qTLog, qTLog.id);
	}

	/**
	 * 按接口分类来展示服务监控列表
	 * @param projectId
	 * @param platFormId
	 * @param sysId
	 * @param status
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
//	@ApiOperation(value = "查看服务监控列表")
//	@GetMapping("/getListPage")
//	public ResultDto<TableData<InterfaceMonitorDto>> getListPage(String projectId, String platFormId, String sysId,
//																 String status, @RequestParam(defaultValue = "1") Integer pageNo,
//																 @RequestParam(defaultValue = "10") Integer pageSize) {
//		try {
//			// 查询条件
//			ArrayList<Predicate> list = new ArrayList<>();
//			// 判断条件是否为空
//			if (StringUtils.isNotBlank(sysId)) {
//				list.add(qTSys.id.eq(sysId));
//			}
//			// 先合并t_interface_monitor，再根据三合一结果进行查询
//			String q = "queryMonitor";
//			StringPath queryLabel = Expressions.stringPath(q);
//			QTInterfaceMonitor monitor = new QTInterfaceMonitor(q);
//			SubQueryExpression query = SQLExpressions
//					.select(qTInterfaceMonitor.id, qTInterfaceMonitor.status.max().as("status"),
//							qTInterfaceMonitor.successCount.sum().as("SUCCESS_COUNT"),
//							qTInterfaceMonitor.errorCount.sum().as("ERROR_COUNT"), qTInterfaceMonitor.projectId,
//							qTInterfaceMonitor.platformId, qTInterfaceMonitor.sysId,
//							qTInterfaceMonitor.createdTime, qTInterfaceMonitor.typeId, qTType.typeName)
//					.from(qTInterfaceMonitor)
//					.leftJoin(qTType).on(qTInterfaceMonitor.typeId.eq(qTType.id))
//					.where(qTInterfaceMonitor.id.isNotNull())
//					.groupBy(qTInterfaceMonitor.platformId, qTInterfaceMonitor.sysId, qTInterfaceMonitor.typeId)
//					.orderBy(qTInterfaceMonitor.createdTime.desc());
//
//			// 按条件筛选
//			if (StringUtils.isNotBlank(projectId)) {
//				list.add(monitor.projectId.eq(projectId));
//			}
//			if (StringUtils.isNotBlank(platFormId)) {
//				list.add(monitor.platformId.eq(platFormId));
//			}
//			if (StringUtils.isNotBlank(status)) {
//				list.add(monitor.status.eq(status));
//			}
//			// 根据结果查询
//			QueryResults<InterfaceMonitorDto> queryResults = sqlQueryFactory
//					.select(Projections.bean(InterfaceMonitorDto.class, monitor.id, monitor.status,monitor.typeName,
//							monitor.successCount, monitor.errorCount,qTProject.projectName, qTPlatform.platformName,
//							qTSys.sysName))
//					.from(query, queryLabel).leftJoin(qTProject).on(qTProject.id.eq(monitor.projectId))
//					.leftJoin(qTPlatform).on(qTPlatform.id.eq(monitor.platformId)).leftJoin(qTSysConfig)
//					.on(qTSysConfig.platformId.eq(qTPlatform.id).and(qTSysConfig.sysConfigType.eq(1)))
//					.leftJoin(qTSys).on(qTSys.id.eq(qTSysConfig.sysId))
//					.leftJoin(qTInterface).on(qTInterface.sysId.eq(qTSys.id))
//					.groupBy(monitor.platformId, monitor.sysId, monitor.typeId)
//					.where(list.toArray(new Predicate[list.size()])).limit(pageSize).offset((pageNo - 1) * pageSize)
//					.orderBy(monitor.createdTime.desc()).fetchResults();
//			// 分页
//			TableData<InterfaceMonitorDto> tableData = new TableData<>(queryResults.getTotal(),
//					queryResults.getResults());
//			return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "", tableData);
//		} catch (Exception e) {
//			logger.error("查看服务监控列表失败! MSG:{}", ExceptionUtil.dealException(e));
//			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "服务监控列表获取失败");
//		}
//	}

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
			QTInterfaceMonitor monitor = new QTInterfaceMonitor(q);
			SubQueryExpression query = SQLExpressions
					.select(qTInterfaceMonitor.status.max().as("status"),
							qTInterfaceMonitor.successCount.sum().as("SUCCESS_COUNT"),
							qTInterfaceMonitor.errorCount.sum().as("ERROR_COUNT"), qTInterfaceMonitor.projectId,
							qTInterfaceMonitor.platformId, qTInterfaceMonitor.sysId, qTInterfaceMonitor.typeId,
							qTInterfaceMonitor.createdTime, qTInterfaceMonitor.businessInterfaceId,
							qTInterface.id.as("INTERFACE_ID"), qTInterface.interfaceName)
					.from(qTInterfaceMonitor)
					.leftJoin(qTBusinessInterface).on(qTBusinessInterface.id.eq(qTInterfaceMonitor.businessInterfaceId))
					.leftJoin(qTSysConfig).on(qTSysConfig.id.eq(qTBusinessInterface.requestSysconfigId)
							.and(qTSysConfig.platformId.eq(qTInterfaceMonitor.platformId)))
					.leftJoin(qTInterface).on(qTInterface.id.eq(qTBusinessInterface.requestInterfaceId))
					.groupBy(qTInterfaceMonitor.platformId, qTInterfaceMonitor.sysId, qTInterface.id)
					.orderBy(qTInterfaceMonitor.createdTime.desc());

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
				list.add(monitor.interfaceName.like(PlatformUtil.createFuzzyText(interfaceName)));
			}
			// 根据结果查询
			QueryResults<InterfaceMonitorDto> queryResults = sqlQueryFactory
					.select(Projections.bean(InterfaceMonitorDto.class, monitor.status, monitor.successCount,
							monitor.errorCount, monitor.interfaceName, monitor.interfaceId,
							qTProject.projectName, qTPlatform.platformName, qTSys.sysName))
					.from(query, queryLabel)
					.leftJoin(qTProject).on(qTProject.id.eq(monitor.projectId))
					.leftJoin(qTPlatform).on(qTPlatform.id.eq(monitor.platformId))
					.leftJoin(qTSysConfig).on(qTSysConfig.platformId.eq(qTPlatform.id).and(qTSysConfig.sysConfigType.eq(1)))
					.leftJoin(qTSys).on(qTSys.id.eq(qTSysConfig.sysId))
					.where(list.toArray(new Predicate[list.size()])).limit(pageSize).offset((pageNo - 1) * pageSize)
					.orderBy(monitor.status.desc(), monitor.createdTime.desc()).fetchResults();
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
		// 查询条件
		ArrayList<Predicate> list = new ArrayList<>();
		if (StringUtils.isEmpty(interfaceId)) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "获取日志详细列表，id必传");
		}
		list.add(qTInterface.id.eq(interfaceId));
		if (StringUtils.isNotBlank(status)) {
			list.add(qTLog.status.eq(status));
		}
		try{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			list.add(qTLog.createdTime.goe(sdf.parse(startTime)));
			list.add(qTLog.createdTime.loe(sdf.parse(endTime)));
		}catch (ParseException e){
			e.printStackTrace();
		}
//		try{
//			if (StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime)) {
//				list.add(qTLog.createdTime.goe(sdf.parse(startTime)));
//				list.add(qTLog.createdTime.loe(sdf.parse(endTime)));
//			}else{
//				String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
//				Calendar calendar = Calendar.getInstance();
//				Date endDate = sdf.parse(dateStr);
//				calendar.setTime(endDate);
//				calendar.set(Calendar.HOUR_OF_DAY,calendar.get(Calendar.HOUR_OF_DAY) - 2);
//				Date startDate = calendar.getTime();
//				list.add(qTLog.createdTime.goe(startDate));
//				list.add(qTLog.createdTime.loe(endDate));
//			}
//		}catch (ParseException e){
//			e.printStackTrace();
//		}
		// 模糊查询接口地址
		if (StringUtils.isNotBlank(visitAddr)) {
			list.add(qTLog.visitAddr.like(PlatformUtil.createFuzzyText(visitAddr)));
		}
		if (StringUtils.isNotBlank(businessReq)) {
			list.add(Expressions.stringTemplate("AES_DECRYPT(from_base64({0}),{1})", qTLog.businessReq, "w5xv7[Nmc0Z/3U^X")
					.like(PlatformUtil.createFuzzyText(businessReq)));
		}
		if (StringUtils.isNotBlank(businessRep)) {
			list.add(Expressions.stringTemplate("AES_DECRYPT(from_base64({0}),{1})", qTLog.businessRep, "w5xv7[Nmc0Z/3U^X")
					.like(PlatformUtil.createFuzzyText(businessRep)));
		}
		if (StringUtils.isNotBlank(venderReq)) {
			list.add(Expressions.stringTemplate("AES_DECRYPT(from_base64({0}),{1})", qTLog.venderReq, "w5xv7[Nmc0Z/3U^X")
					.like(PlatformUtil.createFuzzyText(venderReq)));
		}
		if (StringUtils.isNotBlank(venderRep)) {
			list.add(Expressions.stringTemplate("AES_DECRYPT(from_base64({0}),{1})", qTLog.venderRep, "w5xv7[Nmc0Z/3U^X")
					.like(PlatformUtil.createFuzzyText(venderRep)));
		}

		QueryResults<TLog> queryResults = sqlQueryFactory
				.select(Projections.bean(TLog.class, qTLog.id, qTLog.createdTime, qTLog.status, qTLog.venderRepTime,
						qTLog.businessRepTime, qTLog.visitAddr,qTLog.businessInterfaceId,
						qTBusinessInterface.businessInterfaceName.as("businessInterfaceName"),
						qTBusinessInterface.excErrOrder.add(1).as("excErrOrder")))
				.from(qTLog)
				.leftJoin(qTInterfaceMonitor).on(qTInterfaceMonitor.businessInterfaceId.eq(qTLog.businessInterfaceId))
				.leftJoin(qTBusinessInterface).on(qTBusinessInterface.id.eq(qTInterfaceMonitor.businessInterfaceId))
				.leftJoin(qTInterface).on(qTInterface.id.eq(qTBusinessInterface.requestInterfaceId))
				.where(list.toArray(new Predicate[list.size()])).limit(pageSize).offset((pageNo - 1) * pageSize)
				.orderBy(qTLog.createdTime.desc()).fetchResults();

		long l = sqlQueryFactory.select(qTBusinessInterface).from(qTBusinessInterface)
				.where(qTBusinessInterface.requestInterfaceId.eq(interfaceId)).fetchCount();
		List<TLog> tlogList = queryResults.getResults();
		for(TLog log : tlogList){
			String interfaceOrder = log.getExcErrOrder()+"/"+ l;
			log.setInterfaceOrder(interfaceOrder);
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
				qTLog.businessRep, qTLog.venderRep,
				qTBusinessInterface.businessInterfaceName.as("businessInterfaceName"),
				qTBusinessInterface.excErrOrder.add(1).as("excErrOrder"))).from(qTLog)
				.leftJoin(qTBusinessInterface).on(qTBusinessInterface.id.eq(qTLog.businessInterfaceId))
						.where(qTLog.id.eq(Long.valueOf(id))).fetchFirst();
		long l = sqlQueryFactory.select(qTBusinessInterface).from(qTBusinessInterface)
				.where(qTBusinessInterface.requestInterfaceId.eq(interfaceId)).fetchCount();
		String interfaceOrder = tLog.getExcErrOrder()+"/"+ l;
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
	@PostMapping("/interfaceDebugRedo")
	public ResultDto<String> interfaceDebugRedo(
			@ApiParam(value = "接口转换配置ids") @RequestParam(value = "ids", required = true) String ids) {

		if (StringUtils.isBlank(ids)) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "接口转换配置ids必传");
		}

		String[] idArrays = ids.split(",");
		int successCount = 0;
		int errorConut = 0;
		String errorIds = "";
		for(String id : idArrays){
			//请求方接口调试数据获取
			try {
				List<TBusinessInterface> businessInterfaces = sqlQueryFactory
						.select(Projections.bean(TBusinessInterface.class, qTBusinessInterface.id,
								qTBusinessInterface.requestInterfaceId, qTBusinessInterface.requestSysconfigId,
								qTProject.projectCode.as("projectCode"), qTInterface.interfaceUrl.as("interfaceUrl"),
								qTSys.sysCode.as("sysCode"), qTInterface.inParamFormatType.as("sysIntfInParamFormatType"),
								qTInterface.sysId.as("requestSysId")))
						.from(qTBusinessInterface).leftJoin(qTInterface)
						.on(qTBusinessInterface.requestInterfaceId.eq(qTInterface.id)).leftJoin(qTSysConfig)
						.on(qTSysConfig.id.eq(qTBusinessInterface.requestSysconfigId)).leftJoin(qTPlatform)
						.on(qTPlatform.id.eq(qTSysConfig.platformId)).leftJoin(qTProject)
						.on(qTProject.id.eq(qTPlatform.projectId)).leftJoin(qTSys).on(qTSys.id.eq(qTSysConfig.sysId))
						.where(qTBusinessInterface.id.eq(id)).fetch();
				if (businessInterfaces == null || businessInterfaces.size() == 0) {
					continue;
				}
				// 获取入参列表
				TBusinessInterface businessInterface = businessInterfaces.get(0);
				String interfaceId = StringUtils.isNotEmpty(businessInterface.getRequestInterfaceId())
						? businessInterface.getRequestInterfaceId() : "";
				// 获取医院名称列表
				List<String> sysconfigIds = new ArrayList<>();
				businessInterfaces.forEach(bi -> {
					if (StringUtils.isNotEmpty(businessInterface.getRequestSysconfigId())) {
						sysconfigIds.add(businessInterface.getRequestSysconfigId());
					}
					if (StringUtils.isNotEmpty(businessInterface.getRequestedSysconfigId())) {
						sysconfigIds.add(businessInterface.getRequestedSysconfigId());
					}
				});
				List<String> hospitalCodes = sqlQueryFactory.select(qTSysHospitalConfig.hospitalCode)
						.from(qTSysHospitalConfig).leftJoin(qTSysConfig)
						.on(qTSysConfig.id.eq(qTSysHospitalConfig.sysConfigId)).leftJoin(qTHospital)
						.on(qTSysHospitalConfig.hospitalId.eq(qTHospital.id)).where(qTSysConfig.id.in(sysconfigIds))
						.fetch();
				// 拼接实体
				InterfaceDebugDto dto = new InterfaceDebugDto();
				if ("2".equals(businessInterface.getSysIntfInParamFormatType())) {
					String inparamFormat = sqlQueryFactory.select(qTInterface.inParamFormat).from(qTInterface).where(
							qTInterface.id.eq(interfaceId).and(qTInterface.sysId.eq(businessInterface.getRequestSysId())))
							.fetchFirst();
					dto.setFormat(inparamFormat);
					String wsUrl = niFiRequestUtil.getWsServiceUrl();
					if (!wsUrl.endsWith("/")) {
						wsUrl = wsUrl + "/";
					}
					String suffix = "services/" + businessInterface.getSysCode() + "/" + hospitalCodes.get(0);
					wsUrl = wsUrl + suffix;
					dto.setWsdlUrl(wsUrl);
					List<String> wsOperationNames = PlatformUtil.getWsdlOperationNames(wsUrl);
					dto.setWsOperationName(wsOperationNames.get(0));
					dto.setSysIntfParamFormatType("2");
				} else {
					dto.setSysIntfParamFormatType("3");
				}
				dto.setFuncode(businessInterface.getInterfaceUrl());

				if ("2".equals(dto.getSysIntfParamFormatType())) {
					String wsdlUrl = dto.getWsdlUrl();
					String methodName = dto.getWsOperationName();
					String funcode = dto.getFuncode();
					String param = dto.getFormat();
					PlatformUtil.invokeWsService(wsdlUrl, methodName, funcode, param);
				} else {
					niFiRequestUtil.interfaceDebug(dto.getFormat());
				}
				successCount++;
			} catch (Exception e) {
				logger.error("获取接口调试显示数据失败! MSG:{}", ExceptionUtil.dealException(e));
				errorConut ++;
				errorIds += id + ",";
			}
		}
		if(successCount == idArrays.length){
			return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "日志重放成功",
					"成功条数为" +successCount + "");
		}else if(errorConut > 0 && successCount > 0){
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "部分日志重放成功," + errorIds +"重放失败",
					"成功条数为" + successCount);
		}else{
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "日志重放失败,"+ errorIds +"重放失败");
		}
	}

	/**
	 * 先解密，再脱敏处理
	 * 
	 * @param aes
	 * @return
	 */
	private String decryptAndFilterSensitive(String aes) {
		try {
			return SensitiveUtils.filterSensitive(AesUtil.decrypt(aes));
		} catch (Exception e) {
			return "";
		}
	}
}
