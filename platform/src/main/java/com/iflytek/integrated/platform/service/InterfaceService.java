package com.iflytek.integrated.platform.service;

import static com.iflytek.integrated.platform.entity.QTBusinessInterface.qTBusinessInterface;
import static com.iflytek.integrated.platform.entity.QTHospital.qTHospital;
import static com.iflytek.integrated.platform.entity.QTInterface.qTInterface;
import static com.iflytek.integrated.platform.entity.QTInterfaceParam.qTInterfaceParam;
import static com.iflytek.integrated.platform.entity.QTPlatform.qTPlatform;
import static com.iflytek.integrated.platform.entity.QTProject.qTProject;
import static com.iflytek.integrated.platform.entity.QTSys.qTSys;
import static com.iflytek.integrated.platform.entity.QTSysConfig.qTSysConfig;
import static com.iflytek.integrated.platform.entity.QTSysHospitalConfig.qTSysHospitalConfig;
import static com.iflytek.integrated.platform.entity.QTType.qTType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
import com.iflytek.integrated.platform.dto.InDebugResDto;
import com.iflytek.integrated.platform.dto.InterfaceDebugDto;
import com.iflytek.integrated.platform.dto.InterfaceDto;
import com.iflytek.integrated.platform.dto.JoltDebuggerDto;
import com.iflytek.integrated.platform.dto.MockTemplateDto;
import com.iflytek.integrated.platform.dto.ParamsDto;
import com.iflytek.integrated.platform.dto.RedisDto;
import com.iflytek.integrated.platform.dto.RedisKeyDto;
import com.iflytek.integrated.platform.entity.TBusinessInterface;
import com.iflytek.integrated.platform.entity.TInterface;
import com.iflytek.integrated.platform.entity.TInterfaceParam;
import com.iflytek.integrated.platform.entity.TSysConfig;
import com.iflytek.integrated.platform.entity.TType;
import com.iflytek.integrated.platform.utils.NiFiRequestUtil;
import com.iflytek.integrated.platform.utils.PlatformUtil;
import com.iflytek.medicalboot.core.id.BatchUidService;
import com.querydsl.core.QueryResults;
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
			MockTemplateDto dto = new MockTemplateDto(businessInterface.getId(),
					// 如果mock模板为空，取出参的格式，作为初始的mock模板
					StringUtils.isNotBlank(businessInterface.getMockTemplate()) ? businessInterface.getMockTemplate()
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
		String rtnStr = "";
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
			rtnStr += dto.getId() + ",";
		}
		rtnStr = StringUtils.isBlank(rtnStr) ? null : rtnStr.substring(0, rtnStr.length() - 1);
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "保存mock模板成功!", new RedisDto(rtnStr).toString());
	}

	@ApiOperation(value = "请求方接口调试数据获取", notes = "请求方接口调试数据获取")
	@GetMapping("/getInterfaceDebug")
	public ResultDto<InDebugResDto> getInterfaceDebug(
			@ApiParam(value = "接口转换配置id") @RequestParam(value = "id", required = true) String id) {
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
			if ("2".equals(businessInterface.getSysIntfInParamFormatType())) {
				String inparamFormat = sqlQueryFactory.select(qTInterface.inParamFormat).from(qTInterface).where(
						qTInterface.id.eq(interfaceId).and(qTInterface.sysId.eq(businessInterface.getRequestSysId())))
						.fetchFirst();
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
				resDto.setSysIntfParamFormatType("3");
			}
			resDto.setFuncode(businessInterface.getInterfaceUrl());
			resDto.setProductcode(businessInterface.getSysCode());
			resDto.setProjectcode(businessInterface.getProjectCode());
			resDto.setOrgids(hospitalCodes);
			return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取接口调试显示数据成功!", resDto);
		} catch (Exception e) {
			logger.error("获取接口调试显示数据失败! MSG:{}", ExceptionUtil.dealException(e));
			e.printStackTrace();
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "获取接口调试显示数据失败!");
		}
	}

	@PostMapping("/interfaceDebug")
	@ApiOperation(value = "请求方接口调试", notes = "请求方接口调试")
	public ResultDto<String> interfaceDebug(@RequestBody InterfaceDebugDto degubDto) {
		String result = "";
		if ("2".equals(degubDto.getSysIntfParamFormatType())) {
			String wsdlUrl = degubDto.getWsdlUrl();
			String methodName = degubDto.getWsOperationName();
			String funcode = degubDto.getFuncode();
			String param = degubDto.getFormat();
			result = PlatformUtil.invokeWsService(wsdlUrl, methodName, funcode, param);
		} else {
			result = niFiRequestUtil.interfaceDebug(degubDto.getFormat());
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
		String id = dto.getId();
		if (StringUtils.isBlank(id)) {
			return this.saveInterface(dto, loginUserName);
		}
		return this.updateInterface(dto, loginUserName);
	}

	/** 新增标准接口 */
	private ResultDto saveInterface(InterfaceDto dto, String loginUserName) {
		String interfaceName = dto.getInterfaceName();
		if (null != this.getInterfaceByName(interfaceName)) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "该接口名已存在!", "该接口名已存在!");
		}
		// 出参
		List<TInterfaceParam> outParamList = dto.getOutParamList();
		if (CollectionUtils.isEmpty(outParamList)) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "出参不能为空!", "出参不能为空!");
		}

		// 校验出入参格式字符串是否为json或者xml
		// 入参格式非必填
		String inParamFormat = dto.getInParamFormat();
		// 出参格式必填
		String outParamFormat = dto.getOutParamFormat();
		if (StringUtils.isNotBlank(inParamFormat)) {
			PlatformUtil.strIsJsonOrXml(inParamFormat);
		}
		PlatformUtil.strIsJsonOrXml(outParamFormat);
		// 新增标准接口
		String interfaceId = batchUidService.getUid(qTInterface.getTableName()) + "";
		TInterface ti = new TInterface();
		ti.setId(interfaceId);
		ti.setInParamFormatType(dto.getInParamFormatType());
		ti.setOutParamFormatType(dto.getOutParamFormatType());
		ti.setSysId(dto.getSysId());
		ti.setInterfaceName(dto.getInterfaceName());
		ti.setTypeId(dto.getTypeId());
		ti.setInterfaceUrl(dto.getInterfaceUrl());
		ti.setInParamFormat(inParamFormat);
		ti.setOutParamFormat(outParamFormat);
		ti.setCreatedTime(new Date());
		ti.setCreatedBy(loginUserName);

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
		niFiRequestUtil.generateSchemaToInterface(ti);
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
		// 传入标准接口名
		String interfaceName = dto.getInterfaceName();
		// 查询新接口名是否已存在
		tf = sqlQueryFactory.select(qTInterface).from(qTInterface)
				.where(qTInterface.interfaceName.eq(interfaceName).and(qTInterface.id.notEqualsIgnoreCase(id)))
				.fetchOne();
		if (tf != null) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "该接口名已存在!", "该接口名已存在!");
		}
		// 出参
		List<TInterfaceParam> outParamList = dto.getOutParamList();
		if (CollectionUtils.isEmpty(outParamList)) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "出参不能为空!", "出参不能为空!");
		}

		String interfaceTypeId = dto.getTypeId();
		String interfaceUrl = dto.getInterfaceUrl();
		// 校验出入参格式字符串是否为json或者xml
		String inParamFormat = dto.getInParamFormat();
		String outParamFormat = dto.getOutParamFormat();
		String inParamFormatType = dto.getInParamFormatType();
		String outParamFormatType = dto.getOutParamFormatType();

		if (StringUtils.isNotBlank(inParamFormat)) {
			PlatformUtil.strIsJsonOrXml(inParamFormat);
		}
		PlatformUtil.strIsJsonOrXml(outParamFormat);

		// 修改标准接口信息
		long execute = sqlQueryFactory.update(qTInterface).set(qTInterface.interfaceName, interfaceName)
				.set(qTInterface.typeId, interfaceTypeId).set(qTInterface.interfaceUrl, interfaceUrl)
				.set(qTInterface.inParamFormat, inParamFormat).set(qTInterface.outParamFormat, outParamFormat)
				.set(qTInterface.inParamFormatType, inParamFormatType)
				.set(qTInterface.outParamFormatType, outParamFormatType).set(qTInterface.updatedTime, new Date())
				.set(qTInterface.paramOutStatus, "").set(qTInterface.paramOutStatusSuccess, "")
				.set(qTInterface.updatedBy, loginUserName).where(qTInterface.id.eq(id)).execute();
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
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "标准接口修改成功!", new RedisDto(id).toString());
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
				.select(Projections.bean(TInterface.class, qTInterface.id, qTInterface.interfaceName,
						qTInterface.interfaceUrl, qTInterface.inParamFormat, qTInterface.outParamFormat,
						qTInterface.createdTime, qTInterface.typeId, qTType.typeName.as("interfaceTypeName"),
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
			@ApiParam(value = "页码", example = "1") @RequestParam(value = "pageNo", defaultValue = "1", required = false) Integer pageNo,
			@ApiParam(value = "每页大小", example = "10") @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
		// 获取接口配置列表信息
		QueryResults<TBusinessInterface> queryResults = businessInterfaceService.getInterfaceConfigureList(platformId,
				status, mockStatus, pageNo, pageSize);
		// 匹配列表展示信息
		List<TBusinessInterface> list = queryResults.getResults();
		if (CollectionUtils.isNotEmpty(list)) {
			for (TBusinessInterface tbi : list) {
				// 获取被请求方系统/接口
				if (StringUtils.isNotBlank(tbi.getBusinessInterfaceName())) {
					String[] bizNames = tbi.getBusinessInterfaceName().split(",");
					String bizSysInterfaces = "";
					for (String bizName : bizNames) {
						String sysConfigId = bizName.split("/")[0];
						String bizInterfaceName = bizName.split("/")[1];
						String sysInterface = sqlQueryFactory.select(qTSys.sysName.append("/").append(bizInterfaceName))
								.from(qTSysConfig).join(qTSys).on(qTSys.id.eq(qTSysConfig.sysId))
								.where(qTSysConfig.id.eq(sysConfigId)).fetchOne();
						bizSysInterfaces += sysInterface + ",";
					}
					if (StringUtils.isNotBlank(bizSysInterfaces)) {
						if (bizSysInterfaces.endsWith(",")) {
							bizSysInterfaces = bizSysInterfaces.substring(0, bizSysInterfaces.lastIndexOf(","));
						}
						tbi.setBusinessInterfaceName(bizSysInterfaces);
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
					TSysConfig config = sysConfigService.getOne(requestdSysconfigId);
					tb.setRequestedSysId(config.getSysId());
				}
			}
		}

		dto.setBusinessInterfaceList(tbiList);

		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取接口配置详情成功", dto);
	}

	@Transactional(rollbackFor = Exception.class)
	@ApiOperation(value = "新增/编辑接口配置", notes = "新增/编辑接口配置")
	@PostMapping("/saveAndUpdateInterfaceConfig")
	public ResultDto<String> saveAndUpdateInterfaceConfig(@RequestBody BusinessInterfaceDto dto) {
		if (dto == null) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "请求参数不能为空!");
		}
		// 校验是否获取到登录用户
		String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
		if (StringUtils.isBlank(loginUserName)) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!");
		}
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
	private ResultDto saveInterfaceConfig(BusinessInterfaceDto dto, String loginUserName) {
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
			// 获取schema
			niFiRequestUtil.generateSchemaToInterface(tbi);
			// 新增接口配置
			businessInterfaceService.post(tbi);
		}
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "新增接口配置成功", null);
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

		// 返回缓存接口配置id
		String rtnId = "";
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
				// 获取schema
				niFiRequestUtil.generateSchemaToInterface(tbi);
				// 新增接口配置
				long l = businessInterfaceService.put(tbi.getId(), tbi);
				if (l < 1) {
					throw new RuntimeException("修改新增接口配置信息失败!");
				}
				rtnId += tbi.getId() + ",";
			}
		}
		rtnId = StringUtils.isBlank(rtnId) ? null : rtnId.substring(0, rtnId.length() - 1);
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "编辑接口配置成功", new RedisDto(rtnId).toString());
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
		String rtnStr = "";
		if (CollectionUtils.isNotEmpty(list)) {
			for (TBusinessInterface obj : list) {
				rtnStr += obj.getId() + ",";
			}
		}
		rtnStr = StringUtils.isBlank(rtnStr) ? null : rtnStr.substring(0, rtnStr.length() - 1);
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
	public ResultDto<List<String>> getInterfaceDebugger(String interfaceId) {
		if (StringUtils.isBlank(interfaceId)) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "请求方接口id必传");
		}
		try {
			// 获取入参列表
			List<String> paramNames = sqlQueryFactory.select(qTInterfaceParam.paramName).from(qTInterfaceParam)
					.where(qTInterfaceParam.interfaceId.eq(interfaceId)
							.and(qTInterfaceParam.paramInOut.eq(Constant.ParmInOut.IN)))
					.fetch();
			return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "入参列表获取成功!", paramNames);
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

}
