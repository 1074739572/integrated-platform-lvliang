package com.iflytek.integrated.platform.service;

import static com.iflytek.integrated.platform.entity.QTBusinessInterface.qTBusinessInterface;
import static com.iflytek.integrated.platform.entity.QTDrive.qTDrive;
import static com.iflytek.integrated.platform.entity.QTHospital.qTHospital;
import static com.iflytek.integrated.platform.entity.QTInterface.qTInterface;
import static com.iflytek.integrated.platform.entity.QTInterfaceParam.qTInterfaceParam;
import static com.iflytek.integrated.platform.entity.QTPlatform.qTPlatform;
import static com.iflytek.integrated.platform.entity.QTPlugin.qTPlugin;
import static com.iflytek.integrated.platform.entity.QTProject.qTProject;
import static com.iflytek.integrated.platform.entity.QTSys.qTSys;
import static com.iflytek.integrated.platform.entity.QTSysConfig.qTSysConfig;
import static com.iflytek.integrated.platform.entity.QTSysDriveLink.qTSysDriveLink;
import static com.iflytek.integrated.platform.entity.QTSysHospitalConfig.qTSysHospitalConfig;
import static com.iflytek.integrated.platform.entity.QTType.qTType;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.common.dto.TableData;
import com.iflytek.integrated.common.intercept.UserLoginIntercept;
import com.iflytek.integrated.common.utils.ExceptionUtil;
import com.iflytek.integrated.common.validator.ValidationResult;
import com.iflytek.integrated.common.validator.ValidatorHelper;
import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.platform.common.RedisService;
import com.iflytek.integrated.platform.dto.BusinessInterfaceDto;
import com.iflytek.integrated.platform.dto.DbUrlTestDto;
import com.iflytek.integrated.platform.dto.InDebugResDto;
import com.iflytek.integrated.platform.dto.InterfaceDebugDto;
import com.iflytek.integrated.platform.dto.InterfaceDto;
import com.iflytek.integrated.platform.dto.JoltDebuggerDto;
import com.iflytek.integrated.platform.dto.MockTemplateDto;
import com.iflytek.integrated.platform.dto.ParamsDto;
import com.iflytek.integrated.platform.dto.RedisDto;
import com.iflytek.integrated.platform.dto.RedisKeyDto;
import com.iflytek.integrated.platform.entity.TBusinessInterface;
import com.iflytek.integrated.platform.entity.TDrive;
import com.iflytek.integrated.platform.entity.THospital;
import com.iflytek.integrated.platform.entity.TInterface;
import com.iflytek.integrated.platform.entity.TInterfaceParam;
import com.iflytek.integrated.platform.entity.TPlatform;
import com.iflytek.integrated.platform.entity.TPlugin;
import com.iflytek.integrated.platform.entity.TProject;
import com.iflytek.integrated.platform.entity.TSys;
import com.iflytek.integrated.platform.entity.TSysConfig;
import com.iflytek.integrated.platform.entity.TSysDriveLink;
import com.iflytek.integrated.platform.entity.TSysHospitalConfig;
import com.iflytek.integrated.platform.entity.TType;
import com.iflytek.integrated.platform.utils.NiFiRequestUtil;
import com.iflytek.integrated.platform.utils.PlatformUtil;
import com.iflytek.medicalboot.core.id.BatchUidService;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.StringPath;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

/**
 * 接口管理
 * 
 * @author weihe9
 * @date 2020/12/12 17:16
 */
@Slf4j
@Api(tags = "系统接口管理")
@RestController
@RequestMapping("/{version}/pt/interfaceManage")
public class InterfaceService extends BaseService<TInterface, String, StringPath> {

	@Autowired
	private BusinessInterfaceService businessInterfaceService;
	@Autowired
	private InterfaceParamService interfaceParamService;
	@Autowired
	private SysConfigService sysConfigService;
	@Autowired
	private BatchUidService batchUidService;
	@Autowired
	private NiFiRequestUtil niFiRequestUtil;
	@Autowired
	private ValidatorHelper validatorHelper;
	@Autowired
	private RedisService redisService;
	
	private static final Logger logger = LoggerFactory.getLogger(InterfaceService.class);
	
	public InterfaceService() {
		super(qTInterface, qTInterface.id);
	}

	@ApiOperation(value = "更改mock状态", notes = "更改mock状态")
	@PostMapping("/updateMockStatus")
	@Transactional(rollbackFor = Exception.class)
	public ResultDto<String> updateMockStatus(
			@ApiParam(value = "接口配置") @RequestParam(value = "id", required = true) String id,
			@ApiParam(value = "更改后的状态") @RequestParam(value = "mockStatus", required = true) String mockStatus) {
		// 校验是否获取到登录用户
		String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
        if (StringUtils.isBlank(loginUserName)) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!");
		}
		return businessInterfaceService.updateMockStatus(id, mockStatus, loginUserName);
	}

	@ApiOperation(value = "更改接口配置状态", notes = "更改接口配置状态")
	@PostMapping("/updateStatus")
	@Transactional(rollbackFor = Exception.class)
	public ResultDto<String> updateStatus(
			@ApiParam(value = "接口配置") @RequestParam(value = "id", required = true) String id,
			@ApiParam(value = "更改后的状态") @RequestParam(value = "status", required = true) String status) {
		// 校验是否获取到登录用户
		String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
		if (StringUtils.isBlank(loginUserName)) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!");
		}
		return businessInterfaceService.updateStatus(id, status, loginUserName);
	}

	@ApiOperation(value = "获取mock模板", notes = "获取mock模板")
	@GetMapping("/getMockTemplate")
	public ResultDto<List<MockTemplateDto>> getMockTemplate(
			@ApiParam(value = "接口配置id") @RequestParam(value = "id", required = true) String id) {
		List<TBusinessInterface> interfaces = businessInterfaceService.busInterfaces(id);
		if (CollectionUtils.isEmpty(interfaces)) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "根据id没有找到接口配置");
		}
		List<MockTemplateDto> dtoList = new ArrayList<>();
		for (TBusinessInterface businessInterface : interfaces) {
			String mocktpl = businessInterface.getMockTemplate();
			MockTemplateDto dto = new MockTemplateDto(businessInterface.getId(),
					// 如果mock模板为空，取出参的格式，作为初始的mock模板
					(StringUtils.isNotBlank(mocktpl) && !"null".equalsIgnoreCase(mocktpl)) ? mocktpl
							: businessInterface.getOutParamFormat(),
					businessInterface.getMockIsUse(), businessInterface.getExcErrOrder(),
					businessInterface.getBusinessInterfaceName());
			dtoList.add(dto);
		}
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取mock模板成功!", dtoList);
	}

	@ApiOperation(value = "保存mock模板", notes = "保存mock模板")
	@PostMapping("/saveMockTemplate")
	@Transactional(rollbackFor = Exception.class)
	public ResultDto<String> saveMockTemplate(@RequestBody List<MockTemplateDto> dtoList) {
		// 校验是否获取到登录用户
		String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
		if (StringUtils.isBlank(loginUserName)) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!");
		}
		if (CollectionUtils.isEmpty(dtoList)) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到mock模板!");
		}
		// 返回缓存操作数据
		List<String> rtnStr = new ArrayList<>();
		for (MockTemplateDto dto : dtoList) {
			// 校验参数是否完整
			ValidationResult validationResult = validatorHelper.validate(dto);
			if (validationResult.isHasErrors()) {
				return new ResultDto<>(Constant.ResultCode.ERROR_CODE, validationResult.getErrorMsg(),
						validationResult.getErrorMsg());
			}
			// 校验mock模板格式是否正确
			PlatformUtil.strIsJsonOrXml(dto.getMockTemplate());
			long lon = businessInterfaceService.saveMockTemplate(dto.getId(), dto.getMockTemplate(), dto.getMockIsUse(),
					loginUserName);
			if (lon <= 0) {
				throw new RuntimeException("保存mock模板失败!");
			}
			rtnStr.add(dto.getId());
		}
		//redis缓存信息获取
		ArrayList<Predicate> arr = new ArrayList<>();
		arr.add(qTBusinessInterface.id.in(rtnStr));
		List<RedisKeyDto> redisKeyDtoList = redisService.getRedisKeyDtoList(arr);
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "保存mock模板成功!", new RedisDto(redisKeyDtoList).toString());
	}

	@ApiOperation(value = "请求方接口调试数据获取", notes = "请求方接口调试数据获取")
	@GetMapping("/getInterfaceDebug")
	public ResultDto<InDebugResDto> getInterfaceDebug(
			@ApiParam(value = "接口转换配置id") @RequestParam(value = "id", required = true) String id) {
		try {
			List<TBusinessInterface> businessInterfaces = sqlQueryFactory
					.select(Projections.bean(TBusinessInterface.class, qTBusinessInterface.id,
							qTBusinessInterface.requestInterfaceId, qTBusinessInterface.requestSysconfigId,
							qTInterface.interfaceUrl.as("interfaceUrl"),
							qTSys.sysCode.as("sysCode"), qTInterface.inParamFormatType.as("sysIntfInParamFormatType"),
							qTInterface.sysId.as("requestSysId")))
					.from(qTBusinessInterface).leftJoin(qTInterface)
					.on(qTBusinessInterface.requestInterfaceId.eq(qTInterface.id)).leftJoin(qTSysConfig)
					.on(qTSysConfig.id.eq(qTBusinessInterface.requestSysconfigId)).leftJoin(qTPlatform)
					.on(qTPlatform.id.eq(qTSysConfig.platformId)).leftJoin(qTSys).on(qTSys.id.eq(qTSysConfig.sysId))
					.where(qTBusinessInterface.id.eq(id)).fetch();
			if (businessInterfaces == null || businessInterfaces.size() == 0) {
				return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有查询到接口配置信息");
			}
			// 获取入参列表
			TBusinessInterface businessInterface = businessInterfaces.get(0);
			String interfaceId = StringUtils.isNotEmpty(businessInterface.getRequestInterfaceId())
					? businessInterface.getRequestInterfaceId()
					: "";
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
			InDebugResDto resDto = new InDebugResDto();
			String inparamFormat = sqlQueryFactory.select(qTInterface.inParamFormat).from(qTInterface).where(
					qTInterface.id.eq(interfaceId).and(qTInterface.sysId.eq(businessInterface.getRequestSysId())))
					.fetchFirst();
			if ("2".equals(businessInterface.getSysIntfInParamFormatType())) {
				resDto.setWsInParams(inparamFormat);
				String wsUrl = niFiRequestUtil.getWsServiceUrl();
				if (!wsUrl.endsWith("/")) {
					wsUrl = wsUrl + "/";
				}
				String suffix = "services/" + businessInterface.getSysCode() + "/" + hospitalCodes.get(0);
				wsUrl = wsUrl + suffix;
				resDto.setWsdlUrl(wsUrl);
				List<String> wsOperationNames = PlatformUtil.getWsdlOperationNames(wsUrl);
				resDto.setWsOperationNames(wsOperationNames);
				resDto.setSysIntfParamFormatType("2");
			} else {
				List<String> paramNames = sqlQueryFactory.select(qTInterfaceParam.paramName).from(qTInterfaceParam)
						.where(qTInterfaceParam.interfaceId.eq(interfaceId)
								.and(qTInterfaceParam.paramInOut.eq(Constant.ParmInOut.IN)))
						.fetch();
				resDto.setInParams(paramNames);
				Map<String , Object> paramsMap = new HashMap<>();
				ObjectMapper objectMapper = new ObjectMapper();
				if(StringUtils.isNotBlank(inparamFormat)) {
					paramsMap = objectMapper.readValue(inparamFormat, new TypeReference<Map<String, Object>>() {
					});
				}
				paramsMap.put("funcode", businessInterface.getInterfaceUrl());
				paramsMap.put("productcode", businessInterface.getSysCode());
				paramsMap.put("orgid", hospitalCodes.get(0));
				resDto.setSysIntfParamFormatType("3");
				resDto.setWsInParams(objectMapper.writeValueAsString(paramsMap));
			}
			resDto.setFuncode(businessInterface.getInterfaceUrl());
			resDto.setProductcode(businessInterface.getSysCode());
			resDto.setOrgids(hospitalCodes);
			return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取接口调试显示数据成功!", resDto);
		} catch (Exception e) {
			logger.error("获取接口调试显示数据失败! MSG:{}", ExceptionUtil.dealException(e));
			e.printStackTrace();
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "获取接口调试显示数据失败!");
		}
	}

	@PostMapping("/interfaceDebug/{authFlag}")
	@ApiOperation(value = "请求方接口调试", notes = "请求方接口调试")
	public ResultDto<String> interfaceDebug(@RequestBody InterfaceDebugDto degubDto , @PathVariable("authFlag") String authFlag) {
		String result = "";
		Map<String , String> headerMap = new HashMap<>();
		headerMap.put("Debugreplay-Flag", "1");
		String loginUrlPrefix = niFiRequestUtil.getInterfaceDebugWithAuth();
		boolean isws = "2".equals(degubDto.getSysIntfParamFormatType());
		if(isws) {
			loginUrlPrefix = niFiRequestUtil.getWsServiceUrlWithAuth() + "/services/";
		}
		
		if("1".equals(authFlag)) {
			log.info("isws:" + isws + ",loginurlprefix:" + loginUrlPrefix);
			Map<String , String> tokenInfo = niFiRequestUtil.interfaceAuthLogin(loginUrlPrefix , isws);
			if(tokenInfo != null && tokenInfo.size()> 0) {
				headerMap.putAll(tokenInfo);
			}
		}
		if ("2".equals(degubDto.getSysIntfParamFormatType())) {
			String wsdlUrl = degubDto.getWsdlUrl();
			String methodName = degubDto.getWsOperationName();
			String funcode = degubDto.getFuncode();
			String param = degubDto.getFormat();
			if("1".equals(authFlag)) {
				String path = wsdlUrl.substring(wsdlUrl.indexOf("/services"));
				wsdlUrl = niFiRequestUtil.getWsServiceUrlWithAuth() + path;
				log.info("wsinvokeurl:" + wsdlUrl);
			}
			result = PlatformUtil.invokeWsService(wsdlUrl, methodName, funcode, param , headerMap);
		} else {
			result = niFiRequestUtil.interfaceDebug(degubDto.getFormat() , headerMap , "1".equals(authFlag));
		}
		if (StringUtils.isBlank(result)) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "接口调试失败");
		}
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "", result);
	}

	@Transactional(rollbackFor = Exception.class)
	@ApiOperation(value = "系统接口删除", notes = "系统接口删除")
	@PostMapping("/delInterfaceById/{id}")
	public ResultDto<String> delInterfaceById(
			@ApiParam(value = "标准接口id") @PathVariable(value = "id", required = true) String id) {
		List<TBusinessInterface> list = businessInterfaceService.getListByInterfaceId(id);
		if (CollectionUtils.isNotEmpty(list)) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "该标准接口已有接口配置关联,无法删除!", "该标准接口已有接口配置关联,无法删除!");
		}
		// redis缓存信息获取
		ArrayList<Predicate> arr = new ArrayList<>();
		arr.add(qTInterface.id.in(id));
		List<RedisKeyDto> redisKeyDtoList = redisService.getRedisKeyDtoList(arr);
		// 删除接口
		long l = this.delete(id);
		if (l < 1) {
			throw new RuntimeException("标准接口删除成功!");
		}
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "标准接口删除成功!", new RedisDto(redisKeyDtoList).toString());
	}

	@Transactional(rollbackFor = Exception.class)
	@ApiOperation(value = "标准接口新增/编辑", notes = "标准接口新增/编辑")
	@PostMapping("/saveAndUpdateInterface")
	public ResultDto<String> saveAndUpdateInterface(@RequestBody InterfaceDto dto) {
		if (dto == null) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "数据传入错误!", "数据传入错误!");
		}
		// 校验是否获取到登录用户
		String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
		if (StringUtils.isBlank(loginUserName)) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!", "没有获取到登录用户!");
		}
		String interfaceName = dto.getInterfaceName();
		if (StringUtils.isBlank(interfaceName)) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "接口名为空!", "接口名为空!");
		}
		String interfaceUrl = dto.getInterfaceUrl();
		if (StringUtils.isBlank(interfaceUrl)) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "接口方法为空!", "接口方法为空!");
		}
		String id = dto.getId();
		if (StringUtils.isBlank(id)) {
			return this.saveInterface(dto, loginUserName);
		}
		return this.updateInterface(dto, loginUserName);
	}

	/** 新增标准接口 */
	private ResultDto saveInterface(InterfaceDto dto, String loginUserName) {
		String interfaceUrl = dto.getInterfaceUrl();
		if (null != this.getInterfaceByUrl(interfaceUrl , dto.getSysId())) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "该接口方法已存在!", "该接口方法已存在!");
		}
		// 出参
		List<TInterfaceParam> outParamList = dto.getOutParamList();
		if (CollectionUtils.isEmpty(outParamList)) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "出参不能为空!", "出参不能为空!");
		}

		TInterface ti = new TInterface();
		// 校验出入参格式字符串是否为json或者xml
		// 入参格式非必填
		String inParamFormat = dto.getInParamFormat();
		String inParamFormatType = dto.getInParamFormatType();
		if (StringUtils.isNotBlank(inParamFormat)) {
			PlatformUtil.strIsJsonOrXml(inParamFormat);
			String inParamSchema = niFiRequestUtil.generateSchemaToInterface(inParamFormat, inParamFormatType);
			ti.setInParamSchema(inParamSchema);
		}
		// 出参格式必填
		String outParamFormat = dto.getOutParamFormat();
		String outParamFormatType = dto.getOutParamFormatType();
		PlatformUtil.strIsJsonOrXml(outParamFormat);
		String outParamSchema = niFiRequestUtil.generateSchemaToInterface(outParamFormat, outParamFormatType);
		ti.setOutParamSchema(outParamSchema);
		// 新增标准接口
		String interfaceId = batchUidService.getUid(qTInterface.getTableName()) + "";
		ti.setId(interfaceId);
		ti.setInParamFormatType(inParamFormatType);
		ti.setOutParamFormatType(outParamFormatType);
		ti.setSysId(dto.getSysId());
		ti.setInterfaceName(dto.getInterfaceName());
		ti.setTypeId(dto.getTypeId());
		ti.setInterfaceUrl(dto.getInterfaceUrl());
		ti.setInParamFormat(inParamFormat);
		ti.setOutParamFormat(outParamFormat);
		ti.setCreatedTime(new Date());
		ti.setCreatedBy(loginUserName);
		ti.setAllowLogDiscard(dto.getAllowLogDiscard());
		ti.setInterfaceType(dto.getInterfaceType());

		// 新增接口参数
		// 入参
		List<TInterfaceParam> inParamList = dto.getInParamList();
		if (CollectionUtils.isNotEmpty(inParamList)) {
			// 可以为空 校验
			for (int i = 0; i < inParamList.size(); i++) {
				TInterfaceParam tip = new TInterfaceParam();
				tip.setId(batchUidService.getUid(qTInterfaceParam.getTableName()) + "");
				tip.setInterfaceId(interfaceId);
				TInterfaceParam obj = inParamList.get(i);
				tip.setParamName(obj.getParamName());
				tip.setParamType(obj.getParamType());
				tip.setParamInstruction(obj.getParamInstruction());
				tip.setParamInOut(Constant.ParmInOut.IN);
				tip.setCreatedTime(new Date());
				tip.setCreatedBy(loginUserName);
				interfaceParamService.post(tip);
			}
		}
		// 出参
		for (int i = 0; i < outParamList.size(); i++) {
			TInterfaceParam tip = new TInterfaceParam();
			tip.setId(batchUidService.getUid(qTInterfaceParam.getTableName()) + "");
			tip.setInterfaceId(interfaceId);
			TInterfaceParam obj = outParamList.get(i);
			tip.setParamName(obj.getParamName());
			tip.setParamType(obj.getParamType());
			tip.setParamInstruction(obj.getParamInstruction());
			tip.setParamInOut(Constant.ParmInOut.OUT);
			tip.setCreatedTime(new Date());
			tip.setCreatedBy(loginUserName);
			interfaceParamService.post(tip);
			// 是否开启 1开
			if ("1".equals(obj.getIsStart())) {
				// 存储参数到标准接口表
				ti.setParamOutStatus(obj.getParamName());
				ti.setParamOutStatusSuccess(obj.getParamOutStatusSuccess());
			}
		}
		// 新增标准接口
		this.post(ti);
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "标准接口新增成功!", null);
	}

	/** 修改标准接口 */
	private ResultDto updateInterface(InterfaceDto dto, String loginUserName) {
		String id = dto.getId();
		TInterface tf = this.getOne(id);
		if (tf == null) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "根据传入id未查出对应标准接口,检查是否传入错误!",
					"根据传入id未查出对应标准接口,检查是否传入错误!");
		}
		//redis缓存信息获取
		ArrayList<Predicate> arr = new ArrayList<>();
		arr.add(qTInterface.id.eq(id));
		List<RedisKeyDto> redisKeyDtoList = redisService.getRedisKeyDtoList(arr);
		// 传入标准接口方法
		String interfaceUrl = dto.getInterfaceUrl();
		if(!tf.getInterfaceUrl().equals(interfaceUrl)) {
			// 查询新接口方法是否已存在
			tf = sqlQueryFactory.select(qTInterface).from(qTInterface)
					.where(qTInterface.interfaceUrl.eq(interfaceUrl).and(qTInterface.sysId.eq(tf.getSysId())))
					.fetchOne();
			if (tf != null) {
				return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "该接口方法已存在!", "该接口方法已存在!");
			}
		}
		// 出参
		List<TInterfaceParam> outParamList = dto.getOutParamList();
		if (CollectionUtils.isEmpty(outParamList)) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "出参不能为空!", "出参不能为空!");
		}

		String interfaceName = dto.getInterfaceName();
		String interfaceTypeId = dto.getTypeId();
		// 校验出入参格式字符串是否为json或者xml
		String inParamFormat = dto.getInParamFormat();
		String outParamFormat = dto.getOutParamFormat();
		String inParamFormatType = dto.getInParamFormatType();
		String outParamFormatType = dto.getOutParamFormatType();
		String allowLogDiscard = dto.getAllowLogDiscard();
		Integer interfaceType = dto.getInterfaceType();

		String inParamSchema = "";
		if (StringUtils.isNotBlank(inParamFormat)) {
			PlatformUtil.strIsJsonOrXml(inParamFormat);
			inParamSchema = niFiRequestUtil.generateSchemaToInterface(inParamFormat,inParamFormatType);
		}
		PlatformUtil.strIsJsonOrXml(outParamFormat);
		String outParamSchema = niFiRequestUtil.generateSchemaToInterface(outParamFormat, outParamFormatType);

		// 修改标准接口信息
		long execute = sqlQueryFactory.update(qTInterface).set(qTInterface.interfaceName, interfaceName)
				.set(qTInterface.typeId, interfaceTypeId).set(qTInterface.interfaceUrl, interfaceUrl)
				.set(qTInterface.inParamFormat, inParamFormat).set(qTInterface.outParamFormat, outParamFormat)
				.set(qTInterface.inParamFormatType, inParamFormatType)
				.set(qTInterface.outParamFormatType, outParamFormatType).set(qTInterface.updatedTime, new Date())
				.set(qTInterface.paramOutStatus, "").set(qTInterface.paramOutStatusSuccess, "")
				.set(qTInterface.inParamSchema, inParamSchema).set(qTInterface.outParamSchema, outParamSchema)
				.set(qTInterface.updatedBy, loginUserName).set(qTInterface.allowLogDiscard, allowLogDiscard)
				.set(qTInterface.interfaceType, interfaceType)
				.where(qTInterface.id.eq(id)).execute();
		if (execute < 1) {
			throw new RuntimeException("修改标准接口信息失败!");
		}
		// 替换接口参数
		interfaceParamService.deleteProductInterfaceLinkById(id);
		// 入参
		List<TInterfaceParam> inParamList = dto.getInParamList();
		if (CollectionUtils.isNotEmpty(inParamList)) {
			for (int i = 0; i < inParamList.size(); i++) {
				TInterfaceParam tip = new TInterfaceParam();
				tip.setId(batchUidService.getUid(qTInterfaceParam.getTableName()) + "");
				tip.setInterfaceId(id);
				TInterfaceParam obj = inParamList.get(i);
				tip.setParamName(obj.getParamName());
				tip.setParamType(obj.getParamType());
				tip.setParamInstruction(obj.getParamInstruction());
				tip.setParamInOut(Constant.ParmInOut.IN);
				tip.setCreatedTime(new Date());
				tip.setCreatedBy(loginUserName);
				interfaceParamService.post(tip);
			}
		}
		// 出参
		for (int i = 0; i < outParamList.size(); i++) {
			TInterfaceParam tip = new TInterfaceParam();
			tip.setId(batchUidService.getUid(qTInterfaceParam.getTableName()) + "");
			tip.setInterfaceId(id);
			TInterfaceParam obj = outParamList.get(i);
			tip.setParamName(obj.getParamName());
			tip.setParamType(obj.getParamType());
			tip.setParamInstruction(obj.getParamInstruction());
			tip.setParamInOut(Constant.ParmInOut.OUT);
			tip.setCreatedTime(new Date());
			tip.setCreatedBy(loginUserName);
			interfaceParamService.post(tip);
			// 入参该字段表示是否开启 1开
			if ("1".equals(obj.getIsStart())) {
				// 标准接口信息出参赋值
				String paramName = obj.getParamName();
				String paramOutStatusSuccess = obj.getParamOutStatusSuccess();
				long l = sqlQueryFactory.update(qTInterface)
						.set(qTInterface.paramOutStatus, StringUtils.isBlank(paramName) ? "" : paramName)
						.set(qTInterface.paramOutStatusSuccess,
								StringUtils.isBlank(paramOutStatusSuccess) ? "" : paramOutStatusSuccess)
						.where(qTInterface.id.eq(id)).execute();
				if (l < 1) {
					throw new RuntimeException("修改标准接口出参信息失败!");
				}
			}
		}
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "标准接口修改成功!", new RedisDto(redisKeyDtoList).toString());
	}

	@ApiOperation(value = "获取接口分类")
	@GetMapping("/getInterfaceType")
	public ResultDto<List<TType>> getInterfaceType() {
		List<TType> vendors = sqlQueryFactory
				.select(Projections.bean(TType.class, qTType.id, qTType.typeCode, qTType.typeName, qTType.updatedTime))
				.from(qTType).where(qTType.type.eq(1)).orderBy(qTType.createdTime.desc()).fetch();
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "数据获取成功!", vendors);
	}

	@ApiOperation(value = "系统接口列表")
	@GetMapping("/getInterfaceList/{sysId}")
	public ResultDto<TableData<TInterface>> getInterfaceList(@ApiParam(value = "系统id") @PathVariable String sysId,
			@ApiParam(value = "接口分类id") @RequestParam(value = "typeId", required = false) String typeId,
			@ApiParam(value = "接口名称") @RequestParam(value = "name", required = false) String name,
			@ApiParam(value = "页码", example = "1") @RequestParam(value = "pageNo", defaultValue = "1", required = false) Integer pageNo,
			@ApiParam(value = "每页大小", example = "10") @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
		// 查询条件
		ArrayList<Predicate> list = new ArrayList<>();
		if (StringUtils.isNotEmpty(sysId)) {
			list.add(qTInterface.sysId.eq(sysId));
		}
		if (StringUtils.isNotEmpty(typeId)) {
			list.add(qTInterface.typeId.eq(typeId));
		}
		if (StringUtils.isNotEmpty(name)) {
			list.add(qTInterface.interfaceName.like(PlatformUtil.createFuzzyText(name)));
		}
		QueryResults<TInterface> queryResults = sqlQueryFactory
				.select(Projections.bean(TInterface.class,
						qTInterface.id, qTInterface.interfaceName,qTInterface.allowLogDiscard,
						qTInterface.interfaceUrl, qTInterface.inParamFormat, qTInterface.outParamFormat,
						qTInterface.createdTime, qTInterface.typeId, qTType.typeName.as("interfaceTypeName"),
						qTInterface.interfaceType,
						sqlQueryFactory.select(qTInterfaceParam.id.count()).from(qTInterfaceParam)
								.where((qTInterfaceParam.paramInOut.eq(Constant.ParmInOut.IN))
										.and(qTInterfaceParam.interfaceId.eq(qTInterface.id)))
								.as("inParamCount"),
						sqlQueryFactory.select(qTInterfaceParam.id.count()).from(qTInterfaceParam)
								.where((qTInterfaceParam.paramInOut.eq(Constant.ParmInOut.OUT))
										.and(qTInterfaceParam.interfaceId.eq(qTInterface.id)))
								.as("outParamCount")))
				.from(qTInterface).leftJoin(qTType).on(qTType.id.eq(qTInterface.typeId))
				.where(list.toArray(new Predicate[list.size()])).groupBy(qTInterface.id).limit(pageSize)
				.offset((pageNo - 1) * pageSize).orderBy(qTInterface.createdTime.desc()).fetchResults();
		// 分页
		TableData<TInterface> tableData = new TableData<>(queryResults.getTotal(), queryResults.getResults());
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "标准接口列表获取成功!", tableData);
	}

	@ApiOperation(value = "获取接口配置列表")
	@GetMapping("/getInterfaceConfigureList")
	public ResultDto<TableData<TBusinessInterface>> getInterfaceConfigureList(
			@ApiParam(value = "平台id") @RequestParam(value = "platformId", required = true) String platformId,
			@ApiParam(value = "启停用状态") @RequestParam(value = "status", required = false) String status,
			@ApiParam(value = "mock状态") @RequestParam(value = "mockStatus", required = false) String mockStatus,
			@ApiParam(value = "分类id") @RequestParam(value = "typeId", required = false) Integer typeId,
            @ApiParam(value = "请求方接口名称") @RequestParam(value = "requestInterfaceName", required = false) String requestInterfaceName,
            @ApiParam(value = "被请求方接口名称") @RequestParam(value = "requestedInterfaceName", required = false) String requestedInterfaceName,
			@ApiParam(value = "页码", example = "1") @RequestParam(value = "pageNo", defaultValue = "1", required = false) Integer pageNo,
			@ApiParam(value = "每页大小", example = "10") @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {

        ArrayList<Predicate> predicateList = new ArrayList<>();
        predicateList.add(qTSysConfig.platformId.eq(platformId));
        if (StringUtils.isNotEmpty(status)) {
            predicateList.add(qTBusinessInterface.status.eq(status));
        }
        if (StringUtils.isNotEmpty(mockStatus)) {
            predicateList.add(qTBusinessInterface.mockStatus.eq(mockStatus));
        }
		if (typeId != null && typeId.intValue() != 0) {
			predicateList.add(qTInterface.typeId.eq(typeId.toString()));
		}
        if (StringUtils.isNotEmpty(requestInterfaceName)) {
            predicateList.add(qTInterface.interfaceName.like("%" + requestInterfaceName + "%"));
        }
	    // 获取接口配置列表信息
		QueryResults<TBusinessInterface> queryResults = businessInterfaceService.getInterfaceConfigureList(predicateList, requestedInterfaceName, pageNo, pageSize);
		// 匹配列表展示信息
		List<TBusinessInterface> list = queryResults.getResults();
		if (CollectionUtils.isNotEmpty(list)) {
			for (TBusinessInterface tbi : list) {
				// 获取被请求方系统/接口
				if (StringUtils.isNotBlank(tbi.getBusinessInterfaceName())) {
					String[] bizNames = tbi.getBusinessInterfaceName().split(",");
					String bizSysInterfaces = "";
					String versionIds = "";
					for (String bizName : bizNames) {
						String sysConfigId = bizName.split("/")[0];
						String bizInterfaceName = bizName.substring(sysConfigId.length()+1);

						TSysConfig sysConfig = sqlQueryFactory.select(Projections.bean(TSysConfig.class,
								qTSys.sysName.append("/").append(bizInterfaceName).as("sysInterface"),
								qTSysConfig.versionId))
								.from(qTSysConfig).join(qTSys).on(qTSys.id.eq(qTSysConfig.sysId))
								.where(qTSysConfig.id.eq(sysConfigId)).fetchOne();
						String sysInterface = sysConfig.getSysInterface();
						String versionId = sysConfig.getVersionId();
//						String sysInterface = sqlQueryFactory.select(qTSys.sysName.append("/").append(bizInterfaceName))
//								.from(qTSysConfig).join(qTSys).on(qTSys.id.eq(qTSysConfig.sysId))
//								.where(qTSysConfig.id.eq(sysConfigId)).fetchOne();
						bizSysInterfaces += sysInterface + ",";
						versionIds += versionId + ",";
					}
					if (StringUtils.isNotBlank(bizSysInterfaces)) {
						if (bizSysInterfaces.endsWith(",")) {
							bizSysInterfaces = bizSysInterfaces.substring(0, bizSysInterfaces.lastIndexOf(","));
						}
						tbi.setBusinessInterfaceName(bizSysInterfaces);
					}
					if (StringUtils.isNotBlank(versionIds)) {
						if (versionIds.endsWith(",")) {
							versionIds = versionIds.substring(0, versionIds.lastIndexOf(","));
						}
						tbi.setVersionId(versionIds);
					}
				}
				// 获取请求方系统/接口
				if (StringUtils.isNotBlank(tbi.getRequestInterfaceId())) {
					String requestInter = getSysAndInterfaceById(tbi.getRequestInterfaceId());
					if (StringUtils.isNotBlank(requestInter)) {
						tbi.setRequestInterfaceName(requestInter);
					}
				}
			}
		}
		// 分页
		TableData<TBusinessInterface> tableData = new TableData<>(queryResults.getTotal(), queryResults.getResults());
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取接口配置列表获取成功", tableData);
	}

	private String getSysAndInterfaceById(String interfaceId) {
		return sqlQueryFactory.select(qTSys.sysName.append("/").append(qTInterface.interfaceName)).from(qTInterface)
				.join(qTSys).on(qTInterface.sysId.eq(qTSys.id)).where(qTInterface.id.eq(interfaceId)).fetchOne();
	}

	@ApiOperation(value = "获取接口配置详情")
	@GetMapping("/getInterfaceConfigInfoById")
	public ResultDto<BusinessInterfaceDto> getInterfaceConfigInfoById(
			@ApiParam(value = "接口配置id") @RequestParam(value = "id", required = true) String id) {
		// 返回对象
		BusinessInterfaceDto dto = new BusinessInterfaceDto();
		dto.setId(id);

		// 多个厂商接口信息
		List<TBusinessInterface> tbiList = new ArrayList<>();

		// 获取接口配置对象
		TBusinessInterface tbi = businessInterfaceService.getOne(id);
		if (tbi != null) {
			// 请求方标准接口
			String requestInterfaceId = tbi.getRequestInterfaceId();
			dto.setRequestInterfaceId(requestInterfaceId);
			dto.setInterfaceSlowFlag(tbi.getInterfaceSlowFlag());
			// 获取请求方接口类型
			ArrayList<Predicate> list = new ArrayList<>();
			if (StringUtils.isNotEmpty(requestInterfaceId)) {
				list.add(qTInterface.id.eq(requestInterfaceId));
			}
			TInterface tInterface = sqlQueryFactory.select(qTInterface).from(qTInterface)
					.where(list.toArray(new Predicate[list.size()])).fetchOne();
			dto.setRequestInterfaceTypeId(tInterface.getTypeId());
			// 获取厂商及配置信息
			String requestSysconfigId = tbi.getRequestSysconfigId();
			dto.setRequestSysconfigId(requestSysconfigId);
			TSysConfig tvc = sysConfigService.getOne(requestSysconfigId);
			if (tvc != null) {
				dto.setRequestSysId(tvc.getSysId());
			}
			// 多个厂商配置接口信息
			tbiList = businessInterfaceService.getTBusinessInterfaceList(tbi.getRequestInterfaceId(),
					tbi.getRequestSysconfigId());
			if (CollectionUtils.isNotEmpty(tbiList)) {
				for (TBusinessInterface tb : tbiList) {
					String requestdSysconfigId = tb.getRequestedSysconfigId();
					if(StringUtils.isNotEmpty(requestdSysconfigId)){
						TSysConfig config = sysConfigService.getOne(requestdSysconfigId);
						tb.setRequestedSysId(config.getSysId());
					}
				}
			}
		}

		dto.setBusinessInterfaceList(tbiList);

		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取接口配置详情成功", dto);
	}


	@ApiOperation(value = "获取接口信息（重放标识）", notes = "获取接口信息（重放标识）")
	@GetMapping("/getBusItfInfo")
	public ResultDto getBusItfInfo(
			@ApiParam(value = "请求方系统接口ID") @RequestParam(value = "reqItfId", required = true) String reqItfId){
		//校验入参
		if(StringUtils.isBlank(reqItfId)){
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "请求参数不能为空!");
		}
		// 校验是否获取到登录用户
		String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
		if (StringUtils.isBlank(loginUserName)) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!");
		}
		TBusinessInterface tb = businessInterfaceService.getOneByInterfaceId(reqItfId);
		Map resMap = new HashMap();
		resMap.put("replayFlag",tb.getReplayFlag());
		return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "获取接口信息",resMap);
	}


	@Transactional(rollbackFor = Exception.class)
	@ApiOperation(value = "新增/编辑接口配置", notes = "新增/编辑接口配置")
	@PostMapping("/saveAndUpdateInterfaceConfig/{opt}")
	public ResultDto<String> saveAndUpdateInterfaceConfig(@RequestBody BusinessInterfaceDto dto , @PathVariable("opt") String opt) {
		if (dto == null) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "请求参数不能为空!");
		}
		if(StringUtils.isBlank(dto.getRequestInterfaceId())) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "请求方接口不能为空!");
		}
		// 校验是否获取到登录用户
		String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
		if (StringUtils.isBlank(loginUserName)) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!");
		}
		String newReturnId = "";
		if (Constant.Operation.ADD.equals(dto.getAddOrUpdate())) {
			return this.saveInterfaceConfig(dto, loginUserName);
		}
		if (Constant.Operation.UPDATE.equals(dto.getAddOrUpdate())) {
			return this.updateInterfaceConfig(dto, loginUserName);
		}
		return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "addOrUpdate 新增编辑标识不正确!", null);
	}

	/**
	 * 新增接口配置
	 * 
	 * @param dto
	 * @param loginUserName
	 * @return
	 */
	private ResultDto<String> saveInterfaceConfig(BusinessInterfaceDto dto, String loginUserName) {
		// 获取厂商配置
		String requestSysConfigId = "";
		if (StringUtils.isBlank(dto.getRequestSysconfigId())) {
			TSysConfig tvc = sysConfigService.getRequestConfigByPlatformAndSys(dto.getPlatformId(),
					dto.getRequestSysId());
			requestSysConfigId = tvc != null ? tvc.getId() : null;
		} else {
			requestSysConfigId = dto.getRequestSysconfigId();
		}

		// 根据条件判断是否存在该数据
		List<TBusinessInterface> tbiList = businessInterfaceService.getBusinessInterfaceIsExist(dto.getProjectId(),
				dto.getRequestSysId(), dto.getRequestInterfaceId());
		if (CollectionUtils.isNotEmpty(tbiList)) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "根据项目id,系统id,请求方接口id匹配到该条件数据已存在!", null);
		}

		tbiList = dto.getBusinessInterfaceList();
		String returnId = "";
		for (int i = 0; i < tbiList.size(); i++) {
			TBusinessInterface tbi = tbiList.get(i);
			// 新增接口配置
			tbi.setId(batchUidService.getUid(qTBusinessInterface.getTableName()) + "");
			tbi.setRequestInterfaceId(dto.getRequestInterfaceId());
			tbi.setStatus(Constant.Status.START);
			tbi.setMockStatus(Constant.Status.STOP);
			tbi.setCreatedTime(new Date());
			tbi.setCreatedBy(loginUserName);
			tbi.setRequestSysconfigId(requestSysConfigId);
			tbi.setRequestedSysconfigId(tbi.getRequestedSysconfigId());
			tbi.setExcErrOrder(i);
			tbi.setInterfaceSlowFlag(dto.getInterfaceSlowFlag());
			tbi.setReplayFlag(dto.getReplayFlag());
			// 获取schema
			niFiRequestUtil.generateSchemaToInterface(tbi);
			// 新增接口配置
			businessInterfaceService.post(tbi);
			if(StringUtils.isBlank(returnId)) {
				returnId = tbi.getId();
			}
		}
		Map<String , String> data = new HashMap<String , String>();
		data.put("id", returnId);
		return new ResultDto<String>(Constant.ResultCode.SUCCESS_CODE, "新增接口配置成功", JSON.toJSONString(data));
	}

	/**
	 * 编辑接口配置
	 * 
	 * @param dto
	 * @param loginUserName
	 * @return
	 */
	private ResultDto updateInterfaceConfig(BusinessInterfaceDto dto, String loginUserName) {
		if (StringUtils.isBlank(dto.getPlatformId()) || StringUtils.isBlank(dto.getRequestSysId())) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "平台id或请求方系统id不能为空!", null);
		}
		// 获取系统配置
		TSysConfig tvc = sysConfigService.getRequestConfigByPlatformAndSys(dto.getPlatformId(), dto.getRequestSysId());
		String requestSysConfigId = tvc != null ? tvc.getId() : dto.getRequestSysconfigId();

		// 根据条件判断是否存在该数据
		TBusinessInterface exsitsBI = businessInterfaceService.getOne(dto.getId());
		if(exsitsBI != null) {
			//沒修改過請求方
			if(exsitsBI.getRequestSysconfigId().equals(dto.getRequestSysconfigId()) && exsitsBI.getRequestInterfaceId().equals(dto.getRequestInterfaceId())){
//				沒修改過，不校驗是否已存在
			}else {
				List<TBusinessInterface> tbiList = businessInterfaceService.getBusinessInterfaceIsExist(dto.getProjectId(),
						dto.getRequestSysId(), dto.getRequestInterfaceId());
				if (CollectionUtils.isNotEmpty(tbiList)) {
					return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "根据项目id,系统id,请求方接口id匹配到该条件数据已存在!", null);
				}
			}
		}
		
		// 返回缓存接口配置id
		List<String> rtnId = new ArrayList<>();
		List<TBusinessInterface> tbiList = dto.getBusinessInterfaceList();
		for (int i = 0; i < tbiList.size(); i++) {
			TBusinessInterface tbi = tbiList.get(i);
			if (StringUtils.isBlank(tbi.getId())) {
				// 新增的厂商配置
				tbi.setId(batchUidService.getUid(qTBusinessInterface.getTableName()) + "");
				tbi.setRequestInterfaceId(dto.getRequestInterfaceId());
				tbi.setStatus(Constant.Status.START);
				tbi.setMockStatus(Constant.Status.STOP);
				tbi.setCreatedTime(new Date());
				tbi.setCreatedBy(loginUserName);
				tbi.setRequestSysconfigId(requestSysConfigId);
				tbi.setRequestedSysconfigId(tbi.getRequestedSysconfigId());
				tbi.setExcErrOrder(i);
				tbi.setInterfaceSlowFlag(dto.getInterfaceSlowFlag());
				tbi.setReplayFlag(dto.getReplayFlag());
				// 获取schema
				niFiRequestUtil.generateSchemaToInterface(tbi);
				// 新增接口配置
				businessInterfaceService.post(tbi);
			} else {
				// 接口配置重新赋值
				tbi.setRequestInterfaceId(dto.getRequestInterfaceId());
				tbi.setUpdatedTime(new Date());
				tbi.setUpdatedBy(loginUserName);
				tbi.setRequestedSysconfigId(tbi.getRequestedSysconfigId());
				tbi.setExcErrOrder(i);
				tbi.setInterfaceSlowFlag(dto.getInterfaceSlowFlag());
				tbi.setReplayFlag(dto.getReplayFlag());
				// 获取schema
				niFiRequestUtil.generateSchemaToInterface(tbi);
				// 新增接口配置
				long l = businessInterfaceService.put(tbi.getId(), tbi);
				if (l < 1) {
					throw new RuntimeException("修改新增接口配置信息失败!");
				}
				rtnId.add(tbi.getId());
			}
		}
		// redis缓存信息获取
		ArrayList<Predicate> arr = new ArrayList<>();
		arr.add(qTBusinessInterface.id.in(rtnId));
		List<RedisKeyDto> redisKeyDtoList = redisService.getRedisKeyDtoList(arr);
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "编辑接口配置成功", new RedisDto(redisKeyDtoList).toString());
	}

	@ApiOperation(value = "根据参数格式获取jolt", notes = "根据参数格式获取jolt")
	@PostMapping("/paramFormatJolt")
	public ResultDto<String> paramFormatJolt(String paramFormat, String content,
			@RequestParam(value = "joltType", defaultValue = "request", required = false) String joltType) {
		String contentType = Constant.ParamFormatType.getByType(content);
		if (StringUtils.isBlank(contentType) || Constant.ParamFormatType.NONE.getType().equals(contentType)) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "参数类型无效!", "参数类型无效!");
		}
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "jolt获取成功!",
				niFiRequestUtil.generateJolt(paramFormat, contentType, joltType));
	}

	@Transactional(rollbackFor = Exception.class)
	@ApiOperation(value = "接口配置删除", notes = "接口配置删除")
	@PostMapping("/deleteInterfaceConfigure")
	public ResultDto<String> deleteInterfaceConfigure(
			@ApiParam(value = "接口配置id") @RequestParam(value = "id", required = true) String id) {
		TBusinessInterface tbi = businessInterfaceService.getOne(id);
		if (tbi == null) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "该接口id查询不到接口配置信息!", "该接口id查询不到接口配置信息!");
		}
		List<TBusinessInterface> list = businessInterfaceService.getListByCondition(tbi.getRequestInterfaceId(),
				tbi.getRequestSysconfigId());
		// 获取返回缓存id
		List<String> rtnStr = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(list)) {
			for (TBusinessInterface obj : list) {
				rtnStr.add(obj.getId());
			}
		}
		// redis缓存信息获取
		ArrayList<Predicate> arr = new ArrayList<>();
		arr.add(qTBusinessInterface.id.in(rtnStr));
		List<RedisKeyDto> redisKeyDtoList = redisService.getRedisKeyDtoList(arr);

		// 删除相同条件接口配置
		long count = businessInterfaceService.delObjByCondition(tbi.getRequestInterfaceId(),
				tbi.getRequestSysconfigId());
		if (count <= 0) {
			throw new RuntimeException("接口配置删除失败!");
		}
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "接口配置删除成功!共删除" + count + "条数据",
				new RedisDto(redisKeyDtoList).toString());
	}

	@ApiOperation(value = "根据id删除单个接口配置信息", notes = "根据id删除单个接口配置信息")
	@PostMapping("/deleteBusinessInterfaceById")
	public ResultDto<String> deleteBusinessInterfaceById(
			@ApiParam(value = "接口配置id") @RequestParam(value = "id", required = true) String id) {
		TBusinessInterface tbi = businessInterfaceService.getOne(id);
		if (tbi == null) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "根据id未查出该接口配置信息!", id);
		}
		// redis缓存信息获取
		ArrayList<Predicate> arr = new ArrayList<>();
		arr.add(qTBusinessInterface.id.eq(id));
		List<RedisKeyDto> redisKeyDtoList = redisService.getRedisKeyDtoList(arr);

		long count = businessInterfaceService.delete(id);
		if (count < 1) {
			throw new RuntimeException("根据id删除该接口配置信息失败!");
		}
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "单个接口配置信息删除成功!",
				new RedisDto(redisKeyDtoList).toString());
	}

	@ApiOperation(value = "获取系统接口详情", notes = "获取系统接口详情")
	@GetMapping("/getInterfaceInfoById")
	public ResultDto<InterfaceDto> getInterfaceInfoById(
			@ApiParam(value = "标准接口id") @RequestParam(value = "id", required = true) String id) {
		TInterface ti = this.getOne(id);
		if (ti == null) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "根据id未查出该标准接口!", null);
		}
		try {
			InterfaceDto iDto = new InterfaceDto();
			BeanUtils.copyProperties(ti, iDto);
			iDto.setSysId(ti.getSysId());
			// 获取接口参数
			List<TInterfaceParam> paramsList = interfaceParamService.getParamsByInterfaceId(id);
			// 入参
			List<TInterfaceParam> inParamList = new ArrayList<>();
			// 出参
			List<TInterfaceParam> outParamList = new ArrayList<>();
			if (CollectionUtils.isNotEmpty(paramsList)) {
				TInterfaceParam tip;
				for (TInterfaceParam obj : paramsList) {
					tip = new TInterfaceParam();
					tip.setParamName(obj.getParamName());
					tip.setParamType(obj.getParamType());
					tip.setParamInstruction(obj.getParamInstruction());
					tip.setParamLength(obj.getParamLength());
					tip.setParamInOut(obj.getParamInOut());
					if (iDto.getParamOutStatus().equals(obj.getParamName())) {
						// 开启状态
						tip.setIsStart("1");
						tip.setParamOutStatus(obj.getParamOutStatus());
						tip.setParamOutStatusSuccess(obj.getParamOutStatusSuccess());
					}
					if (Constant.ParmInOut.IN.equals(obj.getParamInOut())) {
						inParamList.add(tip);
					}
					if (Constant.ParmInOut.OUT.equals(obj.getParamInOut())) {
						outParamList.add(tip);
					}
				}
			}
			iDto.setInParamList(inParamList);
			iDto.setOutParamList(outParamList);
			return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取标准接口详情成功!", iDto);
		} catch (Exception e) {
			logger.error("获取标准接口详情失败! MSG:{}", ExceptionUtil.dealException(e));
			e.printStackTrace();
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "获取标准接口详情失败!");
		}
	}

	@ApiOperation(value = "选择接口下拉(可根据当前项目操作选择)")
	@GetMapping("/getDisInterface")
	public ResultDto<List<TInterface>> getDisInterface(
			@ApiParam(value = "项目id") @RequestParam(value = "projectId", required = false) String projectId,
			@ApiParam(value = "操作 1获取当前项目下的接口 2获取非当前项目下的接口") @RequestParam(defaultValue = "1", value = "status", required = false) String status) {
		List<TInterface> interfaces = null;
		if (StringUtils.isNotBlank(projectId)) {
			// 返回当前项目下的接口
			interfaces = sqlQueryFactory.select(qTInterface).from(qTInterface).leftJoin(qTBusinessInterface)
					.on(qTBusinessInterface.requestInterfaceId.eq(qTInterface.id)).leftJoin(qTSysConfig)
					.on(qTSysConfig.id.eq(qTBusinessInterface.requestSysconfigId))
					.where(qTSysConfig.projectId.eq(projectId)).orderBy(qTInterface.createdTime.desc()).fetch();
			if (Constant.Operation.CURRENT.equals(status)) {
				return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "数据获取成功!", interfaces);
			}
		}
		// 获取所有接口
		List<TInterface> allinterfaces = sqlQueryFactory.select(qTInterface).from(qTInterface)
				.orderBy(qTInterface.createdTime.desc()).fetch();
		if (StringUtils.isBlank(projectId)) {
			return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "数据获取成功!", allinterfaces);
		}
		// 去除当前项目下的接口
		interfaces.removeAll(interfaces);
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "数据获取成功!", interfaces);
	}

	@ApiOperation(value = "根据参数模板（json）获取key-value", notes = "根据参数模板（json）获取key-value")
	@PostMapping("/jsonFormat")
	public ResultDto<List<ParamsDto>> jsonFormat(String paramJson) {
		if (StringUtils.isBlank(paramJson)) {
			return new ResultDto(Constant.ResultCode.ERROR_CODE, "参数为空");
		}
		String type = PlatformUtil.strIsJsonOrXml(paramJson);
		if(Constant.ParamFormatType.XML.getType().equals(type)) {
			paramJson = niFiRequestUtil.xml2json(paramJson);
		}
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "", PlatformUtil.jsonFormat(paramJson));
	}

	@ApiOperation(value = "根据系统获取标准接口(新增接口)")
	@GetMapping("/getInterBySys")
	public ResultDto<List<TInterface>> getInterBySys(
			@ApiParam(value = "系统id") @RequestParam(value = "sysId", required = true) String sysId,
			@ApiParam(value = "接口分类id") @RequestParam(value = "typeId", required = false) String typeId) {
		ArrayList<Predicate> list = new ArrayList<>();
		if (StringUtils.isNotEmpty(sysId)) {
			list.add(qTInterface.sysId.eq(sysId));
		}
		if (StringUtils.isNotEmpty(typeId)) {
			list.add(qTInterface.typeId.eq(typeId));
		}
		List<TInterface> interfaces = sqlQueryFactory.select(qTInterface).from(qTInterface)
				.where(list.toArray(new Predicate[list.size()])).fetch();
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "根据系统id获取标准接口成功", interfaces);
	}

	/**
	 * 根据接口名获取标准接口
	 * 
	 * @param interfaceName
	 * @return
	 */
	private TInterface getInterfaceByName(String interfaceName) {
		if (StringUtils.isBlank(interfaceName)) {
			return null;
		}
		return sqlQueryFactory.select(qTInterface).from(qTInterface).where(qTInterface.interfaceName.eq(interfaceName))
				.fetchFirst();
	}

	/**
	 * 根据接口方法获取标准接口
	 * @param interfaceUrl
	 * @return
	 */
	private TInterface getInterfaceByUrl(String interfaceUrl , String sysId){
		if (StringUtils.isBlank(interfaceUrl)) {
			return null;
		}
		return sqlQueryFactory.select(qTInterface).from(qTInterface).where(qTInterface.interfaceUrl.eq(interfaceUrl).and(qTInterface.sysId.eq(sysId)))
				.fetchFirst();
	}
	/**
	 * 根据系统id获取所有接口信息
	 *
	 * @param sysId
	 * @return
	 */
	public List<TInterface> getObjBySysId(String sysId) {
		List<TInterface> list = sqlQueryFactory.select(qTInterface).from(qTInterface).where(qTInterface.sysId.eq(sysId))
				.fetch();
		return list;
	}

	@ApiOperation(value = "被请求方接口调试数据获取")
	@PostMapping("/getInterfaceDebugger")
	public ResultDto<String> getInterfaceDebugger(String interfaceId) {
		if (StringUtils.isBlank(interfaceId)) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "请求方接口id必传");
		}
		try {
			// 获取入参列表
//			List<String> paramNames = sqlQueryFactory.select(qTInterfaceParam.paramName).from(qTInterfaceParam)
//					.where(qTInterfaceParam.interfaceId.eq(interfaceId)
//							.and(qTInterfaceParam.paramInOut.eq(Constant.ParmInOut.IN)))
//					.fetch();
			TInterface inter = sqlQueryFactory.select(qTInterface).from(qTInterface).where(qTInterface.id.eq(interfaceId)).fetchFirst();
			if(inter == null) {
				return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "系统接口不存在，无法获取入参信息，请检查接口配置");
			}
			String paramJson = inter.getInParamFormat();
			if("2".equals(inter.getInParamFormatType())) {
				paramJson = niFiRequestUtil.xml2json(paramJson);
			}
			return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "JSON格式入参模板获取成功!", paramJson);
		} catch (Exception e) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "厂商接口调试数据失败!");
		}
	}

	@ApiOperation(value = "被请求方接口调试接口")
	@PostMapping("/interfaceDebugger")
	public ResultDto<Map> interfaceDebugger(@RequestBody JoltDebuggerDto dto) {
		// 校验参数是否完整
		ValidationResult validationResult = validatorHelper.validate(dto);
		if (validationResult.isHasErrors()) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, validationResult.getErrorMsg());
		}
		if(dto.getJolt() == null && dto.getJslt() == null) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "jolt脚本和jslt脚本参数不能都为空");
		}
		if(dto.getJolt() != null && dto.getJslt() != null) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "jolt脚本和jslt脚本参数不能同时存在");
		}
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "", niFiRequestUtil.joltDebugger(dto));
	}

	@ApiOperation(value = "根据类型id获取关联接口", notes = "根据类型id获取关联接口")
	@GetMapping("/getInterfaceListById/{typeId}")
	public ResultDto getInterfaceListById(@ApiParam(value = "类型id") @PathVariable String typeId) {
		try {
			ArrayList<Predicate> list = new ArrayList<>();
			list.add(qTInterface.typeId.eq(typeId));
			list.add(qTType.type.eq(1));
			List<TInterface> queryResults = sqlQueryFactory.select(qTInterface).from(qTInterface).leftJoin(qTType)
					.on(qTInterface.typeId.eq(qTType.id)).where(list.toArray(new Predicate[list.size()])).fetch();
			if (queryResults.size() > 0) {
				return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "该类型有关联的接口!");
			}
			return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "该类型没有关联的接口!");
		} catch (Exception e) {
			logger.error("根据类型id获取关联接口失败! MSG:{}", ExceptionUtil.dealException(e));
			e.printStackTrace();
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "根据类型id获取关联接口失败!");
		}
	}

	@ApiOperation(value = "测试数据库连接接口")
	@PostMapping("/testDbUrl")
	public ResultDto<String> testDbUrl(@RequestBody DbUrlTestDto dto) {
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "", niFiRequestUtil.testDbUrl(dto));
	}

	@ApiOperation(value = "导出选中的接口转换相关配置")
	@GetMapping("/downloadInterfaceConfigs/{Ids}")
	public void getSqlConfig(HttpServletResponse response,@ApiParam("接口Id")  @PathVariable String Ids) {
		String[] businessInterfaceIds = Ids.split(",");
		StringBuilder sqlStringBuffer = new StringBuilder();
		this.getResourcesByBizInterfaceIds(Arrays.asList(businessInterfaceIds) , sqlStringBuffer);
		
		String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
		String sqlName = "interface_" + dateStr + ".sql";
		try {
			response.setContentType("application/x-msdownload");
			response.setHeader("content-disposition", "attachment;filename=" + URLEncoder.encode("interface_" + dateStr + ".zip", "utf-8"));

			ZipOutputStream zos = new ZipOutputStream(response.getOutputStream());
			BufferedOutputStream bos = new BufferedOutputStream(zos);

			String fileName = sqlName; // 每个zip文件名
			byte[] file = sqlStringBuffer.toString().getBytes(StandardCharsets.UTF_8); // 这个zip文件的字节

			BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(file));
			zos.putNextEntry(new ZipEntry(fileName));

			int len = 0;
			byte[] buf = new byte[10 * 1024];
			while ((len = bis.read(buf, 0, buf.length)) != -1) {
				bos.write(buf, 0, len);
			}
			bis.close();
			bos.flush();
			bos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void getResourcesByBizInterfaceIds(List<String> ids , StringBuilder sqlStringBuffer) {
		List<String> interfaceIds = new ArrayList<>();
		List<String> sysConfigIds = new ArrayList<>();
		List<String> pluginIds = new ArrayList<>();
		List<String> sysIds = new ArrayList<>();
		List<String> driverIds = new ArrayList<>();
		List<String> hospitalIds = new ArrayList<>();
		List<String> platformIds = new ArrayList<>();
		List<String> projectIds = new ArrayList<>();

		List<Path<?>> lists = new ArrayList<>();
		lists.addAll(Arrays.asList(qTBusinessInterface.all()));
		lists.add(qTSysConfig.platformId);
		for (String businessInterfaceId : ids) {
			String requestInterfaceId = sqlQueryFactory.select(qTBusinessInterface.requestInterfaceId).from(qTBusinessInterface)
					.where(qTBusinessInterface.id.eq(businessInterfaceId)).fetchFirst();
			if(StringUtils.isNotBlank(requestInterfaceId)) {
				List<TBusinessInterface> tBusinessInterfaces = sqlQueryFactory.select(Projections.bean(TBusinessInterface.class , lists.toArray(new Path[0]))).from(qTBusinessInterface)
						.join(qTSysConfig).on(qTSysConfig.id.eq(qTBusinessInterface.requestSysconfigId))
						.where(qTBusinessInterface.requestInterfaceId.eq(requestInterfaceId)).fetch();
				for (TBusinessInterface tBusinessInterface : tBusinessInterfaces) {
					interfaceIds.add(tBusinessInterface.getRequestInterfaceId());
					sysConfigIds.add(tBusinessInterface.getRequestSysconfigId());
					sysConfigIds.add(tBusinessInterface.getRequestedSysconfigId());
					pluginIds.add(tBusinessInterface.getPluginId());
					platformIds.add(tBusinessInterface.getPlatformId());
					String mocktpl = tBusinessInterface.getMockTemplate();
					if(StringUtils.isBlank(mocktpl)) {
						mocktpl = tBusinessInterface.getOutParamFormat();
					}
					sqlStringBuffer.append("REPLACE INTO `t_business_interface` (`ID`, `REQUEST_SYSCONFIG_ID`, " +
							"`REQUEST_INTERFACE_ID`, `REQUESTED_SYSCONFIG_ID`, `BUSINESS_INTERFACE_NAME`, `REQUEST_TYPE`, " +
							"`REQUEST_CONSTANT`, `INTERFACE_TYPE`, `PLUGIN_ID`, `IN_PARAM_FORMAT`, `IN_PARAM_SCHEMA`, `IN_PARAM_TEMPLATE_TYPE`, " +
							"`IN_PARAM_TEMPLATE`, `IN_PARAM_FORMAT_TYPE`, `OUT_PARAM_FORMAT`, `OUT_PARAM_SCHEMA`, `OUT_PARAM_TEMPLATE_TYPE`, " +
							"`OUT_PARAM_TEMPLATE`, `OUT_PARAM_FORMAT_TYPE`, `MOCK_TEMPLATE`, `MOCK_STATUS`, `STATUS`, `EXC_ERR_STATUS`, " +
							"`EXC_ERR_ORDER`, `MOCK_IS_USE`, `CREATED_BY`, `CREATED_TIME`, `UPDATED_BY`, `UPDATED_TIME`, `ASYNC_FLAG`, " +
							"`INTERFACE_SLOW_FLAG`) VALUES ('" + tBusinessInterface.getId() + "', '" + tBusinessInterface.getRequestSysconfigId() + "', '" + tBusinessInterface.getRequestInterfaceId() + "', " +
							"'" + tBusinessInterface.getRequestedSysconfigId() + "', '" + PlatformUtil.escapeSqlSingleQuotes(tBusinessInterface.getBusinessInterfaceName())  + "', '" + tBusinessInterface.getRequestType() + "', '" + PlatformUtil.escapeSqlSingleQuotes(tBusinessInterface.getRequestConstant()) + "', " +
							"'" + tBusinessInterface.getInterfaceType() + "', '" + tBusinessInterface.getPluginId() + "', '" + PlatformUtil.escapeSqlSingleQuotes(tBusinessInterface.getInParamFormat()) + "', '" + tBusinessInterface.getInParamSchema() + "', " +
							"" + tBusinessInterface.getInParamTemplateType() + ", '" + PlatformUtil.escapeSqlSingleQuotes(tBusinessInterface.getInParamTemplate()) + "', '" + tBusinessInterface.getInParamFormatType() + "', '" + tBusinessInterface.getOutParamFormat() + "', " +
							"'" + tBusinessInterface.getOutParamSchema() + "', " + tBusinessInterface.getOutParamTemplateType() + ", '" + PlatformUtil.escapeSqlSingleQuotes(tBusinessInterface.getOutParamTemplate()) + "', '" + tBusinessInterface.getOutParamFormatType() + "', " +
							"'" + PlatformUtil.escapeSqlSingleQuotes(mocktpl) + "', '" + tBusinessInterface.getMockStatus() + "', '" + tBusinessInterface.getStatus() + "', '" + tBusinessInterface.getExcErrStatus() + "', " +
							"" + tBusinessInterface.getExcErrOrder() + ", " + tBusinessInterface.getMockIsUse() + ", 'admin', now() , 'admin', now() , " + tBusinessInterface.getAsyncFlag() + ", " + tBusinessInterface.getInterfaceSlowFlag() + "); \n");
					sqlStringBuffer.append("END_OF_SQL\n");
				}
			}
		}
		List<TPlatform> tPlatforms = sqlQueryFactory.select(qTPlatform).from(qTPlatform).where(qTPlatform.id.in(platformIds)).fetch();
		for(TPlatform tp : tPlatforms) {
			sqlStringBuffer.append("REPLACE INTO `t_platform` (`ID`, `PROJECT_ID`, `PLATFORM_NAME`, `PLATFORM_CODE`, " +
					"`PLATFORM_STATUS`, `PLATFORM_TYPE`, `ETL_SERVER_URL`, `ETL_USER`, `ETL_PWD`," +
					" `CREATED_BY`, `CREATED_TIME`, `UPDATED_BY`, `UPDATED_TIME`) VALUES ('" + tp.getId() + "', 'newProjectId_"+tp.getProjectId()+"', '" + PlatformUtil.escapeSqlSingleQuotes(tp.getPlatformName()) + "', '" + tp.getPlatformCode() + "', " +
					"'" + tp.getPlatformStatus() + "', '" + tp.getPlatformType() + "', '" + tp.getEtlServerUrl() + "', '" + tp.getEtlUser() + "', '" + tp.getEtlPwd() +
					"', 'admin', now() , 'admin', now());\n");
			sqlStringBuffer.append("END_OF_SQL\n");
			projectIds.add(tp.getProjectId());
		}
		
		List<TProject> tProjs = sqlQueryFactory.select(qTProject).from(qTProject).where(qTProject.id.in(projectIds)).fetch();
		for(TProject tproj : tProjs) {
			sqlStringBuffer.append("REPLACE INTO `t_project` (`ID`, `PROJECT_NAME`, `PROJECT_CODE`, `PROJECT_STATUS`, `PROJECT_TYPE`, " +
					" `CREATED_BY`, `CREATED_TIME`, `UPDATED_BY`, `UPDATED_TIME`) VALUES ('" + tproj.getId() + "','"+ tproj.getProjectName()+ "', '" + tproj.getProjectCode() + "', '" + tproj.getProjectStatus() + "', " +
					"'" + tproj.getProjectType() + "','admin', now() , 'admin', now());\n");
			sqlStringBuffer.append("END_OF_SQL\n");
		}
		
		List<TInterface> tInterfaces = sqlQueryFactory.select(qTInterface).from(qTInterface).where(qTInterface.id.in(interfaceIds)).fetch();
		for (TInterface tInterface : tInterfaces) {
			sqlStringBuffer.append("REPLACE INTO `t_interface` (`ID`, `SYS_ID`, `INTERFACE_NAME`, `TYPE_ID`, " +
					"`INTERFACE_URL`, `IN_PARAM_FORMAT`, `OUT_PARAM_FORMAT`, `PARAM_OUT_STATUS`, `PARAM_OUT_STATUS_SUCCESS`," +
					" `CREATED_BY`, `CREATED_TIME`, `UPDATED_BY`, `UPDATED_TIME`, `IN_PARAM_SCHEMA`, `IN_PARAM_FORMAT_TYPE`, " +
					"`OUT_PARAM_SCHEMA`, `OUT_PARAM_FORMAT_TYPE`) VALUES ('" + tInterface.getId() + "', '" + tInterface.getSysId() + "', '" + PlatformUtil.escapeSqlSingleQuotes(tInterface.getInterfaceName()) + "', '" + tInterface.getTypeId() + "', " +
					"'" + tInterface.getInterfaceUrl() + "', '" + PlatformUtil.escapeSqlSingleQuotes(tInterface.getInParamFormat()) + "', '" + PlatformUtil.escapeSqlSingleQuotes(tInterface.getOutParamFormat()) + "', '" + tInterface.getParamOutStatus() + "', '" + tInterface.getParamOutStatusSuccess() +
					"', 'admin', now() , 'admin', now(), '" + tInterface.getInParamSchema() + "', '" + tInterface.getInParamFormatType() + "', '" + tInterface.getOutParamSchema() + "', '" + tInterface.getOutParamFormatType() + "');\n");
			sqlStringBuffer.append("END_OF_SQL\n");
		}
		List<TInterfaceParam> tInterfaceParams = sqlQueryFactory.select(qTInterfaceParam).from(qTInterfaceParam).where(qTInterfaceParam.interfaceId.in(interfaceIds)).fetch();
		for (TInterfaceParam tInterfaceParam : tInterfaceParams) {
			sqlStringBuffer.append("REPLACE INTO `t_interface_param` (`ID`, `PARAM_NAME`, `PARAM_INSTRUCTION`, `INTERFACE_ID`, `PARAM_TYPE`, `PARAM_LENGTH`, `PARAM_IN_OUT`, `CREATED_BY`, `CREATED_TIME`, " +
					"`UPDATED_BY`, `UPDATED_TIME`) VALUES ('" + tInterfaceParam.getId() + "', '" + tInterfaceParam.getParamName() + "', '" + tInterfaceParam.getParamInstruction() + "', '" + tInterfaceParam.getInterfaceId() + "'," +
					" '" + tInterfaceParam.getParamType() + "', " + tInterfaceParam.getParamLength() + ", '" + tInterfaceParam.getParamInOut() + "', 'admin', now() , 'admin', now());\n");
			sqlStringBuffer.append("END_OF_SQL\n");
		}

		List<TSysConfig> tSysConfigs = sqlQueryFactory.select(qTSysConfig).from(qTSysConfig).where(qTSysConfig.id.in(sysConfigIds)).fetch();
		for (TSysConfig sysConfig : tSysConfigs) {
			sysIds.add(sysConfig.getSysId());
			sqlStringBuffer.append("REPLACE INTO `t_sys_config` (`ID`, `PROJECT_ID`, `PLATFORM_ID`, `SYS_ID`, `SYS_CONFIG_TYPE`, `HOSPITAL_CONFIGS`, `VERSION_ID`, `CONNECTION_TYPE`, `ADDRESS_URL`, `ENDPOINT_URL`," +
					" `NAMESPACE_URL`, `DATABASE_NAME`, `DATABASE_URL`, `DATABASE_TYPE`, `DATABASE_DRIVER`, `DRIVER_URL`, `JSON_PARAMS`, `USER_NAME`, `USER_PASSWORD`, `CREATED_BY`, `CREATED_TIME`, `UPDATED_BY`, `UPDATED_TIME`, " +
					"`INNER_IDX`) VALUES ('" + sysConfig.getId() + "', 'newProjectId_"+ sysConfig.getProjectId()+"', '"+ sysConfig.getPlatformId() +"', '" + sysConfig.getSysId() + "', " + sysConfig.getSysConfigType() + ", " +
					sysConfig.getHospitalConfigs() + ", '" + sysConfig.getVersionId() + "', '" + sysConfig.getConnectionType() + "', '" + sysConfig.getAddressUrl() + "', '" + sysConfig.getEndpointUrl() + "', " +
					"'" + sysConfig.getNamespaceUrl() + "', '" + sysConfig.getDatabaseName() + "', '" + sysConfig.getDatabaseUrl() + "', '" + sysConfig.getDatabaseType() + "', '" + sysConfig.getDatabaseDriver() + "', " +
					"'" + sysConfig.getDriverUrl() + "', '" + sysConfig.getJsonParams() + "', '" + sysConfig.getUserName() + "', '" + sysConfig.getUserPassword() + "','admin', now() , 'admin', now(), '" + sysConfig.getInnerIdx() + "');\n");
			sqlStringBuffer.append("END_OF_SQL\n");
		}
		List<TPlugin> tPlugins = sqlQueryFactory.select(qTPlugin).from(qTPlugin).where(qTPlugin.id.in(pluginIds)).fetch();
		for (TPlugin tPlugin : tPlugins) {
			sqlStringBuffer.append("REPLACE INTO `t_plugin` (`ID`, `PLUGIN_NAME`, `PLUGIN_CODE`, `TYPE_ID`, `PLUGIN_INSTRUCTION`, `PLUGIN_CONTENT`, `CREATED_BY`, `CREATED_TIME`, `UPDATED_BY`, `UPDATED_TIME`, `DEPENDENT_PATH`) " +
					"VALUES ('" + tPlugin.getId() + "', '" + PlatformUtil.escapeSqlSingleQuotes(tPlugin.getPluginName()) + "', '" + tPlugin.getPluginCode() + "', '" + tPlugin.getTypeId() + "', '" + tPlugin.getPluginInstruction() + "', '" + PlatformUtil.escapeSqlSingleQuotes(tPlugin.getPluginContent()) + "', 'admin', now() , 'admin', now(), '" + tPlugin.getDependentPath() + "');\n");
			sqlStringBuffer.append("END_OF_SQL\n");
		}
		List<TSys> tSyss = sqlQueryFactory.select(qTSys).from(qTSys).where(qTSys.id.in(sysIds)).fetch();
		for (TSys tSys : tSyss) {
			sqlStringBuffer.append("REPLACE INTO `t_sys` (`ID`, `SYS_NAME`, `SYS_CODE`, `IS_VALID`, `CREATED_BY`, `CREATED_TIME`, `UPDATED_BY`, `UPDATED_TIME`) VALUES " +
					"('" + tSys.getId() + "', '" + tSys.getSysName() + "', '" + tSys.getSysCode() + "', '" + tSys.getIsValid() + "', 'admin', now() , 'admin', now());\n");
			sqlStringBuffer.append("END_OF_SQL\n");
		}
		List<TSysDriveLink> tSysDriveLinks = sqlQueryFactory.select(qTSysDriveLink).from(qTSysDriveLink).where(qTSysDriveLink.sysId.in(sysIds)).fetch();
		for (TSysDriveLink tSysDriveLink : tSysDriveLinks) {
			driverIds.add(tSysDriveLink.getDriveId());
			sqlStringBuffer.append("REPLACE INTO `t_sys_drive_link` (`ID`, `SYS_ID`, `DRIVE_ID`, `DRIVE_ORDER`, `CREATED_BY`, `CREATED_TIME`, `UPDATED_BY`, `UPDATED_TIME`) VALUES " +
					"('" + tSysDriveLink.getId() + "', '" + tSysDriveLink.getSysId() + "', '" + tSysDriveLink.getDriveId() + "', " + tSysDriveLink.getDriveOrder() + ", 'admin', now() , 'admin', now());\n");
			sqlStringBuffer.append("END_OF_SQL\n");
		}
		List<TDrive> tDrives = sqlQueryFactory.select(qTDrive).from(qTDrive).where(qTDrive.id.in(driverIds)).fetch();
		for (TDrive tDrive : tDrives) {
			sqlStringBuffer.append("REPLACE INTO `t_drive` (`ID`, `DRIVE_NAME`, `DRIVE_CODE`, `TYPE_ID`, `DRIVE_INSTRUCTION`, `DRIVE_CONTENT`, `CREATED_BY`, `CREATED_TIME`, `UPDATED_BY`, `UPDATED_TIME`, `DRIVE_CALL_TYPE`, `DEPENDENT_PATH`) VALUES " +
					"('" + tDrive.getId() + "', '" + PlatformUtil.escapeSqlSingleQuotes(tDrive.getDriveName()) + "', '" + tDrive.getDriveCode() + "', '" + tDrive.getTypeId() + "', '" + tDrive.getDriveInstruction() + "', '" + PlatformUtil.escapeSqlSingleQuotes(tDrive.getDriveContent()) + "', 'admin', now() , 'admin', now(), '" + tDrive.getDriveCallType() + "', '" + tDrive.getDependentPath() + "');\n");
			sqlStringBuffer.append("END_OF_SQL\n");
		}
		List<TSysHospitalConfig> tSysHospitalConfigs = sqlQueryFactory.select(qTSysHospitalConfig).from(qTSysHospitalConfig).where(qTSysHospitalConfig.sysConfigId.in(sysConfigIds)).fetch();
		for (TSysHospitalConfig tSysHospitalConfig : tSysHospitalConfigs) {
			hospitalIds.add(tSysHospitalConfig.getHospitalId());
			sqlStringBuffer.append("REPLACE INTO `t_sys_hospital_config` (`id`, `sys_config_id`, `hospital_id`, `hospital_code`, `CREATED_BY`, `CREATED_TIME`, `UPDATED_BY`, `UPDATED_TIME`) VALUES " +
					"('" + tSysHospitalConfig.getId() + "', '" + tSysHospitalConfig.getSysConfigId() + "', '" + tSysHospitalConfig.getHospitalId() + "', '" + tSysHospitalConfig.getHospitalCode() + "', 'admin', now() , 'admin', now());\n");
			sqlStringBuffer.append("END_OF_SQL\n");
		}
		List<THospital> tHospitals = sqlQueryFactory.select(qTHospital).from(qTHospital).where(qTHospital.id.in(hospitalIds)).fetch();
		for (THospital tHospital : tHospitals) {
			sqlStringBuffer.append("REPLACE INTO `t_hospital` (`ID`, `HOSPITAL_NAME`, `STATUS`, `AREA_ID`, `CREATED_BY`, `CREATED_TIME`, `UPDATED_BY`, `UPDATED_TIME`) VALUES " +
					"('" + tHospital.getId() + "', '" + tHospital.getHospitalName() + "', '" + tHospital.getStatus() + "', '" + tHospital.getAreaId() + "', 'admin', now() , 'admin', now());\n");
			sqlStringBuffer.append("END_OF_SQL\n");
		}
	}
	
	
    @PostMapping(path = "/uploadInterFaceSql/{projectId}")
    public ResultDto<String> uploadInterFaceSql(@PathVariable String projectId,@RequestParam("sqlFiles") MultipartFile[] sqlFiles) {
         //校验是否获取到登录用户
        String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
        if (org.apache.commons.lang3.StringUtils.isBlank(loginUserName)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!", "没有获取到登录用户!");
        }
        //获取数据库连接
        Connection connection = sqlQueryFactory.getConnection();
        Statement statement=null;
        StringBuilder message=new StringBuilder();
        try {
            statement = connection.createStatement();
            //判断是否获取到文件
            if (sqlFiles == null || sqlFiles.length == 0) {
                return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到上传文件!", "没有获取到上传文件!");
            }
            //sql分批sql语句
            InputStream is=null;
            int insetNum = 0;
            for (MultipartFile file : sqlFiles) {
                try{
                    //获取字符缓冲流
                    is =file.getInputStream();
                    InputStreamReader inputStreamReader = new InputStreamReader(is , StandardCharsets.UTF_8);
//                  int len;
                    StringBuilder sql = new StringBuilder();
                    connection.setAutoCommit(false);//不自动提交
//                  byte [] bytes=new byte[1024];
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);//缓存数据用于读取
                    String lineText = "";
                    while ((lineText = bufferedReader.readLine()) != null) {
                      sql.append(lineText).append("\r\n");
                    }
                    //将sys_config表中的平台id以及项目id进行替换
                    sql=new StringBuilder(sql.toString().replaceAll("'newProjectId_\\d+'", "'"+projectId+"'"));
                    String [] sqls=sql.toString().split("END_OF_SQL");
                    for(String str:sqls){
                        if(str.trim().startsWith("INSERT") || str.trim().startsWith("REPLACE"))
                            statement.addBatch(str);
                    }
                    //事务提交，整体成功或失败
                    statement.executeBatch();
                    connection.commit();
                    //清除SQL语句
                    statement.clearBatch();
                    insetNum++;
                    is.close();
                }catch (Exception e){
                    connection.rollback();
                    statement.clearBatch();
                    if(is!=null)
                        is.close();
                    message.append(e.getMessage());
                }
            }
            if (insetNum==sqlFiles.length) {
                return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "sql脚本全部执行成功", insetNum+"");
            } else {
                return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "sql脚本部分执行错误"+message,insetNum+"");
            }
        } catch (Exception e) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "执行sql脚本失败", e.getLocalizedMessage());
        }finally {
            try{
                if (connection != null) {
                    connection.close();
                }
                if (statement != null) {
                    statement.close();
                }
            }catch (SQLException sqlException){
                sqlException.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
    	String sql = "REPLACE INTO `t_platform` (`ID`, `PROJECT_ID`, `PLATFORM_NAME`, `PLATFORM_CODE`, `PLATFORM_STATUS`, `PLATFORM_TYPE`, `ETL_SERVER_URL`, `ETL_USER`, `ETL_PWD`, `CREATED_BY`, `CREATED_TIME`, `UPDATED_BY`, `UPDATED_TIME`) VALUES ('61195408071721089', 'newProjectId_48109769075982460', '孙思邈医院-智联网', 'ssmyyzhmz', '1', '1', 'null', 'admin', 'wPjVysmnNUWL9sKMJgyKzQ==', 'admin', now() , 'admin', now());";
    	String replaced = sql.replaceAll("'newProjectId_\\d+'", "'11111111'");
    	System.out.println(replaced);
    }
}
