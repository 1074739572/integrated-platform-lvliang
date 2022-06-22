package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.common.dto.TableData;
import com.iflytek.integrated.common.utils.ExceptionUtil;
import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.platform.dto.InterfaceMonitorDto;
import com.iflytek.integrated.platform.entity.*;
import com.iflytek.integrated.platform.utils.NiFiRequestUtil;
import com.iflytek.integrated.platform.utils.PlatformUtil;
import com.querydsl.core.QueryFlag;
import com.querydsl.core.QueryFlag.Position;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.iflytek.integrated.platform.entity.QTBusinessInterface.qTBusinessInterface;
import static com.iflytek.integrated.platform.entity.QTInterface.qTInterface;
import static com.iflytek.integrated.platform.entity.QTLog.qTLog;
import static com.iflytek.integrated.platform.entity.QTSys.qTSys;
import static com.iflytek.integrated.platform.entity.QTSysPublish.qTSysPublish;

/**
 * @author czzhan
 * @version 1.0
 * @date 2020/12/20 16:46
 */
@Slf4j
@Api(tags = "服务监控")
@RestController
@RequestMapping("/{version}/pt/log")
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


	@ApiOperation(value = "查看监控日志列表")
	@GetMapping("/logInfoList")
	public ResultDto<TableData<TLog>> logInfoList(String interfaceId, String status, String visitAddr, String interfaceName,
												   @ApiParam(value = "开始时间") @RequestParam(value = "startTime", required = false) String startTime,
												   @ApiParam(value = "结束时间") @RequestParam(value = "endTime", required = false) String endTime,
												   @ApiParam(value = "请求方请求") @RequestParam(value = "businessReq", required = false) String businessReq,
												   @ApiParam(value = "请求方响应") @RequestParam(value = "businessRep", required = false) String businessRep,
												   @ApiParam(value = "被请求方请求") @RequestParam(value = "venderReq", required = false) String venderReq,
												   @ApiParam(value = "被请求方响应") @RequestParam(value = "venderRep", required = false) String venderRep,
												   @RequestParam(defaultValue = "1") Integer pageNo,
											   @RequestParam(defaultValue = "10") Integer pageSize) {
		
//		if (StringUtils.isEmpty(interfaceId)) {
//			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "获取日志详细列表，id必传");
//		}
		// 查询条件
		ArrayList<Predicate> list = new ArrayList<>();
		if(interfaceId != null && interfaceId != ""){
			if(!"0".equals(interfaceId)) {
				list.add(qTInterface.id.eq(interfaceId));
			}else {
				list.add(qTInterface.id.eq("0"));
			}
		}
		
		try{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			if(startTime != null && startTime != ""){
				list.add(qTLog.createdTime.goe(sdf.parse(startTime)));
			}
			if(endTime != null && endTime != ""){
				list.add(qTLog.createdTime.loe(sdf.parse(endTime)));
			}
		}catch (ParseException e){
			e.printStackTrace();
		}

		if (StringUtils.isNotBlank(interfaceName)) {
			list.add(qTInterface.interfaceName.eq(interfaceName));
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
				qTLog.businessRepTime, qTLog.visitAddr,qTLog.businessInterfaceId, qTLog.debugreplayFlag,
				qTInterface.id.as("interfaceId"), qTInterface.interfaceName, qTInterface.interfaceUrl,
				qTSysPublish.id.as("publishId"), qTSysPublish.publishName,
				qTSys.id.as("publishSysId"), qTSys.sysName.as("publishSysName")))
				.from(qTLog)
				.leftJoin(qTBusinessInterface).on(qTLog.businessInterfaceId.eq(qTBusinessInterface.id))
				.leftJoin(qTInterface).on(qTBusinessInterface.requestInterfaceId.eq(qTInterface.id))
				.leftJoin(qTSysPublish).on(qTLog.publishId.eq(qTSysPublish.id))
				.leftJoin(qTSys).on(qTSysPublish.sysId.eq(qTSys.id))
				;
		if(!"postgresql".equals(dbType)) {
			tlogQuery = tlogQuery.addFlag(new QueryFlag(Position.BEFORE_FILTERS, Expressions.stringTemplate(" FORCE INDEX ( log_query_idx )")));
		}
		QueryResults<TLog> queryResults = tlogQuery
				.where(list.toArray(new Predicate[list.size()])).limit(pageSize).offset((pageNo - 1) * pageSize)
				.orderBy(qTLog.createdTime.desc()).fetchResults();
		
		// 分页
		TableData<TLog> tableData = new TableData<>(queryResults.getTotal(), queryResults.getResults());
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "日志详细列表获取成功!", tableData);
	}

	@ApiOperation(value = "查看日志详细信息")
	@GetMapping("/logInfo")
	public ResultDto<TLog> logInfo(String id) {
		if (StringUtils.isEmpty(id)) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "获取日志详细，id必传");
		}

		// 查询详情
		TLog tLog = sqlQueryFactory.select(Projections.bean(TLog.class, qTLog.id, qTLog.createdTime, qTLog.status,
						qTLog.venderRepTime, qTLog.businessRepTime, qTLog.visitAddr, qTLog.businessReq, qTLog.venderReq,
						qTLog.businessRep, qTLog.venderRep,qTLog.debugreplayFlag,
						qTInterface.interfaceName, qTInterface.interfaceUrl))
				.from(qTLog)
				.leftJoin(qTBusinessInterface).on(qTLog.businessInterfaceId.eq(qTBusinessInterface.id))
				.leftJoin(qTInterface).on(qTBusinessInterface.requestInterfaceId.eq(qTInterface.id))
				.where(qTLog.id.eq(Long.valueOf(id)))
				.fetchFirst();

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
					String regConnectionType = tlog.getRegConnectionType();
					if(StringUtils.isNotEmpty(regConnectionType)) {
						if(StringUtils.isBlank(tlog.getVisitAddr())){
							continue;
						}
						if("1".equals(regConnectionType)){
							String wsdlUrl = tlog.getVisitAddr();
							List<String> wsOperationNames = PlatformUtil.getWsdlOperationNames(wsdlUrl);
							if(wsOperationNames == null || wsOperationNames.size() == 0) {
								continue;
							}
							String methodName = wsOperationNames.get(0);
							PlatformUtil.invokeWsServiceWithOrigin(wsdlUrl, methodName, format , headerMap, readTimeout);
						}else if("2".equals(regConnectionType)){
							niFiRequestUtil.interfaceDebug(format , headerMap , "1".equals(authFlag));
						}else if("3".equals(regConnectionType)){

						}
					}

				}
			} catch (Exception e) {
				logger.error("获取接口调试显示数据失败! MSG:{}", ExceptionUtil.dealException(e));
				return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "日志重放失败");
			}
		}
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "日志重放成功");
	}

}
