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
 * 服务管理
 *
 * @author weihe9
 * @date 2020/12/12 17:16
 */
@Slf4j
@Api(tags = "系统服务管理")
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
    @ApiOperation(value = "服务删除", notes = "服务删除")
    @PostMapping("/delInterfaceById/{id}")
    public ResultDto<String> delInterfaceById(
            @ApiParam(value = "服务id") @PathVariable(value = "id", required = true) String id) {
        List<TBusinessInterface> list = businessInterfaceService.getListByInterfaceId(id);
        if (CollectionUtils.isNotEmpty(list)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "该标准服务已有集成配置关联,无法删除!", "该标准服务已有集成配置关联,无法删除!");
        }
        //删除缓存
        cacheDelete(id);
        // 删除服务
        long l = this.delete(id);
        if (l < 1) {
            throw new RuntimeException("标准服务删除成功!");
        }
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "标准服务删除成功!", null);
    }

    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "标准服务新增/编辑", notes = "标准服务新增/编辑")
    @PostMapping("/saveAndUpdateInterface")
    public ResultDto<String> saveAndUpdateInterface(@RequestBody InterfaceDto dto, @RequestParam("loginUserName") String loginUserName) {
        if (dto == null) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "数据传入错误!", "数据传入错误!");
        }
        // 校验是否获取到登录用户
        if (StringUtils.isBlank(loginUserName)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!", "没有获取到登录用户!");
        }
        String interfaceName = dto.getInterfaceName();
        if (StringUtils.isBlank(interfaceName)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "服务名为空!", "服务名为空!");
        }
        String interfaceUrl = dto.getInterfaceUrl();
        if (StringUtils.isBlank(interfaceUrl)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "服务方法为空!", "服务方法为空!");
        }
        String id = dto.getId();
        if (StringUtils.isBlank(id)) {
            return this.saveInterface(dto, loginUserName);
        }
        return this.updateInterface(dto, loginUserName, true);
    }

    /**
     * 新增标准服务
     */
    private ResultDto saveInterface(InterfaceDto dto, String loginUserName) {
        String interfaceUrl = dto.getInterfaceUrl();
        if (null != this.getInterfaceByUrl(interfaceUrl)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "该服务方法已存在!", "该服务方法已存在!");
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
        // 新增标准服务
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

        // 新增服务参数
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
                tip.setEncryptionStatus(obj.getEncryptionStatus() == null ? 0 : obj.getEncryptionStatus());
                tip.setMaskStatus(obj.getMaskStatus() == null ? 0 : obj.getMaskStatus());
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
            tip.setEncryptionStatus(obj.getEncryptionStatus() == null ? 0 : obj.getEncryptionStatus());
            tip.setMaskStatus(obj.getMaskStatus() == null ? 0 : obj.getMaskStatus());
            interfaceParamService.post(tip);
            // 是否开启 1开
            if ("1".equals(obj.getIsStart())) {
                // 存储参数到标准服务表
                ti.setParamOutStatus(obj.getParamName());
                ti.setParamOutStatusSuccess(obj.getParamOutStatusSuccess());
            }
        }
        // 新增标准服务
        this.post(ti);
        cacheDelete(ti.getId());
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "标准服务新增成功!", null);
    }

    /**
     * 修改标准服务
     */
    public ResultDto updateInterface(InterfaceDto dto, String loginUserName, boolean saveHis) {
        String id = dto.getId();
        //先删除更新之前的删除缓存
        cacheDelete(id);

        TInterface tf = this.getOne(id);
        if (tf == null) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "根据传入id未查出对应标准服务,检查是否传入错误!",
                    "根据传入id未查出对应标准服务,检查是否传入错误!");
        }
        //先写进历史
        if (saveHis) {
            write2His(id, loginUserName);
        }
        // 传入标准服务方法
        String interfaceUrl = dto.getInterfaceUrl();
        if (!tf.getInterfaceUrl().equals(interfaceUrl)) {
            // 查询新服务方法是否已存在
            tf = sqlQueryFactory.select(qTInterface).from(qTInterface)
                    .where(qTInterface.interfaceUrl.eq(interfaceUrl))
                    .fetchOne();
            if (tf != null) {
                return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "该服务方法已存在!", "该服务方法已存在!");
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

        // 修改标准服务信息
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
            throw new RuntimeException("修改标准服务信息失败!");
        }
        // 替换服务参数
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
                tip.setEncryptionStatus(obj.getEncryptionStatus() == null ? 0 : obj.getEncryptionStatus());
                tip.setMaskStatus(obj.getMaskStatus() == null ? 0 : obj.getMaskStatus());
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
            tip.setEncryptionStatus(obj.getEncryptionStatus() == null ? 0 : obj.getEncryptionStatus());
            tip.setMaskStatus(obj.getMaskStatus() == null ? 0 : obj.getMaskStatus());
            tip.setParamInstruction(obj.getParamInstruction());
            tip.setParamInOut(Constant.ParmInOut.OUT);
            tip.setCreatedTime(new Date());
            tip.setCreatedBy(loginUserName);
            interfaceParamService.post(tip);
            // 入参该字段表示是否开启 1开
            if ("1".equals(obj.getIsStart())) {
                // 标准服务信息出参赋值
                String paramName = obj.getParamName();
                String paramOutStatusSuccess = obj.getParamOutStatusSuccess();
                long l = sqlQueryFactory.update(qTInterface)
                        .set(qTInterface.paramOutStatus, StringUtils.isBlank(paramName) ? "" : paramName)
                        .set(qTInterface.paramOutStatusSuccess,
                                StringUtils.isBlank(paramOutStatusSuccess) ? "" : paramOutStatusSuccess)
                        .where(qTInterface.id.eq(id)).execute();
                if (l < 1) {
                    throw new RuntimeException("修改标准服务出参信息失败!");
                }
            }
        }

        //再删除更新之后的删除缓存
        cacheDelete(id);

        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "标准服务修改成功!", null);
    }

    public void write2His(String id, String loginUserName) {
        TInterface ti = this.getOne(id);
        InterfaceDto interfaceInfo = handleInterface(ti);
        //写入历史
        Map map = new HashMap();
        map.put("interfaceName", interfaceInfo.getInterfaceName());
        String hisShow = JSON.toJSONString(map);
        historyService.insertHis(interfaceInfo, 4, loginUserName, interfaceInfo.getId(), interfaceInfo.getId(), hisShow);
    }

    @ApiOperation(value = "获取服务分类")
    @GetMapping("/getInterfaceType")
    public ResultDto<List<TType>> getInterfaceType() {
        List<TType> vendors = sqlQueryFactory
                .select(Projections.bean(TType.class, qTType.id, qTType.typeCode, qTType.typeName, qTType.updatedTime))
                .from(qTType).where(qTType.type.eq(1)).orderBy(qTType.createdTime.desc()).fetch();
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "数据获取成功!", vendors);
    }

    @ApiOperation(value = "系统服务列表")
    @GetMapping("/getInterfaceList")
    public ResultDto<TableData<TInterface>> getInterfaceList(
            @ApiParam(value = "业务类型id") @RequestParam(value = "typeId", required = false) String typeId,
            @ApiParam(value = "服务id") @RequestParam(value = "interfaceId", required = false) String interfaceId,
            @ApiParam(value = "服务名称") @RequestParam(value = "interfaceName", required = false) String interfaceName,
            @ApiParam(value = "页码", example = "1") @RequestParam(value = "pageNo", defaultValue = "1", required = false) Integer pageNo,
            @ApiParam(value = "每页大小", example = "10") @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
        // 查询条件
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
        // 分页
        TableData<TInterface> tableData = new TableData<>(queryResults.getTotal(), queryResults.getResults());
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "标准服务列表获取成功!", tableData);
    }

    @ApiOperation(value = "服务下拉")
    @GetMapping("/getInterfaceSelect")
    public ResultDto<List<TInterface>> getInterfaceSelect(
            @ApiParam(value = "业务类型id") @RequestParam(value = "typeId", required = false) String typeId,
            @ApiParam(value = "服务名称") @RequestParam(value = "interfaceName", required = false) String interfaceName) {
        // 查询条件
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

        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "服务下拉列表获取成功!", queryResults);
    }


    @ApiOperation(value = "获取服务详情", notes = "获取服务详情")
    @GetMapping("/getInterfaceInfoById")
    public ResultDto<InterfaceDto> getInterfaceInfoById(
            @ApiParam(value = "服务id") @RequestParam(value = "id", required = true) String id) {
        TInterface ti = this.getOne(id);
        if (ti == null) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "根据id未查出该服务!", null);
        }
        try {
            InterfaceDto iDto = handleInterface(ti);
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取标准服务详情成功!", iDto);
        } catch (Exception e) {
            logger.error("获取标准服务详情失败! MSG:{}", ExceptionUtil.dealException(e));
            e.printStackTrace();
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "获取标准服务详情失败!");
        }
    }

    public InterfaceDto handleInterface(TInterface ti) {
        InterfaceDto iDto = new InterfaceDto();
        BeanUtils.copyProperties(ti, iDto);
        // 获取服务参数
        List<TInterfaceParam> paramsList = interfaceParamService.getParamsByInterfaceId(ti.getId());
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
                tip.setEncryptionStatus(obj.getEncryptionStatus());
                tip.setMaskStatus(obj.getMaskStatus());
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
        return iDto;
    }

    @ApiOperation(value = "选择服务下拉(可根据当前项目操作选择)")
    @GetMapping("/getDisInterface")
    public ResultDto<List<TInterface>> getDisInterface(
            @ApiParam(value = "操作 1获取当前项目下的服务 2获取非当前项目下的服务") @RequestParam(defaultValue = "1", value = "status", required = false) String status) {
        List<TInterface> interfaces = null;
        // 获取所有服务
        List<TInterface> allinterfaces = sqlQueryFactory.select(qTInterface).from(qTInterface)
                .orderBy(qTInterface.createdTime.desc()).fetch();
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "数据获取成功!", allinterfaces);
    }

    @ApiOperation(value = "根据参数模板（json）获取key-value", notes = "根据参数模板（json）获取key-value")
    @PostMapping("/jsonFormat")
    public ResultDto<List<ParamsDto>> jsonFormat(String paramJson) {
        if (StringUtils.isBlank(paramJson)) {
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "参数为空");
        }
        String type = PlatformUtil.strIsJsonOrXml(paramJson);
        if (Constant.ParamFormatType.XML.getType().equals(type)) {
            paramJson = niFiRequestUtil.xml2json(paramJson);
        }
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "", PlatformUtil.jsonFormat(paramJson));
    }

    @ApiOperation(value = "根据系统获取标准服务(新增服务)")
    @GetMapping("/getInterBySys")
    public ResultDto<List<TInterface>> getInterBySys(
            @ApiParam(value = "服务分类id") @RequestParam(value = "typeId", required = false) String typeId) {
        ArrayList<Predicate> list = new ArrayList<>();
        if (StringUtils.isNotEmpty(typeId)) {
            list.add(qTInterface.typeId.eq(typeId));
        }
        List<TInterface> interfaces = sqlQueryFactory.select(qTInterface).from(qTInterface)
                .where(list.toArray(new Predicate[list.size()])).fetch();
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "根据系统id获取标准服务成功", interfaces);
    }

    /**
     * 根据服务名获取标准服务
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
     * 根据服务方法获取标准服务
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
     * 根据系统id获取所有服务信息
     *
     * @param sysId
     * @return
     */
    public List<TInterface> getObjBySysId(String sysId) {
        List<TInterface> list = sqlQueryFactory.select(qTInterface).from(qTInterface)
                .fetch();
        return list;
    }

    @ApiOperation(value = "根据类型id获取关联服务", notes = "根据类型id获取关联服务")
    @GetMapping("/getInterfaceListById/{typeId}")
    public ResultDto getInterfaceListById(@ApiParam(value = "类型id") @PathVariable String typeId) {
        try {
            ArrayList<Predicate> list = new ArrayList<>();
            list.add(qTInterface.typeId.eq(typeId));
            list.add(qTType.type.eq(1));
            List<TInterface> queryResults = sqlQueryFactory.select(qTInterface).from(qTInterface).leftJoin(qTType)
                    .on(qTInterface.typeId.eq(qTType.id)).where(list.toArray(new Predicate[list.size()])).fetch();
            if (queryResults.size() > 0) {
                return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "该类型有关联的服务!");
            }
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "该类型没有关联的服务!");
        } catch (Exception e) {
            logger.error("根据类型id获取关联服务失败! MSG:{}", ExceptionUtil.dealException(e));
            e.printStackTrace();
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "根据类型id获取关联服务失败!");
        }
    }

    @ApiOperation(value = "测试数据库连接服务")
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
        //需要删除下面两种缓存key
        keyDto.setCacheTypeList(Arrays.asList(
                Constant.CACHE_KEY_PREFIX.SCHEMA_TYPE,
                Constant.CACHE_KEY_PREFIX.COMMON_TYPE
        ));

        cacheDeleteService.cacheKeyDelete(keyDto);
    }

    public static void main(String[] args) {
        String sql = "REPLACE INTO  t_platform  ( ID ,  PROJECT_ID ,  PLATFORM_NAME ,  PLATFORM_CODE ,  PLATFORM_STATUS ,  PLATFORM_TYPE ,  ETL_SERVER_URL ,  ETL_USER ,  ETL_PWD ,  CREATED_BY ,  CREATED_TIME ,  UPDATED_BY ,  UPDATED_TIME ) VALUES ('61195408071721089', 'newProjectId_48109769075982460', '孙思邈医院-智联网', 'ssmyyzhmz', '1', '1', 'null', 'admin', 'wPjVysmnNUWL9sKMJgyKzQ==', 'admin', now() , 'admin', now());";
        String replaced = sql.replaceAll("'newProjectId_\\d+'", "'11111111'");
        System.out.println(replaced);
    }
}
