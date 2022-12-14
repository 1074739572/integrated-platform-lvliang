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
 * ??????????????????
 *
 * @author weihe9
 * @date 2020/12/13 20:40
 */
@Service
@Api(tags = "??????????????????")
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

    @ApiOperation(value = "??????mock??????", notes = "??????mock??????")
    @PostMapping("/updateMockStatus")
    @Transactional(rollbackFor = Exception.class)
    public ResultDto<String> updateMockStatus(
            @ApiParam(value = "????????????id") @RequestParam(value = "id", required = true) String id,
            @ApiParam(value = "??????????????????") @RequestParam(value = "mockStatus", required = true) String mockStatus,
            @RequestParam("loginUserName") String loginUserName) {
        // ?????????????????????????????????
        if (StringUtils.isBlank(loginUserName)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "???????????????????????????!");
        }
        // ?????????????????????????????????id??????
        List<String> idList = busInterfaceIds(id);
        //redis??????????????????
        ArrayList<Predicate> arr = new ArrayList<>();
        arr.add(qTBusinessInterface.id.in(idList));
        List<RedisKeyDto> redisKeyDtoList = redisService.getRedisKeyDtoList(arr);

        long size = sqlQueryFactory.update(qTBusinessInterface).set(qTBusinessInterface.mockStatus, mockStatus)
                .set(qTBusinessInterface.updatedTime, new Date()).set(qTBusinessInterface.updatedBy, loginUserName)
                .where(qTBusinessInterface.id.in(idList)).execute();
        // ????????????????????????
        if (idList.size() != size) {
            throw new RuntimeException("??????mock????????????!");
        }
        //????????????
        cacheDelete(id);
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "??????mock????????????!", new RedisDto(redisKeyDtoList).toString());
    }

    @ApiOperation(value = "????????????????????????", notes = "????????????????????????")
    @PostMapping("/updateStatus")
    @Transactional(rollbackFor = Exception.class)
    public ResultDto<String> updateStatus(
            @ApiParam(value = "????????????") @RequestParam(value = "id", required = true) String id,
            @ApiParam(value = "??????????????????") @RequestParam(value = "status", required = true) String status,
            @RequestParam("loginUserName") String loginUserName) {
        // ?????????????????????????????????
        if (StringUtils.isBlank(loginUserName)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "???????????????????????????!");
        }
        // ?????????????????????????????????id??????
        List<String> idList = busInterfaceIds(id);
        //redis??????????????????
        ArrayList<Predicate> arr = new ArrayList<>();
        arr.add(qTBusinessInterface.id.in(idList));
        List<RedisKeyDto> redisKeyDtoList = redisService.getRedisKeyDtoList(arr);

        long size = sqlQueryFactory.update(qTBusinessInterface).set(qTBusinessInterface.status, status)
                .set(qTBusinessInterface.updatedTime, new Date()).set(qTBusinessInterface.updatedBy, loginUserName)
                .where(qTBusinessInterface.id.in(idList)).execute();
        // ????????????????????????
        if (idList.size() != size) {
            throw new RuntimeException("???????????????????????????!");
        }
        cacheDelete(id);
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "??????????????????????????????!", new RedisDto(redisKeyDtoList).toString());
    }

    @ApiOperation(value = "??????mock??????", notes = "??????mock??????")
    @GetMapping("/getMockTemplate")
    public ResultDto<List<MockTemplateDto>> getMockTemplate(
            @ApiParam(value = "????????????id") @RequestParam(value = "id", required = true) String id) {
        List<TBusinessInterface> interfaces = busInterfaces(id);
        if (CollectionUtils.isEmpty(interfaces)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "??????id????????????????????????");
        }
        List<MockTemplateDto> dtoList = new ArrayList<>();
        for (TBusinessInterface businessInterface : interfaces) {
            String mocktpl = businessInterface.getMockTemplate();
            MockTemplateDto dto = new MockTemplateDto(businessInterface.getId(),
                    // ??????mock???????????????????????????????????????????????????mock??????
                    (StringUtils.isNotBlank(mocktpl) && !"null".equalsIgnoreCase(mocktpl)) ? mocktpl
                            : businessInterface.getOutParamFormat(),
                    businessInterface.getMockIsUse(), businessInterface.getExcErrOrder(),
                    businessInterface.getBusinessInterfaceName());
            dtoList.add(dto);
        }
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "??????mock????????????!", dtoList);
    }

    @ApiOperation(value = "??????mock??????", notes = "??????mock??????")
    @PostMapping("/saveMockTemplate")
    @Transactional(rollbackFor = Exception.class)
    public ResultDto<String> saveMockTemplate(@RequestBody List<MockTemplateDto> dtoList, @RequestParam("loginUserName") String loginUserName) {
        // ?????????????????????????????????
        if (StringUtils.isBlank(loginUserName)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "???????????????????????????!");
        }
        if (CollectionUtils.isEmpty(dtoList)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "???????????????mock??????!");
        }
        // ????????????????????????
        List<String> rtnStr = new ArrayList<>();
        for (MockTemplateDto dto : dtoList) {
            // ????????????????????????
            ValidationResult validationResult = validatorHelper.validate(dto);
            if (validationResult.isHasErrors()) {
                return new ResultDto<>(Constant.ResultCode.ERROR_CODE, validationResult.getErrorMsg(),
                        validationResult.getErrorMsg());
            }
            // ??????mock????????????????????????
            PlatformUtil.strIsJsonOrXml(dto.getMockTemplate());

            //????????????
            cacheDelete(dto.getId());
            long lon =  sqlQueryFactory.update(qTBusinessInterface).set(qTBusinessInterface.mockTemplate, dto.getMockTemplate())
                    .set(qTBusinessInterface.mockIsUse, dto.getMockIsUse()).set(qTBusinessInterface.updatedTime, new Date())
                    .set(qTBusinessInterface.updatedBy, loginUserName).where(qTBusinessInterface.id.eq(dto.getId())).execute();


            if (lon <= 0) {
                throw new RuntimeException("??????mock????????????!");
            }
            rtnStr.add(dto.getId());
        }
        //redis??????????????????
        ArrayList<Predicate> arr = new ArrayList<>();
        arr.add(qTBusinessInterface.id.in(rtnStr));
        List<RedisKeyDto> redisKeyDtoList = redisService.getRedisKeyDtoList(arr);
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "??????mock????????????!", new RedisDto(redisKeyDtoList).toString());
    }

    @ApiOperation(value = "?????????????????????????????????", notes = "?????????????????????????????????")
    @GetMapping("/getInterfaceDebug")
    public ResultDto<InDebugResDto> getInterfaceDebug(
            @ApiParam(value = "????????????id") @RequestParam(value = "id", required = true) String id) {
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
                return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "?????????????????????????????????");
            }
            // ??????????????????
            TBusinessInterface businessInterface = businessInterfaces.get(0);
            String interfaceId = StringUtils.isNotEmpty(businessInterface.getRequestInterfaceId())
                    ? businessInterface.getRequestInterfaceId()
                    : "";
            // ????????????
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
                logger.info("??????wsUrl====={}", wsUrl);
                List<String> wsOperationNames = PlatformUtil.getWsdlOperationNames(wsUrl + businessInterface.getSysCode());
                resDto.setWsOperationNames(wsOperationNames);
                resDto.setSysIntfParamFormatType("2");
                logger.info("??????wsUrl====={}", wsUrl);
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
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "????????????????????????????????????!", resDto);
        } catch (Exception e) {
            logger.error("????????????????????????????????????! MSG:{}", ExceptionUtil.dealException(e));
            e.printStackTrace();
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "????????????????????????????????????!");
        }
    }

    @ApiOperation(value = "????????????????????????")
    @GetMapping("/getInterfaceConfigureList")
    public ResultDto<TableData<TBusinessInterface>> getInterfaceConfigureList(
            @ApiParam(value = "??????id") @RequestParam(value = "typeId", required = false) Integer typeId,
            @ApiParam(value = "????????????") @RequestParam(value = "interfaceName", required = false) String interfaceName,
            @ApiParam(value = "??????id") @RequestParam(value = "interfaceId", required = false) String interfaceId,
            @ApiParam(value = "??????", example = "1") @RequestParam(value = "pageNo", defaultValue = "1", required = false) Integer pageNo,
            @ApiParam(value = "????????????", example = "10") @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {

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
        // ??????????????????????????????
        QueryResults<TBusinessInterface> queryResults = getInterfaceConfigureList(predicateList, pageNo, pageSize);
        // ??????
        TableData<TBusinessInterface> tableData = new TableData<>(queryResults.getTotal(), queryResults.getResults());
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "????????????????????????????????????", tableData);
    }

    @ApiOperation(value = "????????????????????????")
    @GetMapping("/getInterfaceConfigInfoById")
    public ResultDto<BusinessInterfaceDto> getInterfaceConfigInfoById(
            @ApiParam(value = "????????????id") @RequestParam(value = "id", required = true) String id) {
        // ????????????
        BusinessInterfaceDto dto = new BusinessInterfaceDto();
        dto.setId(id);

        // ????????????????????????
        List<TBusinessInterface> tbiList = new ArrayList<>();

        // ????????????????????????
        TBusinessInterface tbi = getOne(id);
        if (tbi != null) {
            // ?????????????????????
            String requestInterfaceId = tbi.getRequestInterfaceId();
            dto.setRequestInterfaceId(requestInterfaceId);
            dto.setInterfaceSlowFlag(tbi.getInterfaceSlowFlag());
            dto.setReplayFlag(tbi.getReplayFlag());
            dto.setMockStatus(tbi.getMockStatus());
            dto.setStatus(tbi.getStatus());
            // ???????????????????????????
            ArrayList<Predicate> list = new ArrayList<>();
            if (StringUtils.isNotEmpty(requestInterfaceId)) {
                list.add(qTInterface.id.eq(requestInterfaceId));
            }
            TInterface tInterface = sqlQueryFactory.select(qTInterface).from(qTInterface)
                    .where(list.toArray(new Predicate[list.size()])).fetchOne();
            dto.setRequestInterfaceTypeId(tInterface.getTypeId());
            // ??????????????????????????????
            tbiList = getTBusinessInterfaceList(tbi.getRequestInterfaceId());
        }

        dto.setBusinessInterfaceList(tbiList);

        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "??????????????????????????????", dto);
    }


    @ApiOperation(value = "????????????????????????????????????", notes = "????????????????????????????????????")
    @GetMapping("/getBusItfInfo")
    public ResultDto getBusItfInfo(
            @ApiParam(value = "?????????????????????ID") @RequestParam(value = "reqItfId", required = true) String reqItfId, @RequestParam("loginUserName") String loginUserName) {
        //????????????
        if (StringUtils.isBlank(reqItfId)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "????????????????????????!");
        }
        // ?????????????????????????????????
        if (StringUtils.isBlank(loginUserName)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "???????????????????????????!");
        }
        TBusinessInterface tb = getOneByInterfaceId(reqItfId);
        Map resMap = new HashMap();
        resMap.put("replayFlag", tb.getReplayFlag());
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "??????????????????", resMap);
    }

    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "??????/??????????????????", notes = "??????/??????????????????")
    @PostMapping("/saveAndUpdateInterfaceConfig")
    public ResultDto<String> saveAndUpdateInterfaceConfig(@RequestBody BusinessInterfaceDto dto, @RequestParam("loginUserName") String loginUserName) {
        if (dto == null) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "????????????????????????!");
        }
        if (StringUtils.isBlank(dto.getRequestInterfaceId())) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "???????????????????????????!");
        }
        // ?????????????????????????????????
        if (StringUtils.isBlank(loginUserName)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "???????????????????????????!");
        }

        if (Constant.Operation.ADD.equals(dto.getAddOrUpdate())) {
            return this.saveInterfaceConfig(dto, loginUserName);
        }
        if (Constant.Operation.UPDATE.equals(dto.getAddOrUpdate())) {
            return this.updateInterfaceConfig(dto, loginUserName, true);
        }
        //??????
        if ("3".equals(dto.getAddOrUpdate())) {
            return this.updateInterfaceConfig(dto, loginUserName, false);
        }
        return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "addOrUpdate ???????????????????????????!", null);
    }

    /**
     * ??????????????????
     *
     * @param dto
     * @param loginUserName
     * @return
     */
    private ResultDto<String> saveInterfaceConfig(BusinessInterfaceDto dto, String loginUserName) {
        //???????????????
        TInterface tInterface = interfaceService.getOne(dto.getRequestInterfaceId());
        if (tInterface == null) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "?????????????????????!", null);
        }

        // ???????????????????????????????????????
        List<TBusinessInterface> tbiList = getBusinessInterfaceIsExist(dto.getRequestInterfaceId());
        if (CollectionUtils.isNotEmpty(tbiList)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "?????????????????????id?????????????????????????????????!", null);
        }

        tbiList = dto.getBusinessInterfaceList();
        String returnId = "";
        for (int i = 0; i < tbiList.size(); i++) {
            TBusinessInterface tbi = tbiList.get(i);
            // ??????????????????
            tbi.setId(batchUidService.getUid(qTBusinessInterface.getTableName()) + "");
            tbi.setRequestInterfaceId(dto.getRequestInterfaceId());
            tbi.setStatus(Constant.Status.START);
            tbi.setMockStatus(Constant.Status.STOP);
            tbi.setCreatedTime(new Date());
            tbi.setCreatedBy(loginUserName);
            tbi.setExcErrOrder(i);
            tbi.setInterfaceSlowFlag(dto.getInterfaceSlowFlag());
            tbi.setReplayFlag(dto.getReplayFlag());
            // ??????schema
            niFiRequestUtil.generateSchemaToInterface(tbi);
            // ??????????????????
            this.post(tbi);
            if (StringUtils.isBlank(returnId)) {
                returnId = tbi.getId();
            }
        }
        // redis????????????
        cacheDelete(returnId);
        Map<String, String> data = new HashMap<String, String>();
        data.put("id", returnId);
        return new ResultDto<String>(Constant.ResultCode.SUCCESS_CODE, "????????????????????????", JSON.toJSONString(data));
    }

    /**
     * ??????????????????
     *
     * @param dto
     * @param loginUserName
     * @return
     */
    private ResultDto updateInterfaceConfig(BusinessInterfaceDto dto, String loginUserName, boolean insertHisFlg) {
        //????????????????????????
        Boolean isModReq = false;
        // ???????????????????????????????????????
        TBusinessInterface exsitsBI = this.getOne(dto.getId());
        if (exsitsBI != null) {
            //?????????????????????
            if (exsitsBI.getRequestInterfaceId().equals(dto.getRequestInterfaceId())) {
//				???????????????????????????????????????
            } else {
                isModReq = true;
                List<TBusinessInterface> tbiList = getBusinessInterfaceIsExist(dto.getRequestInterfaceId());
                if (CollectionUtils.isNotEmpty(tbiList)) {
                    return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "???????????????id?????????????????????????????????!", null);
                }
            }
        }

        //????????????????????????recordId
        String lastRecordId = dto.getRequestInterfaceId();
        String oldRecordId = exsitsBI.getRequestInterfaceId();
        if (isModReq) {
            //?????????
            historyService.updateRecordId(oldRecordId, lastRecordId);
        } else {
            //?????????
        }

        if (insertHisFlg) {
            //??????history
            List<TBusinessInterface> list = getListByCondition(exsitsBI.getRequestInterfaceId());
            String businessInterfaceName = "";
            Integer interfaceSlowFlag = null;
            Integer replayFlag = null;
            String requestInterfaceName = "";
            String typeId = "";
            String requestSysId = "";
            for (int i = 0; i < list.size(); i++) {
                TBusinessInterface tbi = list.get(i);
                //??????????????????
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

            //??????????????????
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

        // ?????????????????????????????????
        cacheDelete(dto.getId());

        List<String> rtnId = new ArrayList<>();
        List<TBusinessInterface> tbiList = dto.getBusinessInterfaceList();

        //??????????????????+???????????????   ??????????????????????????????????????????????????????????????????+?????????????????????
        updateSeq(tbiList);

        for (int i = 0; i < tbiList.size(); i++) {
            TBusinessInterface tbi = tbiList.get(i);
            if (StringUtils.isBlank(tbi.getId())) {
                // ?????????????????????
                tbi.setId(batchUidService.getUid(qTBusinessInterface.getTableName()) + "");
                tbi.setRequestInterfaceId(dto.getRequestInterfaceId());
                tbi.setStatus(exsitsBI.getStatus());
                tbi.setMockStatus(exsitsBI.getMockStatus());
                tbi.setCreatedTime(new Date());
                tbi.setCreatedBy(loginUserName);
                tbi.setExcErrOrder(i);
                tbi.setInterfaceSlowFlag(dto.getInterfaceSlowFlag());
                tbi.setReplayFlag(dto.getReplayFlag());
                // ??????schema
                niFiRequestUtil.generateSchemaToInterface(tbi);
                // ??????????????????
                this.post(tbi);
            } else {
                // ????????????????????????
                tbi.setRequestInterfaceId(dto.getRequestInterfaceId());
                tbi.setUpdatedTime(new Date());
                tbi.setUpdatedBy(loginUserName);
                tbi.setExcErrOrder(i);
                tbi.setInterfaceSlowFlag(dto.getInterfaceSlowFlag());
                tbi.setReplayFlag(dto.getReplayFlag());
                // ??????schema
                niFiRequestUtil.generateSchemaToInterface(tbi);
                // ??????????????????
                long l = this.put(tbi.getId(), tbi);
                if (l < 1) {
                    throw new RuntimeException("????????????????????????????????????!");
                }
                rtnId.add(tbi.getId());
            }
        }
        // ?????????????????????????????????
        cacheDelete(dto.getId());
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "????????????????????????", null);
    }

    private void updateSeq(List<TBusinessInterface> tbiList) {
        List<String> list = tbiList.stream().map(TBusinessInterface::getId).collect(Collectors.toList());
        //???????????????????????????  ????????????????????????
        if (CollectionUtils.isEmpty(list) || list.size() < 2) {
            return;
        }

        sqlQueryFactory.update(qTBusinessInterface).set(qTBusinessInterface.excErrOrder, qTBusinessInterface.excErrOrder.add(list.size()))
                .where(qTBusinessInterface.id.in(list)).execute();
    }

    @ApiOperation(value = "????????????????????????jolt", notes = "????????????????????????jolt")
    @PostMapping("/paramFormatJolt")
    public ResultDto<String> paramFormatJolt(String paramFormat, String content,
                                             @RequestParam(value = "joltType", defaultValue = "request", required = false) String joltType) {
        String contentType = Constant.ParamFormatType.getByType(content);
        if (StringUtils.isBlank(contentType) || Constant.ParamFormatType.NONE.getType().equals(contentType)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "??????????????????!", "??????????????????!");
        }
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "jolt????????????!",
                niFiRequestUtil.generateJolt(paramFormat, contentType, joltType));
    }

    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "??????????????????", notes = "??????????????????")
    @PostMapping("/deleteInterfaceConfigure")
    public ResultDto<String> deleteInterfaceConfigure(
            @ApiParam(value = "????????????id") @RequestParam(value = "id", required = true) String id) {
        TBusinessInterface tbi = this.getOne(id);
        if (tbi == null) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "?????????id??????????????????????????????!", "?????????id??????????????????????????????!");
        }
        List<TBusinessInterface> list = getListByCondition(tbi.getRequestInterfaceId());
        // ??????????????????id
        List<String> rtnStr = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(list)) {
            for (TBusinessInterface obj : list) {
                rtnStr.add(obj.getId());
            }
        }
        // ??????????????????
        cacheDelete(id);

        // ??????????????????????????????
        long count = delObjByCondition(tbi.getRequestInterfaceId());
        if (count <= 0) {
            throw new RuntimeException("????????????????????????!");
        }
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "????????????????????????!?????????" + count + "?????????",
                null);
    }

    @ApiOperation(value = "??????id??????????????????????????????", notes = "??????id??????????????????????????????")
    @PostMapping("/deleteBusinessInterfaceById")
    public ResultDto<String> deleteBusinessInterfaceById(
            @ApiParam(value = "????????????id") @RequestParam(value = "id", required = true) String id) {
        TBusinessInterface tbi = this.getOne(id);
        if (tbi == null) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "??????id??????????????????????????????!", id);
        }

        //????????????
        cacheDelete(id);

        long count = this.delete(id);
        if (count < 1) {
            throw new RuntimeException("??????id?????????????????????????????????!");
        }

        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "????????????????????????????????????!", null);
    }
    @ApiOperation(value = "????????????????????????????????????")
    @PostMapping("/getInterfaceDebugger")
    public ResultDto<String> getInterfaceDebugger(String interfaceId) {
        if (StringUtils.isBlank(interfaceId)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "???????????????id??????");
        }
        try {
            TInterface inter = sqlQueryFactory.select(qTInterface).from(qTInterface).where(qTInterface.id.eq(interfaceId)).fetchFirst();
            if (inter == null) {
                return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "????????????????????????????????????????????????????????????????????????");
            }
            String paramJson = inter.getInParamFormat();
            if ("2".equals(inter.getInParamFormatType())) {
                paramJson = niFiRequestUtil.xml2json(paramJson);
            }
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "JSON??????????????????????????????!", paramJson);
        } catch (Exception e) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "??????????????????????????????!");
        }
    }

    @ApiOperation(value = "??????????????????????????????")
    @PostMapping("/interfaceDebugger")
    public ResultDto<Object> interfaceDebugger(@RequestBody JoltDebuggerDto dto) {
        // ????????????????????????
        ValidationResult validationResult = validatorHelper.validate(dto);
        if (validationResult.isHasErrors()) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, validationResult.getErrorMsg());
        }
        if (dto.getJolt() == null && dto.getJslt() == null) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "jolt?????????jslt???????????????????????????");
        }
        if (dto.getJolt() != null && dto.getJslt() != null) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "jolt?????????jslt??????????????????????????????");
        }
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "", niFiRequestUtil.joltDebugger2(dto));
    }

    @ApiOperation(value = "???????????????????????????")
    @GetMapping("/downloadInterfaceConfigs/{Ids}")
    public void getSqlConfig(HttpServletResponse response, @ApiParam("??????Id") @PathVariable String Ids) {
        String[] businessInterfaceIds = Ids.split(",");
        StringBuilder sqlStringBuffer = new StringBuilder();
        this.getResourcesByBizInterfaceIds(Arrays.asList(businessInterfaceIds), sqlStringBuffer);

        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String sqlName = "interface_" + dateStr + ".sql";
        try {
            response.setContentType("application/x-msdownload");
            response.setHeader("content-disposition", "attachment;filename=" + URLEncoder.encode("interface_" + dateStr + ".zip", "utf-8"));

            String fileName = sqlName; // ??????zip?????????
            byte[] file = sqlStringBuffer.toString().getBytes(StandardCharsets.UTF_8); // ??????zip???????????????
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
    @ApiOperation(value = "?????????????????????", notes = "?????????????????????")
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
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "??????????????????");
        }
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "", result);
    }

    @PostMapping(path = "/uploadInterFaceSql")
    public ResultDto<String> uploadInterFaceSql(@RequestParam("sqlFiles") MultipartFile[]
                                                        sqlFiles, @RequestParam("loginUserName") String loginUserName) {
        //?????????????????????????????????
        if (org.apache.commons.lang3.StringUtils.isBlank(loginUserName)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "???????????????????????????!", "???????????????????????????!");
        }
        //?????????????????????
        StringBuilder message = new StringBuilder();
        try (Connection connection = sqlQueryFactory.getConnection();) {

            //???????????????????????????
            if (sqlFiles == null || sqlFiles.length == 0) {
                return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "???????????????????????????!", "???????????????????????????!");
            }
            //sql??????sql??????
            int insetNum = 0;
            for (MultipartFile file : sqlFiles) {
                try (
                        Statement statement = connection.createStatement();
                        InputStream is = file.getInputStream();
                        InputStreamReader inputStreamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
                ) {
                    StringBuilder sql = new StringBuilder();
                    connection.setAutoCommit(false);//???????????????
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);//????????????????????????
                    String lineText = "";
                    while ((lineText = bufferedReader.readLine()) != null) {
                        sql.append(lineText).append("\r\n");
                    }
                    //???sys_config???????????????id????????????id????????????
                    sql = new StringBuilder(sql.toString());
                    String[] sqls = sql.toString().split("END_OF_SQL");
                    for (String str : sqls) {
                        if (str.trim().startsWith("INSERT") || str.trim().startsWith("REPLACE") || str.trim().startsWith("delete"))
                            statement.addBatch(str);
                    }
                    //????????????????????????????????????
                    statement.executeBatch();
                    connection.commit();
                    //??????SQL??????
                    statement.clearBatch();
                    insetNum++;
                } catch (Exception e) {
                    connection.rollback();
                    message.append(e.getMessage());
                }
            }
            if (insetNum == sqlFiles.length) {
                return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "sql????????????????????????", insetNum + "");
            } else {
                return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "sql????????????????????????" + message, insetNum + "");
            }
        } catch (Exception e) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "??????sql????????????", e.getLocalizedMessage());
        }
    }

    /**
     * ??????????????????????????????????????????
     *
     * @param interfaceId
     */
    public long delObjByCondition(String interfaceId) {
        long count = sqlQueryFactory.delete(qTBusinessInterface).where(qTBusinessInterface.requestInterfaceId
                .eq(interfaceId)).execute();
        return count;
    }

    /**
     * ????????????????????????????????????????????????
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

                //????????????

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
     * ??????????????????id??????????????????id(??????????????????????????????)
     *
     * @param interfaceId
     */
    public List<TBusinessInterface> getListByInterfaceId(String interfaceId) {
        return sqlQueryFactory.select(qTBusinessInterface).from(qTBusinessInterface)
                .where(qTBusinessInterface.requestInterfaceId.eq(interfaceId)).fetch();
    }

    /**
     * ??????????????????id????????????????????????
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
     * ???????????????????????????ID????????????????????????
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
     * ??????????????????????????????
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

        //????????????requestInterfaceName  ????????????????????????
        List<TBusinessInterface> results = queryResults.getResults();
        if (CollectionUtils.isEmpty(results)) {
            return queryResults;
        }

        for (TBusinessInterface result : results) {
            String businessInterfaceName = result.getBusinessInterfaceName();
            String[] nameSplit = businessInterfaceName.split(",");
            //?????????????????????
            List<String> strings = Arrays.asList(nameSplit).stream().sorted(Comparator.comparing(e -> e.substring(e.lastIndexOf("/")))).collect(Collectors.toList());
            strings = strings.stream().map(e -> e.substring(0, e.lastIndexOf("/"))).collect(Collectors.toList());
            result.setBusinessInterfaceName(String.join(",", strings));
        }

        return queryResults;
    }


    /**
     * ?????????????????????
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
     * ????????????????????????????????????????????????????????????
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
     * ???????????????id??????????????????????????????
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
     * ??????????????????????????????????????????
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
     * ??????id????????????????????????????????????
     *
     * @param id
     * @return
     */
    public List<TBusinessInterface> busInterfaces(String id) {
        TBusinessInterface businessInterface = getOne(id);
        if (businessInterface == null) {
            throw new RuntimeException("????????????????????????");
        }
        List<TBusinessInterface> interfaces = sqlQueryFactory.select(qTBusinessInterface).from(qTBusinessInterface)
                .where(qTBusinessInterface.requestInterfaceId.eq(businessInterface.getRequestInterfaceId())).orderBy(qTBusinessInterface.excErrOrder.asc()).fetch();
        if (CollectionUtils.isEmpty(interfaces)) {
            throw new RuntimeException("?????????????????????????????????");
        }
        return interfaces;
    }

    /**
     * ??????id?????????id???list
     *
     * @return
     */
    private List<String> busInterfaceIds(String id) {
        // ??????id??????
        List<TBusinessInterface> interfaces = busInterfaces(id);
        return interfaces.stream().map(TBusinessInterface::getId).collect(Collectors.toList());
    }

    /**
     * ??????id??????????????????????????????
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
            throw new RuntimeException("????????????????????????");
        }
        return interfaceList;
    }

    public long selectByQIId(String QIId) {
        return sqlQueryFactory.select(qTBusinessInterface).from(qTBusinessInterface).fetchCount();
    }

    /**
     * ??????????????????????????????
     *
     * @param pluginId
     */
    public List<TBusinessInterface> getByPlugin(String pluginId) {
        return sqlQueryFactory.select(qTBusinessInterface).from(qTBusinessInterface).where(qTBusinessInterface.pluginId.eq(pluginId)).fetch();
    }

    public void cacheDelete(String id) {
        TBusinessInterface businessInterface = this.getOne(id);
        CacheDeleteDto keyDto = new CacheDeleteDto();
        keyDto.setInterfaceIds(Arrays.asList(businessInterface.getRequestInterfaceId()));
        //??????????????????????????????key
        keyDto.setCacheTypeList(Arrays.asList(
                Constant.CACHE_KEY_PREFIX.COMMON_TYPE
        ));

        cacheDeleteService.cacheKeyDelete(keyDto);
    }
}
