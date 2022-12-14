package com.iflytek.integrated.platform.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
import com.iflytek.integrated.platform.entity.TInterface;
import com.iflytek.integrated.platform.entity.TInterfaceParam;
import com.iflytek.integrated.platform.entity.TPlugin;
import com.iflytek.integrated.platform.entity.TSys;
import com.iflytek.integrated.platform.entity.TSysDriveLink;
import com.iflytek.integrated.platform.entity.TSysRegistry;
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
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
 * ????????????
 *
 * @author weihe9
 * @date 2020/12/12 17:16
 */
@Slf4j
@Api(tags = "??????????????????")
@RestController
@RequestMapping("/{version}/pt/interfaceManage")
public class InterfaceService extends BaseService<TInterface, String, StringPath> {

    @Autowired
    private BusinessInterfaceService businessInterfaceService;
    @Autowired
    private InterfaceParamService interfaceParamService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private CacheDeleteService cacheDeleteService;
    @Autowired
    private BatchUidService batchUidService;
    @Autowired
    private NiFiRequestUtil niFiRequestUtil;


    private static final Logger logger = LoggerFactory.getLogger(InterfaceService.class);

    public InterfaceService() {
        super(qTInterface, qTInterface.id);
    }


    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "????????????", notes = "????????????")
    @PostMapping("/delInterfaceById/{id}")
    public ResultDto<String> delInterfaceById(
            @ApiParam(value = "??????id") @PathVariable(value = "id", required = true) String id) {
        List<TBusinessInterface> list = businessInterfaceService.getListByInterfaceId(id);
        if (CollectionUtils.isNotEmpty(list)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "???????????????????????????????????????,????????????!", "???????????????????????????????????????,????????????!");
        }
        //????????????
        cacheDelete(id);
        // ????????????
        long l = this.delete(id);
        if (l < 1) {
            throw new RuntimeException("????????????????????????!");
        }
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "????????????????????????!", null);
    }

    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "??????????????????/??????", notes = "??????????????????/??????")
    @PostMapping("/saveAndUpdateInterface")
    public ResultDto<String> saveAndUpdateInterface(@RequestBody InterfaceDto dto, @RequestParam("loginUserName") String loginUserName) {
        if (dto == null) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "??????????????????!", "??????????????????!");
        }
        // ?????????????????????????????????
        if (StringUtils.isBlank(loginUserName)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "???????????????????????????!", "???????????????????????????!");
        }
        String interfaceName = dto.getInterfaceName();
        if (StringUtils.isBlank(interfaceName)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "???????????????!", "???????????????!");
        }
        String interfaceUrl = dto.getInterfaceUrl();
        if (StringUtils.isBlank(interfaceUrl)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "??????????????????!", "??????????????????!");
        }
        String id = dto.getId();
        if (StringUtils.isBlank(id)) {
            return this.saveInterface(dto, loginUserName);
        }
        return this.updateInterface(dto, loginUserName, true);
    }

    /**
     * ??????????????????
     */
    private ResultDto saveInterface(InterfaceDto dto, String loginUserName) {
        String interfaceUrl = dto.getInterfaceUrl();
        if (null != this.getInterfaceByUrl(interfaceUrl)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "????????????????????????!", "????????????????????????!");
        }
        // ??????
        List<TInterfaceParam> outParamList = dto.getOutParamList();
        if (CollectionUtils.isEmpty(outParamList)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "??????????????????!", "??????????????????!");
        }

        TInterface ti = new TInterface();
        // ???????????????????????????????????????json??????xml
        // ?????????????????????
        String inParamFormat = dto.getInParamFormat();
        String inParamFormatType = dto.getInParamFormatType();
        if (StringUtils.isNotBlank(inParamFormat)) {
            PlatformUtil.strIsJsonOrXml(inParamFormat);
            String inParamSchema = niFiRequestUtil.generateSchemaToInterface(inParamFormat, inParamFormatType);
            ti.setInParamSchema(inParamSchema);
        }
        // ??????????????????
        String outParamFormat = dto.getOutParamFormat();
        String outParamFormatType = dto.getOutParamFormatType();
        PlatformUtil.strIsJsonOrXml(outParamFormat);
        String outParamSchema = niFiRequestUtil.generateSchemaToInterface(outParamFormat, outParamFormatType);
        ti.setOutParamSchema(outParamSchema);
        // ??????????????????
        String interfaceId = batchUidService.getUid(qTInterface.getTableName()) + "";
        ti.setId(interfaceId);
        ti.setInParamFormatType(inParamFormatType);
        ti.setOutParamFormatType(outParamFormatType);
        ti.setInterfaceName(dto.getInterfaceName());
        ti.setTypeId(dto.getTypeId());
        ti.setInterfaceUrl(dto.getInterfaceUrl());
        ti.setInParamFormat(inParamFormat);
        ti.setOutParamFormat(outParamFormat);
        ti.setCreatedTime(new Date());
        ti.setCreatedBy(loginUserName);
        ti.setAllowLogDiscard(dto.getAllowLogDiscard());
        ti.setInterfaceType(dto.getInterfaceType());
        ti.setEncryptionType(dto.getEncryptionType());
        ti.setMaskPosStart(dto.getMaskPosStart());
        ti.setMaskPosEnd(dto.getMaskPosEnd());

        // ??????????????????
        // ??????
        List<TInterfaceParam> inParamList = dto.getInParamList();
        if (CollectionUtils.isNotEmpty(inParamList)) {
            // ???????????? ??????
            for (int i = 0; i < inParamList.size(); i++) {
                TInterfaceParam tip = new TInterfaceParam();
                tip.setId(batchUidService.getUid(qTInterfaceParam.getTableName()) + "");
                tip.setInterfaceId(interfaceId);
                TInterfaceParam obj = inParamList.get(i);
                tip.setParamName(obj.getParamName());
                tip.setParamType(obj.getParamType());
                tip.setParamInstruction(obj.getParamInstruction());
                tip.setParamInOut(Constant.ParmInOut.IN);
                tip.setEncryptionStatus(obj.getEncryptionStatus() == null ? 0 : obj.getEncryptionStatus());
                tip.setMaskStatus(obj.getMaskStatus() == null ? 0 : obj.getMaskStatus());
                tip.setCreatedTime(new Date());
                tip.setCreatedBy(loginUserName);
                interfaceParamService.post(tip);
            }
        }
        // ??????
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
            tip.setEncryptionStatus(obj.getEncryptionStatus() == null ? 0 : obj.getEncryptionStatus());
            tip.setMaskStatus(obj.getMaskStatus() == null ? 0 : obj.getMaskStatus());
            interfaceParamService.post(tip);
            // ???????????? 1???
            if ("1".equals(obj.getIsStart())) {
                // ??????????????????????????????
                ti.setParamOutStatus(obj.getParamName());
                ti.setParamOutStatusSuccess(obj.getParamOutStatusSuccess());
            }
        }
        // ??????????????????
        this.post(ti);
        cacheDelete(ti.getId());
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "????????????????????????!", null);
    }

    /**
     * ??????????????????
     */
    public ResultDto updateInterface(InterfaceDto dto, String loginUserName, boolean saveHis) {
        String id = dto.getId();
        //????????????????????????????????????
        cacheDelete(id);

        TInterface tf = this.getOne(id);
        if (tf == null) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "????????????id???????????????????????????,????????????????????????!",
                    "????????????id???????????????????????????,????????????????????????!");
        }
        //???????????????
        if (saveHis) {
            write2His(id, loginUserName);
        }
        // ????????????????????????
        String interfaceUrl = dto.getInterfaceUrl();
        if (!tf.getInterfaceUrl().equals(interfaceUrl)) {
            // ????????????????????????????????????
            tf = sqlQueryFactory.select(qTInterface).from(qTInterface)
                    .where(qTInterface.interfaceUrl.eq(interfaceUrl))
                    .fetchOne();
            if (tf != null) {
                return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "????????????????????????!", "????????????????????????!");
            }
        }
        // ??????
        List<TInterfaceParam> outParamList = dto.getOutParamList();
        if (CollectionUtils.isEmpty(outParamList)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "??????????????????!", "??????????????????!");
        }

        String interfaceName = dto.getInterfaceName();
        String interfaceTypeId = dto.getTypeId();
        // ???????????????????????????????????????json??????xml
        String inParamFormat = dto.getInParamFormat();
        String outParamFormat = dto.getOutParamFormat();
        String inParamFormatType = dto.getInParamFormatType();
        String outParamFormatType = dto.getOutParamFormatType();
        String allowLogDiscard = dto.getAllowLogDiscard();
        Integer interfaceType = dto.getInterfaceType();
        Integer asyncFlag = dto.getAsyncFlag();
        Integer encryptionType = dto.getEncryptionType();
        Integer maskPosStart = dto.getMaskPosStart();
        Integer maskPosEnd = dto.getMaskPosEnd();
        String inParamSchema = "";
        if (StringUtils.isNotBlank(inParamFormat)) {
            PlatformUtil.strIsJsonOrXml(inParamFormat);
            inParamSchema = niFiRequestUtil.generateSchemaToInterface(inParamFormat, inParamFormatType);
        }
        PlatformUtil.strIsJsonOrXml(outParamFormat);
        String outParamSchema = niFiRequestUtil.generateSchemaToInterface(outParamFormat, outParamFormatType);

        // ????????????????????????
        long execute = sqlQueryFactory.update(qTInterface).set(qTInterface.interfaceName, interfaceName)
                .set(qTInterface.typeId, interfaceTypeId).set(qTInterface.interfaceUrl, interfaceUrl)
                .set(qTInterface.inParamFormat, inParamFormat).set(qTInterface.outParamFormat, outParamFormat)
                .set(qTInterface.inParamFormatType, inParamFormatType)
                .set(qTInterface.outParamFormatType, outParamFormatType).set(qTInterface.updatedTime, new Date())
                .set(qTInterface.paramOutStatus, "").set(qTInterface.paramOutStatusSuccess, "")
                .set(qTInterface.inParamSchema, inParamSchema).set(qTInterface.outParamSchema, outParamSchema)
                .set(qTInterface.updatedBy, loginUserName).set(qTInterface.allowLogDiscard, allowLogDiscard)
                .set(qTInterface.interfaceType, interfaceType)
                .set(qTInterface.asyncFlag, asyncFlag)
                .set(qTInterface.encryptionType, encryptionType)
                .set(qTInterface.maskPosStart, maskPosStart)
                .set(qTInterface.maskPosEnd, maskPosEnd)
                .where(qTInterface.id.eq(id)).execute();
        if (execute < 1) {
            throw new RuntimeException("??????????????????????????????!");
        }
        // ??????????????????
        interfaceParamService.deleteProductInterfaceLinkById(id);
        // ??????
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
                tip.setEncryptionStatus(obj.getEncryptionStatus() == null ? 0 : obj.getEncryptionStatus());
                tip.setMaskStatus(obj.getMaskStatus() == null ? 0 : obj.getMaskStatus());
                interfaceParamService.post(tip);
            }
        }
        // ??????
        for (int i = 0; i < outParamList.size(); i++) {
            TInterfaceParam tip = new TInterfaceParam();
            tip.setId(batchUidService.getUid(qTInterfaceParam.getTableName()) + "");
            tip.setInterfaceId(id);
            TInterfaceParam obj = outParamList.get(i);
            tip.setParamName(obj.getParamName());
            tip.setParamType(obj.getParamType());
            tip.setEncryptionStatus(obj.getEncryptionStatus() == null ? 0 : obj.getEncryptionStatus());
            tip.setMaskStatus(obj.getMaskStatus() == null ? 0 : obj.getMaskStatus());
            tip.setParamInstruction(obj.getParamInstruction());
            tip.setParamInOut(Constant.ParmInOut.OUT);
            tip.setCreatedTime(new Date());
            tip.setCreatedBy(loginUserName);
            interfaceParamService.post(tip);
            // ????????????????????????????????? 1???
            if ("1".equals(obj.getIsStart())) {
                // ??????????????????????????????
                String paramName = obj.getParamName();
                String paramOutStatusSuccess = obj.getParamOutStatusSuccess();
                long l = sqlQueryFactory.update(qTInterface)
                        .set(qTInterface.paramOutStatus, StringUtils.isBlank(paramName) ? "" : paramName)
                        .set(qTInterface.paramOutStatusSuccess,
                                StringUtils.isBlank(paramOutStatusSuccess) ? "" : paramOutStatusSuccess)
                        .where(qTInterface.id.eq(id)).execute();
                if (l < 1) {
                    throw new RuntimeException("????????????????????????????????????!");
                }
            }
        }

        //????????????????????????????????????
        cacheDelete(id);

        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "????????????????????????!", null);
    }

    public void write2His(String id, String loginUserName) {
        TInterface ti = this.getOne(id);
        InterfaceDto interfaceInfo = handleInterface(ti);
        //????????????
        Map map = new HashMap();
        map.put("interfaceName", interfaceInfo.getInterfaceName());
        String hisShow = JSON.toJSONString(map);
        historyService.insertHis(interfaceInfo, 4, loginUserName, interfaceInfo.getId(), interfaceInfo.getId(), hisShow);
    }

    @ApiOperation(value = "??????????????????")
    @GetMapping("/getInterfaceType")
    public ResultDto<List<TType>> getInterfaceType() {
        List<TType> vendors = sqlQueryFactory
                .select(Projections.bean(TType.class, qTType.id, qTType.typeCode, qTType.typeName, qTType.updatedTime))
                .from(qTType).where(qTType.type.eq(1)).orderBy(qTType.createdTime.desc()).fetch();
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "??????????????????!", vendors);
    }

    @ApiOperation(value = "??????????????????")
    @GetMapping("/getInterfaceList")
    public ResultDto<TableData<TInterface>> getInterfaceList(
            @ApiParam(value = "????????????id") @RequestParam(value = "typeId", required = false) String typeId,
            @ApiParam(value = "??????id") @RequestParam(value = "interfaceId", required = false) String interfaceId,
            @ApiParam(value = "????????????") @RequestParam(value = "interfaceName", required = false) String interfaceName,
            @ApiParam(value = "??????", example = "1") @RequestParam(value = "pageNo", defaultValue = "1", required = false) Integer pageNo,
            @ApiParam(value = "????????????", example = "10") @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
        // ????????????
        ArrayList<Predicate> list = new ArrayList<>();
        if (StringUtils.isNotEmpty(typeId)) {
            list.add(qTInterface.typeId.eq(typeId));
        }
        if (StringUtils.isNotEmpty(interfaceId)) {
            list.add(qTInterface.id.eq(interfaceId));
        }
        if (StringUtils.isNotEmpty(interfaceName)) {
            list.add(qTInterface.interfaceName.like(PlatformUtil.createFuzzyText(interfaceName)));
        }
        QueryResults<TInterface> queryResults = sqlQueryFactory
                .select(Projections.bean(TInterface.class,
                        qTInterface.id, qTInterface.interfaceName, qTInterface.allowLogDiscard,
                        qTInterface.interfaceUrl, qTInterface.inParamFormat, qTInterface.outParamFormat,
                        qTInterface.createdTime, qTInterface.typeId, qTType.typeName.as("interfaceTypeName"),
                        qTInterface.interfaceType, qTInterface.asyncFlag,
                        sqlQueryFactory.select(qTInterfaceParam.id.count()).from(qTInterfaceParam)
                                .where((qTInterfaceParam.paramInOut.eq(Constant.ParmInOut.IN))
                                        .and(qTInterfaceParam.interfaceId.eq(qTInterface.id)))
                                .as("inParamCount"),
                        sqlQueryFactory.select(qTInterfaceParam.id.count()).from(qTInterfaceParam)
                                .where((qTInterfaceParam.paramInOut.eq(Constant.ParmInOut.OUT))
                                        .and(qTInterfaceParam.interfaceId.eq(qTInterface.id)))
                                .as("outParamCount")))
                .from(qTInterface).leftJoin(qTType).on(qTType.id.eq(qTInterface.typeId))
                .where(list.toArray(new Predicate[list.size()])).limit(pageSize)
                .offset((pageNo - 1) * pageSize).orderBy(qTInterface.createdTime.desc()).fetchResults();
        // ??????
        TableData<TInterface> tableData = new TableData<>(queryResults.getTotal(), queryResults.getResults());
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "??????????????????????????????!", tableData);
    }

    @ApiOperation(value = "????????????")
    @GetMapping("/getInterfaceSelect")
    public ResultDto<List<TInterface>> getInterfaceSelect(
            @ApiParam(value = "????????????id") @RequestParam(value = "typeId", required = false) String typeId,
            @ApiParam(value = "????????????") @RequestParam(value = "interfaceName", required = false) String interfaceName) {
        // ????????????
        ArrayList<Predicate> list = new ArrayList<>();
        if (StringUtils.isNotEmpty(typeId)) {
            list.add(qTInterface.typeId.eq(typeId));
        }
        if (StringUtils.isNotEmpty(interfaceName)) {
            list.add(qTInterface.interfaceName.like(PlatformUtil.createFuzzyText(interfaceName)));
        }
        List<TInterface> queryResults = sqlQueryFactory
                .select(Projections.bean(TInterface.class,
                        qTInterface.id, qTInterface.interfaceName, qTInterface.allowLogDiscard,
                        qTInterface.interfaceUrl, qTInterface.inParamFormat, qTInterface.outParamFormat,
                        qTInterface.createdTime, qTInterface.typeId,
                        qTInterface.interfaceType, qTInterface.asyncFlag))
                .from(qTInterface)
                .where(list.toArray(new Predicate[list.size()]))
                .orderBy(qTInterface.createdTime.desc()).fetch();

        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "??????????????????????????????!", queryResults);
    }


    @ApiOperation(value = "??????????????????", notes = "??????????????????")
    @GetMapping("/getInterfaceInfoById")
    public ResultDto<InterfaceDto> getInterfaceInfoById(
            @ApiParam(value = "??????id") @RequestParam(value = "id", required = true) String id) {
        TInterface ti = this.getOne(id);
        if (ti == null) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "??????id??????????????????!", null);
        }
        try {
            InterfaceDto iDto = handleInterface(ti);
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "??????????????????????????????!", iDto);
        } catch (Exception e) {
            logger.error("??????????????????????????????! MSG:{}", ExceptionUtil.dealException(e));
            e.printStackTrace();
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "??????????????????????????????!");
        }
    }

    public InterfaceDto handleInterface(TInterface ti) {
        InterfaceDto iDto = new InterfaceDto();
        BeanUtils.copyProperties(ti, iDto);
        // ??????????????????
        List<TInterfaceParam> paramsList = interfaceParamService.getParamsByInterfaceId(ti.getId());
        // ??????
        List<TInterfaceParam> inParamList = new ArrayList<>();
        // ??????
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
                tip.setEncryptionStatus(obj.getEncryptionStatus());
                tip.setMaskStatus(obj.getMaskStatus());
                if (iDto.getParamOutStatus().equals(obj.getParamName())) {
                    // ????????????
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
        return iDto;
    }

    @ApiOperation(value = "??????????????????(?????????????????????????????????)")
    @GetMapping("/getDisInterface")
    public ResultDto<List<TInterface>> getDisInterface(
            @ApiParam(value = "?????? 1?????????????????????????????? 2?????????????????????????????????") @RequestParam(defaultValue = "1", value = "status", required = false) String status) {
        List<TInterface> interfaces = null;
        // ??????????????????
        List<TInterface> allinterfaces = sqlQueryFactory.select(qTInterface).from(qTInterface)
                .orderBy(qTInterface.createdTime.desc()).fetch();
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "??????????????????!", allinterfaces);
    }

    @ApiOperation(value = "?????????????????????json?????????key-value", notes = "?????????????????????json?????????key-value")
    @PostMapping("/jsonFormat")
    public ResultDto<List<ParamsDto>> jsonFormat(String paramJson) {
        if (StringUtils.isBlank(paramJson)) {
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "????????????");
        }
        String type = PlatformUtil.strIsJsonOrXml(paramJson);
        if (Constant.ParamFormatType.XML.getType().equals(type)) {
            paramJson = niFiRequestUtil.xml2json(paramJson);
        }
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "", PlatformUtil.jsonFormat(paramJson));
    }

    @ApiOperation(value = "??????????????????????????????(????????????)")
    @GetMapping("/getInterBySys")
    public ResultDto<List<TInterface>> getInterBySys(
            @ApiParam(value = "????????????id") @RequestParam(value = "typeId", required = false) String typeId) {
        ArrayList<Predicate> list = new ArrayList<>();
        if (StringUtils.isNotEmpty(typeId)) {
            list.add(qTInterface.typeId.eq(typeId));
        }
        List<TInterface> interfaces = sqlQueryFactory.select(qTInterface).from(qTInterface)
                .where(list.toArray(new Predicate[list.size()])).fetch();
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "????????????id????????????????????????", interfaces);
    }

    /**
     * ?????????????????????????????????
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
     * ????????????????????????????????????
     *
     * @param interfaceUrl
     * @return
     */
    private TInterface getInterfaceByUrl(String interfaceUrl) {
        if (StringUtils.isBlank(interfaceUrl)) {
            return null;
        }
        return sqlQueryFactory.select(qTInterface).from(qTInterface).where(qTInterface.interfaceUrl.eq(interfaceUrl))
                .fetchFirst();
    }

    /**
     * ????????????id????????????????????????
     *
     * @param sysId
     * @return
     */
    public List<TInterface> getObjBySysId(String sysId) {
        List<TInterface> list = sqlQueryFactory.select(qTInterface).from(qTInterface)
                .fetch();
        return list;
    }

    @ApiOperation(value = "????????????id??????????????????", notes = "????????????id??????????????????")
    @GetMapping("/getInterfaceListById/{typeId}")
    public ResultDto getInterfaceListById(@ApiParam(value = "??????id") @PathVariable String typeId) {
        try {
            ArrayList<Predicate> list = new ArrayList<>();
            list.add(qTInterface.typeId.eq(typeId));
            list.add(qTType.type.eq(1));
            List<TInterface> queryResults = sqlQueryFactory.select(qTInterface).from(qTInterface).leftJoin(qTType)
                    .on(qTInterface.typeId.eq(qTType.id)).where(list.toArray(new Predicate[list.size()])).fetch();
            if (queryResults.size() > 0) {
                return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "???????????????????????????!");
            }
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "??????????????????????????????!");
        } catch (Exception e) {
            logger.error("????????????id????????????????????????! MSG:{}", ExceptionUtil.dealException(e));
            e.printStackTrace();
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "????????????id????????????????????????!");
        }
    }

    @ApiOperation(value = "???????????????????????????")
    @PostMapping("/testDbUrl")
    public ResultDto<String> testDbUrl(@RequestBody DbUrlTestDto dto) {
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "", niFiRequestUtil.testDbUrl(dto));
    }


    public TInterface getByTypeId(String typeId) {
        return sqlQueryFactory.select(qTInterface).from(qTInterface).where(qTInterface.typeId.eq(typeId)).fetchFirst();
    }

    public List<TInterface> getAll() {
        return sqlQueryFactory.select(qTInterface).from(qTInterface).fetch();
    }

    public List<TInterface> getByIdList(List<String> idList) {
        return sqlQueryFactory.select(qTInterface).from(qTInterface).where(qTInterface.id.in(idList)).fetch();
    }

    public void cacheDelete(String id) {
        TInterface tInterface = this.getOne(id);
        if (StringUtils.isEmpty((tInterface.getInterfaceUrl()))) {
            return;
        }
        CacheDeleteDto keyDto = new CacheDeleteDto();
        keyDto.setInterfaceCodes(Arrays.asList(tInterface.getInterfaceUrl()));
        //??????????????????????????????key
        keyDto.setCacheTypeList(Arrays.asList(
                Constant.CACHE_KEY_PREFIX.SCHEMA_TYPE,
                Constant.CACHE_KEY_PREFIX.COMMON_TYPE
        ));

        cacheDeleteService.cacheKeyDelete(keyDto);
    }

    public static void main(String[] args) {
        String sql = "REPLACE INTO  t_platform  ( ID ,  PROJECT_ID ,  PLATFORM_NAME ,  PLATFORM_CODE ,  PLATFORM_STATUS ,  PLATFORM_TYPE ,  ETL_SERVER_URL ,  ETL_USER ,  ETL_PWD ,  CREATED_BY ,  CREATED_TIME ,  UPDATED_BY ,  UPDATED_TIME ) VALUES ('61195408071721089', 'newProjectId_48109769075982460', '???????????????-?????????', 'ssmyyzhmz', '1', '1', 'null', 'admin', 'wPjVysmnNUWL9sKMJgyKzQ==', 'admin', now() , 'admin', now());";
        String replaced = sql.replaceAll("'newProjectId_\\d+'", "'11111111'");
        System.out.println(replaced);
    }
}
