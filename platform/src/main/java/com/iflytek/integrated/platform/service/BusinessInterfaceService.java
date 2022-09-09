package com.iflytek.integrated.platform.service;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.common.dto.TableData;
import com.iflytek.integrated.common.utils.ExceptionUtil;
import com.iflytek.integrated.common.validator.ValidationResult;
import com.iflytek.integrated.common.validator.ValidatorHelper;
import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.common.CacheDeleteService;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.platform.common.RedisService;
import com.iflytek.integrated.platform.dto.BusinessInterfaceDto;
import com.iflytek.integrated.platform.dto.CacheDeleteDto;
import com.iflytek.integrated.platform.dto.InDebugResDto;
import com.iflytek.integrated.platform.dto.InterfaceDebugDto;
import com.iflytek.integrated.platform.dto.JoltDebuggerDto;
import com.iflytek.integrated.platform.dto.MockTemplateDto;
import com.iflytek.integrated.platform.dto.RedisDto;
import com.iflytek.integrated.platform.dto.RedisKeyDto;
import com.iflytek.integrated.platform.entity.TBusinessInterface;
import com.iflytek.integrated.platform.entity.TDrive;
import com.iflytek.integrated.platform.entity.TInterface;
import com.iflytek.integrated.platform.entity.TInterfaceParam;
import com.iflytek.integrated.platform.entity.TPlugin;
import com.iflytek.integrated.platform.entity.TSys;
import com.iflytek.integrated.platform.entity.TSysDriveLink;
import com.iflytek.integrated.platform.entity.TSysRegistry;
import com.iflytek.integrated.platform.utils.NiFiRequestUtil;
import com.iflytek.integrated.platform.utils.PlatformUtil;
import com.iflytek.medicalboot.core.id.BatchUidService;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.core.types.dsl.StringTemplate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.iflytek.integrated.platform.entity.QTBusinessInterface.qTBusinessInterface;
import static com.iflytek.integrated.platform.entity.QTDrive.qTDrive;
import static com.iflytek.integrated.platform.entity.QTInterface.qTInterface;
import static com.iflytek.integrated.platform.entity.QTInterfaceParam.qTInterfaceParam;
import static com.iflytek.integrated.platform.entity.QTPlugin.qTPlugin;
import static com.iflytek.integrated.platform.entity.QTSys.qTSys;
import static com.iflytek.integrated.platform.entity.QTSysDriveLink.qTSysDriveLink;
import static com.iflytek.integrated.platform.entity.QTSysRegistry.qTSysRegistry;
import static com.iflytek.integrated.platform.entity.QTType.qTType;

/**
 * 对接接口配置
 *
 * @author weihe9
 * @date 2020/12/13 20:40
 */
@Service
@Api(tags = "系统服务管理")
@RestController
@RequestMapping("/{version}/pt/bussInterfaceManage")
public class BusinessInterfaceService extends BaseService<TBusinessInterface, String, StringPath> {

    private static final Logger logger = LoggerFactory.getLogger(BusinessInterfaceService.class);

    @Autowired
    private RedisService redisService;

    @Autowired
    private CacheDeleteService cacheDeleteService;

    @Autowired
    private BatchUidService batchUidService;
    @Autowired
    private NiFiRequestUtil niFiRequestUtil;
    @Autowired
    private ValidatorHelper validatorHelper;
    @Autowired
    private InterfaceService interfaceService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private SysRegistryService sysRegistryService;
    @Autowired
    private SysService sysService;

    @Value("${server.db}")
    private String dbType;


    @Value("${config.request.nifiapi.readtimeout}")
    private int readTimeout;

    public BusinessInterfaceService() {
        super(qTBusinessInterface, qTBusinessInterface.id);
    }

    @ApiOperation(value = "更改mock状态", notes = "更改mock状态")
    @PostMapping("/updateMockStatus")
    @Transactional(rollbackFor = Exception.class)
    public ResultDto<String> updateMockStatus(
            @ApiParam(value = "集成配置id") @RequestParam(value = "id", required = true) String id,
            @ApiParam(value = "更改后的状态") @RequestParam(value = "mockStatus", required = true) String mockStatus,
            @RequestParam("loginUserName") String loginUserName) {
        // 校验是否获取到登录用户
        if (StringUtils.isBlank(loginUserName)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!");
        }
        // 获取多接口，多个接口的id集合
        List<String> idList = busInterfaceIds(id);
        //redis缓存信息获取
        ArrayList<Predicate> arr = new ArrayList<>();
        arr.add(qTBusinessInterface.id.in(idList));
        List<RedisKeyDto> redisKeyDtoList = redisService.getRedisKeyDtoList(arr);

        long size = sqlQueryFactory.update(qTBusinessInterface).set(qTBusinessInterface.mockStatus, mockStatus)
                .set(qTBusinessInterface.updatedTime, new Date()).set(qTBusinessInterface.updatedBy, loginUserName)
                .where(qTBusinessInterface.id.in(idList)).execute();
        // 判断编辑是否成功
        if (idList.size() != size) {
            throw new RuntimeException("更改mock状态失败!");
        }
        //删除缓存
        cacheDelete(id);
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "更改mock状态成功!", new RedisDto(redisKeyDtoList).toString());
    }

    @ApiOperation(value = "更改集成配置状态", notes = "更改集成配置状态")
    @PostMapping("/updateStatus")
    @Transactional(rollbackFor = Exception.class)
    public ResultDto<String> updateStatus(
            @ApiParam(value = "集成配置") @RequestParam(value = "id", required = true) String id,
            @ApiParam(value = "更改后的状态") @RequestParam(value = "status", required = true) String status,
            @RequestParam("loginUserName") String loginUserName) {
        // 校验是否获取到登录用户
        if (StringUtils.isBlank(loginUserName)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!");
        }
        // 获取多接口，多个接口的id集合
        List<String> idList = busInterfaceIds(id);
        //redis缓存信息获取
        ArrayList<Predicate> arr = new ArrayList<>();
        arr.add(qTBusinessInterface.id.in(idList));
        List<RedisKeyDto> redisKeyDtoList = redisService.getRedisKeyDtoList(arr);

        long size = sqlQueryFactory.update(qTBusinessInterface).set(qTBusinessInterface.status, status)
                .set(qTBusinessInterface.updatedTime, new Date()).set(qTBusinessInterface.updatedBy, loginUserName)
                .where(qTBusinessInterface.id.in(idList)).execute();
        // 判断编辑是否成功
        if (idList.size() != size) {
            throw new RuntimeException("启停用状态编辑失败!");
        }
        cacheDelete(id);
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "更改接口配置状态成功!", new RedisDto(redisKeyDtoList).toString());
    }

    @ApiOperation(value = "获取mock模板", notes = "获取mock模板")
    @GetMapping("/getMockTemplate")
    public ResultDto<List<MockTemplateDto>> getMockTemplate(
            @ApiParam(value = "集成配置id") @RequestParam(value = "id", required = true) String id) {
        List<TBusinessInterface> interfaces = busInterfaces(id);
        if (CollectionUtils.isEmpty(interfaces)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "根据id没有找到集成配置");
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
    public ResultDto<String> saveMockTemplate(@RequestBody List<MockTemplateDto> dtoList, @RequestParam("loginUserName") String loginUserName) {
        // 校验是否获取到登录用户
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

            //删除缓存
            cacheDelete(dto.getId());
            long lon =  sqlQueryFactory.update(qTBusinessInterface).set(qTBusinessInterface.mockTemplate, dto.getMockTemplate())
                    .set(qTBusinessInterface.mockIsUse, dto.getMockIsUse()).set(qTBusinessInterface.updatedTime, new Date())
                    .set(qTBusinessInterface.updatedBy, loginUserName).where(qTBusinessInterface.id.eq(dto.getId())).execute();


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

    @ApiOperation(value = "请求方服务调试数据获取", notes = "请求方服务调试数据获取")
    @GetMapping("/getInterfaceDebug")
    public ResultDto<InDebugResDto> getInterfaceDebug(
            @ApiParam(value = "集成配置id") @RequestParam(value = "id", required = true) String id) {
        try {
            List<TBusinessInterface> businessInterfaces = sqlQueryFactory
                    .select(Projections.bean(TBusinessInterface.class, qTBusinessInterface.id,
                            qTBusinessInterface.requestInterfaceId,
                            qTInterface.interfaceUrl.as("interfaceUrl"),
                            qTSys.sysCode.as("sysCode"), qTInterface.inParamFormatType.as("sysIntfInParamFormatType")))
                    .from(qTBusinessInterface)
                    .leftJoin(qTInterface).on(qTBusinessInterface.requestInterfaceId.eq(qTInterface.id))
                    .leftJoin(qTSysRegistry).on(qTSysRegistry.id.eq(qTBusinessInterface.sysRegistryId))
                    .leftJoin(qTSys).on(qTSysRegistry.sysId.eq(qTSys.id))
                    .where(qTBusinessInterface.id.eq(id)).fetch();
            if (businessInterfaces == null || businessInterfaces.size() == 0) {
                return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有查询到集成配置信息");
            }
            // 获取入参列表
            TBusinessInterface businessInterface = businessInterfaces.get(0);
            String interfaceId = StringUtils.isNotEmpty(businessInterface.getRequestInterfaceId())
                    ? businessInterface.getRequestInterfaceId()
                    : "";
            // 拼接实体
            InDebugResDto resDto = new InDebugResDto();
            String inparamFormat = sqlQueryFactory.select(qTInterface.inParamFormat).from(qTInterface).where(
                            qTInterface.id.eq(interfaceId))
                    .fetchFirst();
            if ("2".equals(businessInterface.getSysIntfInParamFormatType())) {
                resDto.setWsInParams(inparamFormat);
                String wsUrl = niFiRequestUtil.getWsServiceUrl();
                if (!wsUrl.endsWith("/")) {
                    wsUrl = wsUrl + "/";
                }
                String suffix = "services/";
                wsUrl = wsUrl + suffix;
                resDto.setWsdlUrl(wsUrl);
                logger.info("之前wsUrl====={}", wsUrl);
                List<String> wsOperationNames = PlatformUtil.getWsdlOperationNames(wsUrl + businessInterface.getSysCode());
                resDto.setWsOperationNames(wsOperationNames);
                resDto.setSysIntfParamFormatType("2");
                logger.info("之后wsUrl====={}", wsUrl);
            } else {
                List<String> paramNames = sqlQueryFactory.select(qTInterfaceParam.paramName).from(qTInterfaceParam)
                        .where(qTInterfaceParam.interfaceId.eq(interfaceId)
                                .and(qTInterfaceParam.paramInOut.eq(Constant.ParmInOut.IN)))
                        .fetch();
                resDto.setInParams(paramNames);
                Map<String, Object> paramsMap = new HashMap<>();
                ObjectMapper objectMapper = new ObjectMapper();
                if (StringUtils.isNotBlank(inparamFormat)) {
                    paramsMap = objectMapper.readValue(inparamFormat, new TypeReference<Map<String, Object>>() {
                    });
                }
                paramsMap.put("funcode", businessInterface.getInterfaceUrl());
                paramsMap.put("productcode", "");
                resDto.setSysIntfParamFormatType("3");
                resDto.setWsInParams(objectMapper.writeValueAsString(paramsMap));
            }

            resDto.setFuncode(businessInterface.getInterfaceUrl());
            resDto.setProductcode("");
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取服务调试显示数据成功!", resDto);
        } catch (Exception e) {
            logger.error("获取服务调试显示数据失败! MSG:{}", ExceptionUtil.dealException(e));
            e.printStackTrace();
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "获取服务调试显示数据失败!");
        }
    }

    @ApiOperation(value = "获取集成配置列表")
    @GetMapping("/getInterfaceConfigureList")
    public ResultDto<TableData<TBusinessInterface>> getInterfaceConfigureList(
            @ApiParam(value = "分类id") @RequestParam(value = "typeId", required = false) Integer typeId,
            @ApiParam(value = "服务名称") @RequestParam(value = "interfaceName", required = false) String interfaceName,
            @ApiParam(value = "服务id") @RequestParam(value = "interfaceId", required = false) String interfaceId,
            @ApiParam(value = "页码", example = "1") @RequestParam(value = "pageNo", defaultValue = "1", required = false) Integer pageNo,
            @ApiParam(value = "每页大小", example = "10") @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {

        ArrayList<Predicate> predicateList = new ArrayList<>();
        if (typeId != null && typeId.intValue() != 0) {
            predicateList.add(qTInterface.typeId.eq(typeId.toString()));
        }
        if (StringUtils.isNotEmpty(interfaceName)) {
            predicateList.add(qTInterface.interfaceName.like("%" + interfaceName + "%"));
        }
        if (StringUtils.isNotEmpty(interfaceId)) {
            predicateList.add(qTBusinessInterface.requestInterfaceId.eq(interfaceId));
        }
        // 获取集成配置列表信息
        QueryResults<TBusinessInterface> queryResults = getInterfaceConfigureList(predicateList, pageNo, pageSize);
        // 分页
        TableData<TBusinessInterface> tableData = new TableData<>(queryResults.getTotal(), queryResults.getResults());
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取集成配置列表获取成功", tableData);
    }

    @ApiOperation(value = "获取集成配置详情")
    @GetMapping("/getInterfaceConfigInfoById")
    public ResultDto<BusinessInterfaceDto> getInterfaceConfigInfoById(
            @ApiParam(value = "结成配置id") @RequestParam(value = "id", required = true) String id) {
        // 返回对象
        BusinessInterfaceDto dto = new BusinessInterfaceDto();
        dto.setId(id);

        // 多个厂商服务信息
        List<TBusinessInterface> tbiList = new ArrayList<>();

        // 获取集成配置对象
        TBusinessInterface tbi = getOne(id);
        if (tbi != null) {
            // 请求方标准服务
            String requestInterfaceId = tbi.getRequestInterfaceId();
            dto.setRequestInterfaceId(requestInterfaceId);
            dto.setInterfaceSlowFlag(tbi.getInterfaceSlowFlag());
            dto.setReplayFlag(tbi.getReplayFlag());
            dto.setMockStatus(tbi.getMockStatus());
            dto.setStatus(tbi.getStatus());
            // 获取请求方服务类型
            ArrayList<Predicate> list = new ArrayList<>();
            if (StringUtils.isNotEmpty(requestInterfaceId)) {
                list.add(qTInterface.id.eq(requestInterfaceId));
            }
            TInterface tInterface = sqlQueryFactory.select(qTInterface).from(qTInterface)
                    .where(list.toArray(new Predicate[list.size()])).fetchOne();
            dto.setRequestInterfaceTypeId(tInterface.getTypeId());
            // 多个厂商配置服务信息
            tbiList = getTBusinessInterfaceList(tbi.getRequestInterfaceId());
        }

        dto.setBusinessInterfaceList(tbiList);

        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取集成配置详情成功", dto);
    }


    @ApiOperation(value = "获取服务信息（重放标识）", notes = "获取服务信息（重放标识）")
    @GetMapping("/getBusItfInfo")
    public ResultDto getBusItfInfo(
            @ApiParam(value = "请求方系统服务ID") @RequestParam(value = "reqItfId", required = true) String reqItfId, @RequestParam("loginUserName") String loginUserName) {
        //校验入参
        if (StringUtils.isBlank(reqItfId)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "请求参数不能为空!");
        }
        // 校验是否获取到登录用户
        if (StringUtils.isBlank(loginUserName)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!");
        }
        TBusinessInterface tb = getOneByInterfaceId(reqItfId);
        Map resMap = new HashMap();
        resMap.put("replayFlag", tb.getReplayFlag());
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "获取服务信息", resMap);
    }

    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "新增/编辑服务配置", notes = "新增/编辑服务配置")
    @PostMapping("/saveAndUpdateInterfaceConfig")
    public ResultDto<String> saveAndUpdateInterfaceConfig(@RequestBody BusinessInterfaceDto dto, @RequestParam("loginUserName") String loginUserName) {
        if (dto == null) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "请求参数不能为空!");
        }
        if (StringUtils.isBlank(dto.getRequestInterfaceId())) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "请求方服务不能为空!");
        }
        // 校验是否获取到登录用户
        if (StringUtils.isBlank(loginUserName)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!");
        }

        if (Constant.Operation.ADD.equals(dto.getAddOrUpdate())) {
            return this.saveInterfaceConfig(dto, loginUserName);
        }
        if (Constant.Operation.UPDATE.equals(dto.getAddOrUpdate())) {
            return this.updateInterfaceConfig(dto, loginUserName, true);
        }
        //暂存
        if ("3".equals(dto.getAddOrUpdate())) {
            return this.updateInterfaceConfig(dto, loginUserName, false);
        }
        return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "addOrUpdate 新增编辑标识不正确!", null);
    }

    /**
     * 新增集成配置
     *
     * @param dto
     * @param loginUserName
     * @return
     */
    private ResultDto<String> saveInterfaceConfig(BusinessInterfaceDto dto, String loginUserName) {
        //当前请求方
        TInterface tInterface = interfaceService.getOne(dto.getRequestInterfaceId());
        if (tInterface == null) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "未查询到请求方!", null);
        }

        // 根据条件判断是否存在该数据
        List<TBusinessInterface> tbiList = getBusinessInterfaceIsExist(dto.getRequestInterfaceId());
        if (CollectionUtils.isNotEmpty(tbiList)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "根据请求方服务id匹配到该条件数据已存在!", null);
        }

        tbiList = dto.getBusinessInterfaceList();
        String returnId = "";
        for (int i = 0; i < tbiList.size(); i++) {
            TBusinessInterface tbi = tbiList.get(i);
            // 新增集成配置
            tbi.setId(batchUidService.getUid(qTBusinessInterface.getTableName()) + "");
            tbi.setRequestInterfaceId(dto.getRequestInterfaceId());
            tbi.setStatus(Constant.Status.START);
            tbi.setMockStatus(Constant.Status.STOP);
            tbi.setCreatedTime(new Date());
            tbi.setCreatedBy(loginUserName);
            tbi.setExcErrOrder(i);
            tbi.setInterfaceSlowFlag(dto.getInterfaceSlowFlag());
            tbi.setReplayFlag(dto.getReplayFlag());
            // 获取schema
            niFiRequestUtil.generateSchemaToInterface(tbi);
            // 新增集成配置
            this.post(tbi);
            if (StringUtils.isBlank(returnId)) {
                returnId = tbi.getId();
            }
        }
        // redis缓存删除
        cacheDelete(returnId);
        Map<String, String> data = new HashMap<String, String>();
        data.put("id", returnId);
        return new ResultDto<String>(Constant.ResultCode.SUCCESS_CODE, "新增集成配置成功", JSON.toJSONString(data));
    }

    /**
     * 编辑集成配置
     *
     * @param dto
     * @param loginUserName
     * @return
     */
    private ResultDto updateInterfaceConfig(BusinessInterfaceDto dto, String loginUserName, boolean insertHisFlg) {
        //是否修改过请求方
        Boolean isModReq = false;
        // 根据条件判断是否存在该数据
        TBusinessInterface exsitsBI = this.getOne(dto.getId());
        if (exsitsBI != null) {
            //沒修改過請求方
            if (exsitsBI.getRequestInterfaceId().equals(dto.getRequestInterfaceId())) {
//				沒修改過，不校驗是否已存在
            } else {
                isModReq = true;
                List<TBusinessInterface> tbiList = getBusinessInterfaceIsExist(dto.getRequestInterfaceId());
                if (CollectionUtils.isNotEmpty(tbiList)) {
                    return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "请求方服务id匹配到该条件数据已存在!", null);
                }
            }
        }

        //更新历史记录中的recordId
        String lastRecordId = dto.getRequestInterfaceId();
        String oldRecordId = exsitsBI.getRequestInterfaceId();
        if (isModReq) {
            //修改过
            historyService.updateRecordId(oldRecordId, lastRecordId);
        } else {
            //未修改
        }

        if (insertHisFlg) {
            //插入history
            List<TBusinessInterface> list = getListByCondition(exsitsBI.getRequestInterfaceId());
            String businessInterfaceName = "";
            Integer interfaceSlowFlag = null;
            Integer replayFlag = null;
            String requestInterfaceName = "";
            String typeId = "";
            String requestSysId = "";
            for (int i = 0; i < list.size(); i++) {
                TBusinessInterface tbi = list.get(i);
                //查询服务名称
                TInterface tInterface = interfaceService.getOne(tbi.getRequestInterfaceId());
                requestInterfaceName = tInterface.getInterfaceName();

                if (StringUtils.isNotBlank(tbi.getSysRegistryId())) {
                    TSysRegistry tSysRegistry = sysRegistryService.getOne(tbi.getSysRegistryId());
                    TSys requestedSys = sysService.getOne(tSysRegistry.getSysId());
                    businessInterfaceName += (requestedSys.getSysName() + "/" + tbi.getBusinessInterfaceName() + ",");
                }


                if (interfaceSlowFlag == null) {
                    interfaceSlowFlag = tbi.getInterfaceSlowFlag();
                }
                if (replayFlag == null) {
                    replayFlag = tbi.getReplayFlag();
                }
                if (StringUtils.isBlank(typeId)) {
                    typeId = tInterface.getTypeId();
                }

            }
            if (businessInterfaceName.endsWith(",")) {
                businessInterfaceName = businessInterfaceName.substring(0, businessInterfaceName.length() - 1);
            }

            //插入历史记录
            Map map = new HashMap();
            map.put("requestInterfaceId", exsitsBI.getRequestInterfaceId());
            map.put("businessInterfaceName", businessInterfaceName);
            map.put("requestInterfaceName", requestInterfaceName);
            map.put("requestSysId", requestSysId);
            map.put("requestInterfaceTypeId", typeId);
            map.put("interfaceSlowFlag", interfaceSlowFlag);
            map.put("replayFlag", replayFlag);
            String hisShow = JSON.toJSONString(map);
            historyService.insertHis(list, 1, loginUserName, lastRecordId, lastRecordId, hisShow);
        }

        // 先删除更新之前缓存删除
        cacheDelete(dto.getId());

        List<String> rtnId = new ArrayList<>();
        List<TBusinessInterface> tbiList = dto.getBusinessInterfaceList();

        //为了做到服务+顺序的唯一   如果包含修改超过两条则先把所有顺序按照最大值+自己当前顺序值
        updateSeq(tbiList);

        for (int i = 0; i < tbiList.size(); i++) {
            TBusinessInterface tbi = tbiList.get(i);
            if (StringUtils.isBlank(tbi.getId())) {
                // 新增的厂商配置
                tbi.setId(batchUidService.getUid(qTBusinessInterface.getTableName()) + "");
                tbi.setRequestInterfaceId(dto.getRequestInterfaceId());
                tbi.setStatus(exsitsBI.getStatus());
                tbi.setMockStatus(exsitsBI.getMockStatus());
                tbi.setCreatedTime(new Date());
                tbi.setCreatedBy(loginUserName);
                tbi.setExcErrOrder(i);
                tbi.setInterfaceSlowFlag(dto.getInterfaceSlowFlag());
                tbi.setReplayFlag(dto.getReplayFlag());
                // 获取schema
                niFiRequestUtil.generateSchemaToInterface(tbi);
                // 新增集成配置
                this.post(tbi);
            } else {
                // 集成配置重新赋值
                tbi.setRequestInterfaceId(dto.getRequestInterfaceId());
                tbi.setUpdatedTime(new Date());
                tbi.setUpdatedBy(loginUserName);
                tbi.setExcErrOrder(i);
                tbi.setInterfaceSlowFlag(dto.getInterfaceSlowFlag());
                tbi.setReplayFlag(dto.getReplayFlag());
                // 获取schema
                niFiRequestUtil.generateSchemaToInterface(tbi);
                // 新增集成配置
                long l = this.put(tbi.getId(), tbi);
                if (l < 1) {
                    throw new RuntimeException("修改新增集成配置信息失败!");
                }
                rtnId.add(tbi.getId());
            }
        }
        // 再删除更新之后缓存删除
        cacheDelete(dto.getId());
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "编辑集成配置成功", null);
    }

    private void updateSeq(List<TBusinessInterface> tbiList) {
        List<String> list = tbiList.stream().map(TBusinessInterface::getId).collect(Collectors.toList());
        //如果不存在两条修改  则不涉及顺序调整
        if (CollectionUtils.isEmpty(list) || list.size() < 2) {
            return;
        }

        sqlQueryFactory.update(qTBusinessInterface).set(qTBusinessInterface.excErrOrder, qTBusinessInterface.excErrOrder.add(list.size()))
                .where(qTBusinessInterface.id.in(list)).execute();
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
    @ApiOperation(value = "集成配置删除", notes = "集成配置删除")
    @PostMapping("/deleteInterfaceConfigure")
    public ResultDto<String> deleteInterfaceConfigure(
            @ApiParam(value = "集成配置id") @RequestParam(value = "id", required = true) String id) {
        TBusinessInterface tbi = this.getOne(id);
        if (tbi == null) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "该服务id查询不到集成配置信息!", "该服务id查询不到集成配置信息!");
        }
        List<TBusinessInterface> list = getListByCondition(tbi.getRequestInterfaceId());
        // 获取返回缓存id
        List<String> rtnStr = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(list)) {
            for (TBusinessInterface obj : list) {
                rtnStr.add(obj.getId());
            }
        }
        // 缓存信息获取
        cacheDelete(id);

        // 删除相同条件集成配置
        long count = delObjByCondition(tbi.getRequestInterfaceId());
        if (count <= 0) {
            throw new RuntimeException("集成配置删除失败!");
        }
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "集成配置删除成功!共删除" + count + "条数据",
                null);
    }

    @ApiOperation(value = "根据id删除单个集成配置信息", notes = "根据id删除单个集成配置信息")
    @PostMapping("/deleteBusinessInterfaceById")
    public ResultDto<String> deleteBusinessInterfaceById(
            @ApiParam(value = "集成配置id") @RequestParam(value = "id", required = true) String id) {
        TBusinessInterface tbi = this.getOne(id);
        if (tbi == null) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "根据id未查出该集成配置信息!", id);
        }

        //删除缓存
        cacheDelete(id);

        long count = this.delete(id);
        if (count < 1) {
            throw new RuntimeException("根据id删除该集成配置信息失败!");
        }

        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "单个集成配置信息删除成功!", null);
    }
    @ApiOperation(value = "被请求方服务调试数据获取")
    @PostMapping("/getInterfaceDebugger")
    public ResultDto<String> getInterfaceDebugger(String interfaceId) {
        if (StringUtils.isBlank(interfaceId)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "请求方服务id必传");
        }
        try {
            TInterface inter = sqlQueryFactory.select(qTInterface).from(qTInterface).where(qTInterface.id.eq(interfaceId)).fetchFirst();
            if (inter == null) {
                return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "系统服务不存在，无法获取入参信息，请检查集成配置");
            }
            String paramJson = inter.getInParamFormat();
            if ("2".equals(inter.getInParamFormatType())) {
                paramJson = niFiRequestUtil.xml2json(paramJson);
            }
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "JSON格式入参模板获取成功!", paramJson);
        } catch (Exception e) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "厂商服务调试数据失败!");
        }
    }

    @ApiOperation(value = "被请求方服务调试服务")
    @PostMapping("/interfaceDebugger")
    public ResultDto<Object> interfaceDebugger(@RequestBody JoltDebuggerDto dto) {
        // 校验参数是否完整
        ValidationResult validationResult = validatorHelper.validate(dto);
        if (validationResult.isHasErrors()) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, validationResult.getErrorMsg());
        }
        if (dto.getJolt() == null && dto.getJslt() == null) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "jolt脚本和jslt脚本参数不能都为空");
        }
        if (dto.getJolt() != null && dto.getJslt() != null) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "jolt脚本和jslt脚本参数不能同时存在");
        }
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "", niFiRequestUtil.joltDebugger2(dto));
    }

    @ApiOperation(value = "导出选中的集成配置")
    @GetMapping("/downloadInterfaceConfigs/{Ids}")
    public void getSqlConfig(HttpServletResponse response, @ApiParam("服务Id") @PathVariable String Ids) {
        String[] businessInterfaceIds = Ids.split(",");
        StringBuilder sqlStringBuffer = new StringBuilder();
        this.getResourcesByBizInterfaceIds(Arrays.asList(businessInterfaceIds), sqlStringBuffer);

        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String sqlName = "interface_" + dateStr + ".sql";
        try {
            response.setContentType("application/x-msdownload");
            response.setHeader("content-disposition", "attachment;filename=" + URLEncoder.encode("interface_" + dateStr + ".zip", "utf-8"));

            String fileName = sqlName; // 每个zip文件名
            byte[] file = sqlStringBuffer.toString().getBytes(StandardCharsets.UTF_8); // 这个zip文件的字节
            try (
                    ZipOutputStream zos = new ZipOutputStream(response.getOutputStream());
                    BufferedOutputStream bos = new BufferedOutputStream(zos);
                    BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(file))) {
                zos.putNextEntry(new ZipEntry(fileName));
                int len = 0;
                byte[] buf = new byte[10 * 1024];
                while ((len = bis.read(buf, 0, buf.length)) != -1) {
                    bos.write(buf, 0, len);
                }
                bos.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @PostMapping("/interfaceDebug/{authFlag}")
    @ApiOperation(value = "请求方服务调试", notes = "请求方服务调试")
    public ResultDto<String> interfaceDebug(@RequestBody InterfaceDebugDto degubDto, @PathVariable("authFlag") String authFlag) {
        String result = "";
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Debugreplay-Flag", "1");
        String loginUrlPrefix = niFiRequestUtil.getInterfaceDebugWithAuth();
        boolean isws = "2".equals(degubDto.getSysIntfParamFormatType());
        if (isws) {
            loginUrlPrefix = niFiRequestUtil.getWsServiceUrlWithAuth() + "/services/";
        }

        if ("1".equals(authFlag)) {
            logger.info("isws:" + isws + ",loginurlprefix:" + loginUrlPrefix);
            Map<String, String> tokenInfo = niFiRequestUtil.interfaceAuthLogin(loginUrlPrefix, isws);
            if (tokenInfo != null && tokenInfo.size() > 0) {
                headerMap.putAll(tokenInfo);
            }
        }
        if ("2".equals(degubDto.getSysIntfParamFormatType())) {
            String wsdlUrl = degubDto.getWsdlUrl();
            String methodName = degubDto.getWsOperationName();
            String funcode = degubDto.getFuncode();
            String param = degubDto.getFormat();
            if ("1".equals(authFlag)) {
                String path = wsdlUrl.substring(wsdlUrl.indexOf("/services"));
                wsdlUrl = niFiRequestUtil.getWsServiceUrlWithAuth() + path;
                logger.info("wsinvokeurl:" + wsdlUrl);
            }
            result = PlatformUtil.invokeWsService(wsdlUrl, methodName, funcode, param, headerMap, readTimeout);
        } else {
            result = niFiRequestUtil.interfaceDebug(degubDto.getFormat(), headerMap, "1".equals(authFlag));
        }
        if (StringUtils.isBlank(result)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "服务调试失败");
        }
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "", result);
    }

    @PostMapping(path = "/uploadInterFaceSql")
    public ResultDto<String> uploadInterFaceSql(@RequestParam("sqlFiles") MultipartFile[]
                                                        sqlFiles, @RequestParam("loginUserName") String loginUserName) {
        //校验是否获取到登录用户
        if (org.apache.commons.lang3.StringUtils.isBlank(loginUserName)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!", "没有获取到登录用户!");
        }
        //获取数据库连接
        StringBuilder message = new StringBuilder();
        try (Connection connection = sqlQueryFactory.getConnection();) {

            //判断是否获取到文件
            if (sqlFiles == null || sqlFiles.length == 0) {
                return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到上传文件!", "没有获取到上传文件!");
            }
            //sql分批sql语句
            int insetNum = 0;
            for (MultipartFile file : sqlFiles) {
                try (
                        Statement statement = connection.createStatement();
                        InputStream is = file.getInputStream();
                        InputStreamReader inputStreamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
                ) {
                    StringBuilder sql = new StringBuilder();
                    connection.setAutoCommit(false);//不自动提交
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);//缓存数据用于读取
                    String lineText = "";
                    while ((lineText = bufferedReader.readLine()) != null) {
                        sql.append(lineText).append("\r\n");
                    }
                    //将sys_config表中的平台id以及项目id进行替换
                    sql = new StringBuilder(sql.toString());
                    String[] sqls = sql.toString().split("END_OF_SQL");
                    for (String str : sqls) {
                        if (str.trim().startsWith("INSERT") || str.trim().startsWith("REPLACE") || str.trim().startsWith("delete"))
                            statement.addBatch(str);
                    }
                    //事务提交，整体成功或失败
                    statement.executeBatch();
                    connection.commit();
                    //清除SQL语句
                    statement.clearBatch();
                    insetNum++;
                } catch (Exception e) {
                    connection.rollback();
                    message.append(e.getMessage());
                }
            }
            if (insetNum == sqlFiles.length) {
                return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "sql脚本全部执行成功", insetNum + "");
            } else {
                return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "sql脚本部分执行错误" + message, insetNum + "");
            }
        } catch (Exception e) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "执行sql脚本失败", e.getLocalizedMessage());
        }
    }

    /**
     * 根据相同条件删除接口配置数据
     *
     * @param interfaceId
     */
    public long delObjByCondition(String interfaceId) {
        long count = sqlQueryFactory.delete(qTBusinessInterface).where(qTBusinessInterface.requestInterfaceId
                .eq(interfaceId)).execute();
        return count;
    }

    /**
     * 根据相同条件查询所有接口配置数据
     *
     * @param interfaceId
     */
    public List<TBusinessInterface> getListByCondition(String interfaceId) {
        ArrayList<Predicate> list = new ArrayList<>();
        if (StringUtils.isNotBlank(interfaceId)) {
            list.add(qTBusinessInterface.requestInterfaceId.eq(interfaceId));
        }
        List<TBusinessInterface> tbiList = sqlQueryFactory.select(qTBusinessInterface).from(qTBusinessInterface)
                .where(list.toArray(new Predicate[list.size()])).fetch();
        return tbiList;
    }

    private void getResourcesByBizInterfaceIds(List<String> ids, StringBuilder sqlStringBuffer) {
        List<String> interfaceIds = new ArrayList<>();
        List<String> sysRegistryIds = new ArrayList<>();
        List<String> pluginIds = new ArrayList<>();
        List<String> sysIds = new ArrayList<>();
        List<String> driverIds = new ArrayList<>();

        List<Path<?>> lists = new ArrayList<>();
        lists.addAll(Arrays.asList(qTBusinessInterface.all()));
        for (String businessInterfaceId : ids) {
            String requestInterfaceId = sqlQueryFactory.select(qTBusinessInterface.requestInterfaceId).from(qTBusinessInterface)
                    .where(qTBusinessInterface.id.eq(businessInterfaceId)).fetchFirst();
            if (StringUtils.isNotBlank(requestInterfaceId)) {
                List<TBusinessInterface> tBusinessInterfaces = sqlQueryFactory.select(Projections.bean(TBusinessInterface.class, lists.toArray(new Path[0]))).from(qTBusinessInterface)
                        .where(qTBusinessInterface.requestInterfaceId.eq(requestInterfaceId)).fetch();

                //集成配置

                for (TBusinessInterface tBusinessInterface : tBusinessInterfaces) {
                    interfaceIds.add(tBusinessInterface.getRequestInterfaceId());
                    pluginIds.add(tBusinessInterface.getPluginId());
                    sysRegistryIds.add(tBusinessInterface.getSysRegistryId());
                    String mocktpl = tBusinessInterface.getMockTemplate();
                    if (StringUtils.isBlank(mocktpl)) {
                        mocktpl = tBusinessInterface.getOutParamFormat();
                    }
                    sqlStringBuffer.append("delete from t_business_interface where id ='" + tBusinessInterface.getId() + "'; \n");
                    sqlStringBuffer.append("END_OF_SQL\n");

                    sqlStringBuffer.append("INSERT INTO  t_business_interface  ( ID ,  " +
                            " REQUEST_INTERFACE_ID ,  SYS_REGISTRY_ID ,  BUSINESS_INTERFACE_NAME ,  REQUEST_TYPE , " +
                            " REQUEST_CONSTANT ,  INTERFACE_TYPE ,  PLUGIN_ID ,  IN_PARAM_FORMAT ,  IN_PARAM_SCHEMA ,  IN_PARAM_TEMPLATE_TYPE , " +
                            " IN_PARAM_TEMPLATE ,  IN_PARAM_FORMAT_TYPE ,  OUT_PARAM_FORMAT ,  OUT_PARAM_SCHEMA ,  OUT_PARAM_TEMPLATE_TYPE , " +
                            " OUT_PARAM_TEMPLATE ,  OUT_PARAM_FORMAT_TYPE ,  MOCK_TEMPLATE ,  MOCK_STATUS ,  STATUS ,  EXC_ERR_STATUS , " +
                            " EXC_ERR_ORDER ,  MOCK_IS_USE ,  CREATED_BY ,  CREATED_TIME ,  UPDATED_BY ,  UPDATED_TIME ,  ASYNC_FLAG , " +
                            " INTERFACE_SLOW_FLAG ) VALUES ('" + tBusinessInterface.getId() + "', '" + tBusinessInterface.getRequestInterfaceId() + "', " +
                            "'" + tBusinessInterface.getSysRegistryId() + "', '" + PlatformUtil.escapeSqlSingleQuotes(tBusinessInterface.getBusinessInterfaceName()) + "', '" + tBusinessInterface.getRequestType() + "', '" + PlatformUtil.escapeSqlSingleQuotes(tBusinessInterface.getRequestConstant()) + "', " +
                            "'" + tBusinessInterface.getInterfaceType() + "', '" + tBusinessInterface.getPluginId() + "', '" + PlatformUtil.escapeSqlSingleQuotes(tBusinessInterface.getInParamFormat()) + "', '" + tBusinessInterface.getInParamSchema() + "', " +
                            "" + tBusinessInterface.getInParamTemplateType() + ", '" + PlatformUtil.escapeSqlSingleQuotes(tBusinessInterface.getInParamTemplate()) + "', '" + tBusinessInterface.getInParamFormatType() + "', '" + tBusinessInterface.getOutParamFormat() + "', " +
                            "'" + tBusinessInterface.getOutParamSchema() + "', " + tBusinessInterface.getOutParamTemplateType() + ", '" + PlatformUtil.escapeSqlSingleQuotes(tBusinessInterface.getOutParamTemplate()) + "', '" + tBusinessInterface.getOutParamFormatType() + "', " +
                            "'" + PlatformUtil.escapeSqlSingleQuotes(mocktpl) + "', '" + tBusinessInterface.getMockStatus() + "', '" + tBusinessInterface.getStatus() + "', '" + tBusinessInterface.getExcErrStatus() + "', " +
                            "" + tBusinessInterface.getExcErrOrder() + ", " + tBusinessInterface.getMockIsUse() + ", 'admin', now() , 'admin', now() , " + tBusinessInterface.getAsyncFlag() + ", " + tBusinessInterface.getInterfaceSlowFlag() + "); \n");
                    sqlStringBuffer.append("END_OF_SQL\n");
                }
            }
        }

        List<TInterface> tInterfaces = sqlQueryFactory.select(qTInterface).from(qTInterface).where(qTInterface.id.in(interfaceIds)).fetch();
        for (TInterface tInterface : tInterfaces) {
            sqlStringBuffer.append("delete from t_interface where id ='" + tInterface.getId() + "'; \n");
            sqlStringBuffer.append("END_OF_SQL\n");

            sqlStringBuffer.append("INSERT INTO  t_interface  ( ID ,  INTERFACE_NAME ,  TYPE_ID , " +
                    " INTERFACE_URL ,  IN_PARAM_FORMAT ,  OUT_PARAM_FORMAT ,  PARAM_OUT_STATUS ,  PARAM_OUT_STATUS_SUCCESS ," +
                    "  CREATED_BY ,  CREATED_TIME ,  UPDATED_BY ,  UPDATED_TIME ,  IN_PARAM_SCHEMA ,  IN_PARAM_FORMAT_TYPE , " +
                    " OUT_PARAM_SCHEMA ,  OUT_PARAM_FORMAT_TYPE,allow_log_discard,interface_type,async_flag," +
                    " encryption_type,mask_pos_start,mask_pos_end) VALUES ('" + tInterface.getId() + "', '" + PlatformUtil.escapeSqlSingleQuotes(tInterface.getInterfaceName()) + "', '" + tInterface.getTypeId() + "', " +
                    "'" + tInterface.getInterfaceUrl() + "', '" + PlatformUtil.escapeSqlSingleQuotes(tInterface.getInParamFormat()) + "', '" + PlatformUtil.escapeSqlSingleQuotes(tInterface.getOutParamFormat()) + "', '" + tInterface.getParamOutStatus() + "', '" + tInterface.getParamOutStatusSuccess() +
                    "', 'admin', now() , 'admin', now(), '" + tInterface.getInParamSchema() + "', '" + tInterface.getInParamFormatType() + "', '" + tInterface.getOutParamSchema() + "', '" + tInterface.getOutParamFormatType() + "', '" + tInterface.getAllowLogDiscard() + "', " + tInterface.getInterfaceType() + ", " + tInterface.getAsyncFlag() +
                    "," + tInterface.getEncryptionType() + ", " + tInterface.getMaskPosStart() + ", " + tInterface.getMaskPosEnd() +
                    ") ;\n");
            sqlStringBuffer.append("END_OF_SQL\n");
        }
        List<TInterfaceParam> tInterfaceParams = sqlQueryFactory.select(qTInterfaceParam).from(qTInterfaceParam).where(qTInterfaceParam.interfaceId.in(interfaceIds)).fetch();
        for (TInterfaceParam tInterfaceParam : tInterfaceParams) {
            sqlStringBuffer.append("delete from t_interface_param where id ='" + tInterfaceParam.getId() + "'; \n");
            sqlStringBuffer.append("END_OF_SQL\n");

            sqlStringBuffer.append("INSERT INTO  t_interface_param  ( ID ,  PARAM_NAME ,  PARAM_INSTRUCTION ,  INTERFACE_ID ,  PARAM_TYPE ,  PARAM_LENGTH ,  PARAM_IN_OUT ,  CREATED_BY ,  CREATED_TIME , " +
                    " UPDATED_BY ,  UPDATED_TIME ) VALUES ('" + tInterfaceParam.getId() + "', '" + tInterfaceParam.getParamName() + "', '" + tInterfaceParam.getParamInstruction() + "', '" + tInterfaceParam.getInterfaceId() + "'," +
                    " '" + tInterfaceParam.getParamType() + "', " + tInterfaceParam.getParamLength() + ", '" + tInterfaceParam.getParamInOut() + "', 'admin', now() , 'admin', now());\n");
            sqlStringBuffer.append("END_OF_SQL\n");
        }

        List<TSysRegistry> tSysRegistrys = sqlQueryFactory.select(qTSysRegistry).from(qTSysRegistry).where(qTSysRegistry.id.in(sysRegistryIds)).fetch();
        for (TSysRegistry sysRegistry : tSysRegistrys) {
            sysIds.add(sysRegistry.getSysId());
            sqlStringBuffer.append("delete from t_sys_registry where id ='" + sysRegistry.getId() + "'; \n");
            sqlStringBuffer.append("END_OF_SQL\n");

            sqlStringBuffer.append("INSERT INTO  t_sys_registry  ( ID ,  SYS_ID ,   CONNECTION_TYPE ,  ADDRESS_URL ,  ENDPOINT_URL ," +
                    "  NAMESPACE_URL ,  DATABASE_NAME ,  DATABASE_URL ,  DATABASE_TYPE ,  DATABASE_DRIVER , " +
                    " DRIVER_URL ,  JSON_PARAMS ,  USER_NAME ,  USER_PASSWORD ,  CREATED_BY , " +
                    " CREATED_TIME ,  UPDATED_BY ,  UPDATED_TIME , " +
                    " REGISTRY_NAME ,USE_STATUS) VALUES ('" + sysRegistry.getId() + "', '" + sysRegistry.getSysId() + "', '" +
                    sysRegistry.getConnectionType() + "', '" + sysRegistry.getAddressUrl() + "', '" + sysRegistry.getEndpointUrl() + "', " +
                    "'" + sysRegistry.getNamespaceUrl() + "', '" + sysRegistry.getDatabaseName() + "', '" + sysRegistry.getDatabaseUrl() + "', '" + sysRegistry.getDatabaseType() + "', '" + sysRegistry.getDatabaseDriver() + "', " +
                    "'" + sysRegistry.getDriverUrl() + "', '" + sysRegistry.getJsonParams() + "', '" + sysRegistry.getUserName() + "', '" + sysRegistry.getUserPassword() + "','admin', now() , 'admin', now(), '" + sysRegistry.getRegistryName() + "','" + sysRegistry.getUseStatus() + "') ;\n");
            sqlStringBuffer.append("END_OF_SQL\n");
        }
        List<TPlugin> tPlugins = sqlQueryFactory.select(qTPlugin).from(qTPlugin).where(qTPlugin.id.in(pluginIds)).fetch();
        for (TPlugin tPlugin : tPlugins) {
            sqlStringBuffer.append("delete from t_plugin where id ='" + tPlugin.getId() + "'; \n");
            sqlStringBuffer.append("END_OF_SQL\n");

            sqlStringBuffer.append("INSERT INTO  t_plugin  ( ID ,  PLUGIN_NAME ,  PLUGIN_CODE ,  TYPE_ID ,  PLUGIN_INSTRUCTION ,  PLUGIN_CONTENT ,  CREATED_BY ,  CREATED_TIME ,  UPDATED_BY ,  UPDATED_TIME ,  DEPENDENT_PATH ) " +
                    "VALUES ('" + tPlugin.getId() + "', '" + PlatformUtil.escapeSqlSingleQuotes(tPlugin.getPluginName()) + "', '" + tPlugin.getPluginCode() + "', '" + tPlugin.getTypeId() + "', '" + tPlugin.getPluginInstruction() + "', '" + PlatformUtil.escapeSqlSingleQuotes(tPlugin.getPluginContent()) + "', 'admin', now() , 'admin', now(), '" + tPlugin.getDependentPath() + "') ON conflict(ID) DO nothing;\n");
            sqlStringBuffer.append("END_OF_SQL\n");
        }
        List<TSys> tSyss = sqlQueryFactory.select(qTSys).from(qTSys).where(qTSys.id.in(sysIds)).fetch();
        for (TSys tSys : tSyss) {
            sqlStringBuffer.append("delete from t_sys where id ='" + tSys.getId() + "'; \n");
            sqlStringBuffer.append("END_OF_SQL\n");

            sqlStringBuffer.append("INSERT INTO  t_sys  ( ID ,  SYS_NAME ,  SYS_CODE ,  IS_VALID ,  CREATED_BY ,  CREATED_TIME ,  UPDATED_BY ,  UPDATED_TIME ) VALUES " +
                    "('" + tSys.getId() + "', '" + tSys.getSysName() + "', '" + tSys.getSysCode() + "', '" + tSys.getIsValid() + "', 'admin', now() , 'admin', now()) ON conflict(ID) DO nothing;\n");
            sqlStringBuffer.append("END_OF_SQL\n");
        }
        List<TSysDriveLink> tSysDriveLinks = sqlQueryFactory.select(qTSysDriveLink).from(qTSysDriveLink).where(qTSysDriveLink.sysId.in(sysIds)).fetch();
        for (TSysDriveLink tSysDriveLink : tSysDriveLinks) {
            sqlStringBuffer.append("delete from t_sys_drive_link where id ='" + tSysDriveLink.getId() + "'; \n");
            sqlStringBuffer.append("END_OF_SQL\n");

            driverIds.add(tSysDriveLink.getDriveId());
            sqlStringBuffer.append("INSERT INTO  t_sys_drive_link  ( ID ,  SYS_ID ,  DRIVE_ID ,  DRIVE_ORDER ,  CREATED_BY ,  CREATED_TIME ,  UPDATED_BY ,  UPDATED_TIME ) VALUES " +
                    "('" + tSysDriveLink.getId() + "', '" + tSysDriveLink.getSysId() + "', '" + tSysDriveLink.getDriveId() + "', " + tSysDriveLink.getDriveOrder() + ", 'admin', now() , 'admin', now()) ON conflict(ID) DO nothing;\n");
            sqlStringBuffer.append("END_OF_SQL\n");
        }
        List<TDrive> tDrives = sqlQueryFactory.select(qTDrive).from(qTDrive).where(qTDrive.id.in(driverIds)).fetch();
        for (TDrive tDrive : tDrives) {
            sqlStringBuffer.append("delete from t_drive where id ='" + tDrive.getId() + "'; \n");
            sqlStringBuffer.append("END_OF_SQL\n");

            sqlStringBuffer.append("INSERT INTO  t_drive  ( ID ,  DRIVE_NAME ,  DRIVE_CODE ,  TYPE_ID ,  DRIVE_INSTRUCTION ,  DRIVE_CONTENT ,  CREATED_BY ,  CREATED_TIME ,  UPDATED_BY ,  UPDATED_TIME ,  DRIVE_CALL_TYPE ,  DEPENDENT_PATH ) VALUES " +
                    "('" + tDrive.getId() + "', '" + PlatformUtil.escapeSqlSingleQuotes(tDrive.getDriveName()) + "', '" + tDrive.getDriveCode() + "', '" + tDrive.getTypeId() + "', '" + tDrive.getDriveInstruction() + "', '" + PlatformUtil.escapeSqlSingleQuotes(tDrive.getDriveContent()) + "', 'admin', now() , 'admin', now(), '" + tDrive.getDriveCallType() + "', '" + tDrive.getDependentPath() + "') ON conflict(ID) DO nothing;\n");
            sqlStringBuffer.append("END_OF_SQL\n");
        }
    }


    /**
     * 根据标准接口id获取接口配置id(获取标准接口详情使用)
     *
     * @param interfaceId
     */
    public List<TBusinessInterface> getListByInterfaceId(String interfaceId) {
        return sqlQueryFactory.select(qTBusinessInterface).from(qTBusinessInterface)
                .where(qTBusinessInterface.requestInterfaceId.eq(interfaceId)).fetch();
    }

    /**
     * 根据服务注册id获取集成配置信息
     *
     * @param sysRegistryId
     */
    public List<TBusinessInterface> getListBySysRegistryId(String sysRegistryId) {
        return sqlQueryFactory.select(qTBusinessInterface).from(qTBusinessInterface)
                .where(qTBusinessInterface.sysRegistryId.eq(sysRegistryId)).fetch();
    }

    public List<TBusinessInterface> getListBySysRegistryId(List<String> sysRegistryIds) {
        return sqlQueryFactory.select(qTBusinessInterface).from(qTBusinessInterface)
                .where(qTBusinessInterface.sysRegistryId.in(sysRegistryIds)).fetch();
    }

    public List<TBusinessInterface> getListByRegSysIdList(List<String> sysIds) {
        return sqlQueryFactory.select(qTBusinessInterface).from(qTBusinessInterface)
                .leftJoin(qTSysRegistry).on(qTBusinessInterface.sysRegistryId.eq(qTSysRegistry.id))
                .where(qTSysRegistry.sysId.in(sysIds)).fetch();
    }

    /**
     * 根据请求方系统接口ID获取接口配置信息
     *
     * @param interfaceId
     */
    public TBusinessInterface getOneByInterfaceId(String interfaceId) {
        return sqlQueryFactory
                .select(Projections.bean(TBusinessInterface.class, qTBusinessInterface.requestInterfaceId))
                .from(qTBusinessInterface)
                .where(qTBusinessInterface.requestInterfaceId.eq(interfaceId)).fetchFirst();
    }

    /**
     * 获取集成配置信息列表
     *
     * @param list
     * @param pageNo
     * @param pageSize
     * @return
     */
    public QueryResults<TBusinessInterface> getInterfaceConfigureList(ArrayList<Predicate> list, Integer pageNo, Integer pageSize) {
        StringTemplate template = Expressions.stringTemplate("concat(string_agg (concat(concat(concat(concat({0},'/' ::TEXT),{1}),'/' ::TEXT),{2}), ',' :: TEXT ))",
                qTSys.sysName, qTBusinessInterface.businessInterfaceName, qTBusinessInterface.excErrOrder);
        QueryResults<TBusinessInterface> queryResults = sqlQueryFactory
                .select(Projections.bean(TBusinessInterface.class, qTBusinessInterface.id.min().as("id"), qTType.typeName.min().as(qTType.typeName),
                        qTBusinessInterface.requestInterfaceId,
                        qTInterface.interfaceName.min().as("requestInterfaceName"),
                        template.as(qTBusinessInterface.businessInterfaceName),
                        qTBusinessInterface.mockStatus.min().as(qTBusinessInterface.mockStatus),
                        qTBusinessInterface.status.min().as("status"),
                        qTBusinessInterface.createdBy.min().as(qTBusinessInterface.createdBy),
                        qTBusinessInterface.createdTime.min().as(qTBusinessInterface.createdTime),
                        qTBusinessInterface.updatedBy.min().as(qTBusinessInterface.updatedBy),
                        qTBusinessInterface.updatedTime.max().as(qTBusinessInterface.updatedTime)))
                .from(qTBusinessInterface)
                .leftJoin(qTSysRegistry).on(qTSysRegistry.id.eq(qTBusinessInterface.sysRegistryId))
                .leftJoin(qTSys).on(qTSys.id.eq(qTSysRegistry.sysId))
                .leftJoin(qTInterface).on(qTInterface.id.eq(qTBusinessInterface.requestInterfaceId))
                .leftJoin(qTType).on(qTType.id.eq(qTInterface.typeId))
                .where(list.toArray(new Predicate[list.size()]))
                .groupBy(qTBusinessInterface.requestInterfaceId)
                .limit(pageSize).offset((pageNo - 1) * pageSize)
                .orderBy(qTBusinessInterface.createdTime.as("createdTime").desc()).fetchResults();

        //处理其中requestInterfaceName  按照执行顺序组装
        List<TBusinessInterface> results = queryResults.getResults();
        if (CollectionUtils.isEmpty(results)) {
            return queryResults;
        }

        for (TBusinessInterface result : results) {
            String businessInterfaceName = result.getBusinessInterfaceName();
            String[] nameSplit = businessInterfaceName.split(",");
            //取出每个里面的
            List<String> strings = Arrays.asList(nameSplit).stream().sorted(Comparator.comparing(e -> e.substring(e.lastIndexOf("/")))).collect(Collectors.toList());
            strings = strings.stream().map(e -> e.substring(0, e.lastIndexOf("/"))).collect(Collectors.toList());
            result.setBusinessInterfaceName(String.join(",", strings));
        }

        return queryResults;
    }


    /**
     * 根据三条件获取
     *
     * @param interfaceId
     * @return
     */
    public List<TBusinessInterface> getTBusinessInterfaceList(String interfaceId) {
        List<TBusinessInterface> list = sqlQueryFactory.select(qTBusinessInterface).from(qTBusinessInterface)
                .where(qTBusinessInterface.requestInterfaceId.eq(interfaceId))
                .orderBy(qTBusinessInterface.excErrOrder.asc()).fetch();
        return list;
    }

    /**
     * 新增接口配置时根据条件判断是否存在该数据
     *
     * @param interfaceId
     * @return
     */
    public List<TBusinessInterface> getBusinessInterfaceIsExist(String interfaceId) {
        ArrayList<Predicate> list = new ArrayList<>();

        if (StringUtils.isNotEmpty(interfaceId)) {
            list.add(qTBusinessInterface.requestInterfaceId.eq(interfaceId));
        }

        List<TBusinessInterface> rtnList = sqlQueryFactory.select(qTBusinessInterface).from(qTBusinessInterface)
                .where(list.toArray(new Predicate[list.size()])).fetch();
        return rtnList;
    }

    /**
     * 根据插件表id获取对接接口配置数据
     *
     * @param pluginId
     * @return
     */
    public List<TBusinessInterface> getListByPluginId(String pluginId) {
        List<TBusinessInterface> list = sqlQueryFactory.select(qTBusinessInterface).from(qTBusinessInterface)
                .where(qTBusinessInterface.pluginId.eq(pluginId)).fetch();
        return list;
    }

    /**
     * 获取平台下的所有接口配置信息
     *
     * @param platformId
     */
    public List<TBusinessInterface> getListByPlatform(String platformId) {
        List<TBusinessInterface> list = null;
        return list;
    }

    public List<TBusinessInterface> getListByPlatforms(List<String> platformIds) {
        List<TBusinessInterface> list = null;
        return list;
    }

    /**
     * 根据id获取多接口配置下全部接口
     *
     * @param id
     * @return
     */
    public List<TBusinessInterface> busInterfaces(String id) {
        TBusinessInterface businessInterface = getOne(id);
        if (businessInterface == null) {
            throw new RuntimeException("没有找到集成配置");
        }
        List<TBusinessInterface> interfaces = sqlQueryFactory.select(qTBusinessInterface).from(qTBusinessInterface)
                .where(qTBusinessInterface.requestInterfaceId.eq(businessInterface.getRequestInterfaceId())).orderBy(qTBusinessInterface.excErrOrder.asc()).fetch();
        if (CollectionUtils.isEmpty(interfaces)) {
            throw new RuntimeException("没有找到多服务配置集合");
        }
        return interfaces;
    }

    /**
     * 根据id，获取id的list
     *
     * @return
     */
    private List<String> busInterfaceIds(String id) {
        // 获取id集合
        List<TBusinessInterface> interfaces = busInterfaces(id);
        return interfaces.stream().map(TBusinessInterface::getId).collect(Collectors.toList());
    }

    /**
     * 根据id集合获取集成配置信息
     *
     * @return
     */
    public List<TBusinessInterface> busInterfaceIdList(List<String> idList) {
        StringTemplate template = Expressions.stringTemplate("concat(string_agg ( concat(concat({0},'/' ::TEXT),{1}), ',' :: TEXT ))", qTSys.sysName, qTBusinessInterface.businessInterfaceName);
        List<TBusinessInterface> interfaceList = sqlQueryFactory
                .select(Projections.bean(TBusinessInterface.class, qTBusinessInterface.id.min().as("id"), qTType.typeName.min().as(qTType.typeName),
                        qTBusinessInterface.requestInterfaceId,
                        qTInterface.interfaceName.min().as("requestInterfaceName"),
                        template.as(qTBusinessInterface.businessInterfaceName),
                        qTBusinessInterface.mockStatus.min().as(qTBusinessInterface.mockStatus),
                        qTBusinessInterface.status.min().as("status"),
                        qTBusinessInterface.createdBy.min().as(qTBusinessInterface.createdBy),
                        qTBusinessInterface.createdTime.max().as(qTBusinessInterface.createdTime),
                        qTBusinessInterface.updatedBy.min().as(qTBusinessInterface.updatedBy),
                        qTBusinessInterface.updatedTime.max().as(qTBusinessInterface.updatedTime)))
                .from(qTBusinessInterface)
                .leftJoin(qTSysRegistry).on(qTSysRegistry.id.eq(qTBusinessInterface.sysRegistryId))
                .leftJoin(qTSys).on(qTSys.id.eq(qTSysRegistry.sysId))
                .leftJoin(qTInterface).on(qTInterface.id.eq(qTBusinessInterface.requestInterfaceId))
                .leftJoin(qTType).on(qTType.id.eq(qTInterface.typeId))
                .where(qTBusinessInterface.requestInterfaceId.in(idList))
                .groupBy(qTBusinessInterface.requestInterfaceId)
                .fetch();
        if (CollectionUtils.isEmpty(interfaceList)) {
            throw new RuntimeException("没有找到集成配置");
        }
        return interfaceList;
    }

    public long selectByQIId(String QIId) {
        return sqlQueryFactory.select(qTBusinessInterface).from(qTBusinessInterface).fetchCount();
    }

    /**
     * 根据插件查找集成配置
     *
     * @param pluginId
     */
    public List<TBusinessInterface> getByPlugin(String pluginId) {
        return sqlQueryFactory.select(qTBusinessInterface).from(qTBusinessInterface).where(qTBusinessInterface.pluginId.eq(pluginId)).fetch();
    }

    private void cacheDelete(String id) {
        TBusinessInterface businessInterface = this.getOne(id);
        CacheDeleteDto keyDto = new CacheDeleteDto();
        keyDto.setInterfaceIds(Arrays.asList(businessInterface.getRequestInterfaceId()));
        //需要删除下面两种缓存key
        keyDto.setCacheTypeList(Arrays.asList(
                Constant.CACHE_KEY_PREFIX.COMMON_TYPE
        ));

        cacheDeleteService.cacheKeyDelete(keyDto);
    }
}
