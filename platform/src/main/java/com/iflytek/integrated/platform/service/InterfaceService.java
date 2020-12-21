package com.iflytek.integrated.platform.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iflytek.integrated.common.Constant;
import com.iflytek.integrated.common.ResultDto;
import com.iflytek.integrated.common.TableData;
import com.iflytek.integrated.common.utils.ExceptionUtil;
import com.iflytek.integrated.common.utils.RedisUtil;
import com.iflytek.integrated.platform.annotation.AvoidRepeatCommit;
import com.iflytek.integrated.platform.dto.InDebugResDto;
import com.iflytek.integrated.platform.utils.ToolsGenerate;
import com.iflytek.integrated.platform.utils.Utils;
import com.iflytek.integrated.platform.dto.InterfaceDto;
import com.iflytek.integrated.platform.entity.*;
import com.iflytek.medicalboot.core.dto.PageRequest;
import com.iflytek.medicalboot.core.id.BatchUidService;
import com.iflytek.medicalboot.core.querydsl.QuerydslService;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.StringPath;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

import static com.iflytek.integrated.platform.entity.QTBusinessInterface.qTBusinessInterface;
import static com.iflytek.integrated.platform.entity.QTHospital.qTHospital;
import static com.iflytek.integrated.platform.entity.QTHospitalVendorLink.qTHospitalVendorLink;
import static com.iflytek.integrated.platform.entity.QTInterface.qTInterface;
import static com.iflytek.integrated.platform.entity.QTInterfaceParam.qTInterfaceParam;
import static com.iflytek.integrated.platform.entity.QTInterfaceType.qTInterfaceType;
import static com.iflytek.integrated.platform.entity.QTPlatform.qTPlatform;
import static com.iflytek.integrated.platform.entity.QTProductFunctionLink.qTProductFunctionLink;
import static com.iflytek.integrated.platform.entity.QTProductInterfaceLink.qTProductInterfaceLink;
import static com.iflytek.integrated.platform.entity.QTProduct.qTProduct;
import static com.iflytek.integrated.platform.entity.QTFunction.qTFunction;
import static com.iflytek.integrated.platform.entity.QTProject.qTProject;
import static com.iflytek.integrated.platform.entity.QTProjectProductLink.qTProjectProductLink;
import static com.iflytek.integrated.platform.entity.QTVendorConfig.qTVendorConfig;

/**
* 接口管理
* @author weihe9
* @date 2020/12/12 17:16
*/
@Slf4j
@Api(tags = "接口管理")
@CrossOrigin
@RestController
@RequestMapping("/v1/pb/interfaceManage")
public class InterfaceService extends QuerydslService<TInterface, String, TInterface, StringPath, PageRequest<TInterface>> {

    @Autowired
    private BusinessInterfaceService businessInterfaceService;
    @Autowired
    private ProductInterfaceLinkService productInterfaceLinkService;
    @Autowired
    private ProductFunctionLinkService productFunctionLinkService;
    @Autowired
    private InterfaceParamService interfaceParamService;
    @Autowired
    private VendorConfigService vendorConfigService;
    @Autowired
    private BatchUidService batchUidService;
    @Autowired
    private ToolsGenerate toolsGenerate;
    @Resource
    private RedisUtil redisUtil;

    private static final Logger logger = LoggerFactory.getLogger(InterfaceService.class);

    public InterfaceService(){
        super(qTInterface, qTInterface.id);
    }

    @ApiOperation(value = "更改mock状态", notes = "更改mock状态")
    @PostMapping("/updateMockStatus")
    @AvoidRepeatCommit
    public ResultDto updateMockStatus(@ApiParam(value = "接口配置") @RequestParam(value = "id", required = true) String id,
                                      @ApiParam(value = "更改后的状态") @RequestParam(value = "mockStatus", required = true) String mockStatus) {
        businessInterfaceService.updateMockStatus(id, mockStatus);
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "更改mock状态成功!", id);
    }

    @ApiOperation(value = "更改接口配置状态", notes = "更改接口配置状态")
    @PostMapping("/updateStatus")
    @AvoidRepeatCommit
    public ResultDto updateStatus(@ApiParam(value = "接口配置") @RequestParam(value = "id", required = true) String id,
                                      @ApiParam(value = "更改后的状态") @RequestParam(value = "status", required = true) String status) {
        businessInterfaceService.updateStatus(id, status);
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "更改接口配置状态成功!", id);
    }

    @ApiOperation(value = "获取mock模板", notes = "获取mock模板")
    @GetMapping("/getMockTemplate")
    public ResultDto getMockTemplate(@ApiParam(value = "接口配置id") @RequestParam(value = "id", required = true) String id) {
        TBusinessInterface obj = businessInterfaceService.getOne(id);
        if(obj == null || StringUtils.isEmpty(obj.getId())){
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "没有找到接口", id);
        }
        String template = obj.getMockTemplate();
        if(StringUtils.isEmpty(template)){
            //如果没有出参模板，获取出参格式
            template = obj.getOutParamFormat();
        }
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "获取mock模板成功!", template);
    }

    @ApiOperation(value = "保存mock模板", notes = "保存mock模板")
    @PostMapping("/saveMockTemplate")
    public ResultDto saveMockTemplate(@ApiParam(value = "接口配置id") @RequestParam(value = "id", required = true) String id,
                                     @ApiParam(value = "mock模板") @RequestParam(value = "mockTemplate", required = true) String mockTemplate) {
        //校验mock模板格式是否正确
        Utils.strIsJsonOrXml(mockTemplate);
        businessInterfaceService.saveMockTemplate(id, mockTemplate);
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "保存mock模板成功!", id);
    }

    @ApiOperation(value = "获取接口调试显示数据", notes = "获取接口调试显示数据")
    @GetMapping("/getInterfaceDebug")
    public ResultDto getInterfaceDebug(@ApiParam(value = "接口配置id") @RequestParam(value = "id", required = true) String id) {
        try {
            TBusinessInterface businessInterface = sqlQueryFactory.select(
                Projections.bean(
                        TBusinessInterface.class,
                        qTBusinessInterface.id,
                        qTBusinessInterface.interfaceId,
                        qTBusinessInterface.vendorConfigId,
                        qTProject.projectCode.as("projectCode"),
                        qTInterface.interfaceUrl.as("interfaceUrl"),
                        qTProduct.productCode.as("productCode")
                    )
                ).from(qTBusinessInterface)
                    .leftJoin(qTInterface).on(qTBusinessInterface.interfaceId.eq(qTInterface.id))
                    .leftJoin(qTVendorConfig).on(qTVendorConfig.id.eq(qTBusinessInterface.vendorConfigId))
                    .leftJoin(qTPlatform).on(qTPlatform.id.eq(qTVendorConfig.platformId))
                    .leftJoin(qTProject).on(qTProject.id.eq(qTPlatform.projectId))
                    .leftJoin(qTProductFunctionLink).on(qTProductFunctionLink.id.eq(qTBusinessInterface.productFunctionLinkId))
                    .leftJoin(qTProduct).on(qTProduct.id.eq(qTProductFunctionLink.productId))
                    .where(qTBusinessInterface.id.eq(id)).fetchOne();
            if(businessInterface == null || StringUtils.isEmpty(businessInterface.getId())){
                return new ResultDto(Constant.ResultCode.ERROR_CODE, "", "没有查询到接口配置信息");
            }
            //获取入参列表
            String interfaceId = StringUtils.isNotEmpty(businessInterface.getInterfaceId())?businessInterface.getInterfaceId():"";
            List<String> paramNames = sqlQueryFactory.select(qTInterfaceParam.paramName).from(qTInterfaceParam)
                    .where(qTInterfaceParam.interfaceId.eq(interfaceId).and(qTInterfaceParam.paramInOut.eq("1"))).fetch();

            //获取医院名称列表
            String vendorConfigId = StringUtils.isNotEmpty(businessInterface.getVendorConfigId())?
                    businessInterface.getVendorConfigId():"";
            List<String> hospitals = sqlQueryFactory.select(qTHospital.hospitalCode).from(qTHospitalVendorLink)
                    .leftJoin(qTHospital).on(qTHospital.id.eq(qTHospitalVendorLink.hospitalId))
                    .where(qTHospitalVendorLink.vendorConfigId.eq(vendorConfigId).and(qTHospital.hospitalCode.isNotEmpty())).fetch();

            //拼接实体
            InDebugResDto resDto = new InDebugResDto();
            resDto.setFuncode(businessInterface.getInterfaceUrl());
            resDto.setProductcode(businessInterface.getProductCode());
            resDto.setProjectcode(businessInterface.getProjectCode());
            resDto.setInParams(paramNames);
            resDto.setOrgids(hospitals);
            return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "获取接口调试显示数据成功!", resDto);
        } catch (Exception e) {
            logger.error("获取接口调试显示数据失败!", ExceptionUtil.dealException(e));
            e.printStackTrace();
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "获取接口调试显示数据失败!", ExceptionUtil.dealException(e));
        }
    }

    @PostMapping("/interfaceDebug")
    @AvoidRepeatCommit
    @ApiOperation(value = "接口调试", notes = "接口调试")
    public ResultDto interfaceDebug(String format){
        String result = toolsGenerate.interfaceDebug(format);
        if(StringUtils.isBlank(result)){
            return new ResultDto(Constant.ResultCode.ERROR_CODE,"接口调试失败");
        }
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "", result);
    }

    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "标准接口删除", notes = "标准接口删除")
    @PostMapping("/delInterfaceById")
    public ResultDto delInterfaceById(@ApiParam(value = "标准接口id") @RequestParam(value = "id", required = true) String id) {
        try {
            //删除接口
            this.delete(id);
            //产品与标准接口关联
            productInterfaceLinkService.deleteProductInterfaceLinkById(id);
        } catch (Exception e) {
            logger.error("厂商删除失败!", ExceptionUtil.dealException(e));
            e.printStackTrace();
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "厂商删除失败!", ExceptionUtil.dealException(e));
        }
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "厂商删除成功!", null);
    }


    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "标准接口新增/编辑", notes = "标准接口新增/编辑")
    @PostMapping("/saveAndUpdateInterface")
    @AvoidRepeatCommit
    public ResultDto saveAndUpdateInterface(@RequestBody JSONObject jsonObj) {
        if (StringUtils.isBlank(jsonObj.getString("id"))) {
            return this.saveInterface(jsonObj);
        }
        return this.updateInterface(jsonObj);
    }

    /** 新增标准接口 */
    private ResultDto saveInterface(JSONObject jsonObj) {
        Utils.strIsJsonOrXml(jsonObj.getString("interfaceFormat"));
        String interfaceId = batchUidService.getUid(qTInterface.getTableName()) + "";
        //新增标准接口
        TInterface ti = new TInterface();
        ti.setId(interfaceId);
        ti.setInterfaceName(jsonObj.getString("interfaceName"));
        ti.setInterfaceTypeId(jsonObj.getString("interfaceTypeId"));
        ti.setInterfaceUrl(jsonObj.getString("interfaceUrl"));
        ti.setInterfaceFormat(jsonObj.getString("interfaceFormat"));
        ti.setCreatedTime(new Date());
        this.post(ti);
        //新增产品与接口关联
        JSONArray productIdArr = jsonObj.getJSONArray("productIds");
        for (int i = 0; i < productIdArr.size(); i++) {
            TProductInterfaceLink tpil = new TProductInterfaceLink();
            tpil.setId(batchUidService.getUid(qTProductInterfaceLink.getTableName()) + "");
            tpil.setProductId(productIdArr.getString(i));
            tpil.setInterfaceId(interfaceId);
            tpil.setCreatedTime(new Date());
            productInterfaceLinkService.post(tpil);
        }
        //新增接口参数
        JSONArray inParamList = jsonObj.getJSONArray("inParamList");
        for (int i = 0; i < inParamList.size(); i++) {
            TInterfaceParam tip = new TInterfaceParam();
            tip.setId(batchUidService.getUid(qTInterfaceParam.getTableName()) + "");
            tip.setInterfaceId(interfaceId);
            JSONObject obj = inParamList.getJSONObject(i);
            tip.setParamName(obj.getString("paramName"));
            tip.setParamType(obj.getString("paramType"));
            tip.setParamInstruction(obj.getString("paramInstruction"));
            tip.setParamLength(obj.getString("paramName").length());
            tip.setParamInOut("1");
            tip.setCreatedTime(new Date());
            interfaceParamService.post(tip);
        }
        JSONArray outParamList = jsonObj.getJSONArray("outParamList");
        for (int i = 0; i < outParamList.size(); i++) {
            TInterfaceParam tip = new TInterfaceParam();
            tip.setId(batchUidService.getUid(qTInterfaceParam.getTableName()) + "");
            tip.setInterfaceId(interfaceId);
            JSONObject obj = outParamList.getJSONObject(i);
            tip.setParamName(obj.getString("paramName"));
            tip.setParamType(obj.getString("paramType"));
            tip.setParamInstruction(obj.getString("paramInstruction"));
            tip.setParamLength(obj.getString("paramName").length());
            tip.setParamInOut("2");
            tip.setCreatedTime(new Date());
            interfaceParamService.post(tip);
        }
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "标准接口新增成功!", interfaceId);
    }

    /** 新增标准接口 */
    private ResultDto updateInterface(JSONObject jsonObj) {
        String id = jsonObj.getString("id");
        String interfaceName = jsonObj.getString("interfaceName");
        String interfaceTypeId = jsonObj.getString("interfaceTypeId");
        String interfaceUrl = jsonObj.getString("interfaceUrl");
        String interfaceFormat = jsonObj.getString("interfaceFormat");

        //修改标准接口信息
        sqlQueryFactory.update(qTInterface).set(qTInterface.interfaceName, interfaceName)
                .set(qTInterface.interfaceTypeId, interfaceTypeId).set(qTInterface.interfaceUrl, interfaceUrl)
                .set(qTInterface.interfaceFormat, interfaceFormat).set(qTInterface.updatedTime, new Date())
                .where(qTInterface.id.eq(id)).execute();
        //替换产品与接口关联
        productInterfaceLinkService.deleteProductInterfaceLinkById(id);
        JSONArray productIdArr = jsonObj.getJSONArray("productIds");
        for (int i = 0; i < productIdArr.size(); i++) {
            TProductInterfaceLink tpil = new TProductInterfaceLink();
            tpil.setId(batchUidService.getUid(qTProductInterfaceLink.getTableName()) + "");
            tpil.setProductId(productIdArr.getString(i));
            tpil.setInterfaceId(id);
            tpil.setCreatedTime(new Date());
            productInterfaceLinkService.post(tpil);
        }
        //替换接口参数
        interfaceParamService.deleteProductInterfaceLinkById(id);
        JSONArray inParamList = jsonObj.getJSONArray("inParamList");
        for (int i = 0; i < inParamList.size(); i++) {
            TInterfaceParam tip = new TInterfaceParam();
            tip.setId(batchUidService.getUid(qTInterfaceParam.getTableName()) + "");
            tip.setInterfaceId(id);
            JSONObject obj = inParamList.getJSONObject(i);
            tip.setParamName(obj.getString("paramName"));
            tip.setParamType(obj.getString("paramType"));
            tip.setParamInstruction(obj.getString("paramInstruction"));
            tip.setParamLength(obj.getString("paramName").length());
            tip.setParamInOut("1");
            tip.setCreatedTime(new Date());
            interfaceParamService.post(tip);
        }
        JSONArray outParamList = jsonObj.getJSONArray("outParamList");
        for (int i = 0; i < outParamList.size(); i++) {
            TInterfaceParam tip = new TInterfaceParam();
            tip.setId(batchUidService.getUid(qTInterfaceParam.getTableName()) + "");
            tip.setInterfaceId(id);
            JSONObject obj = outParamList.getJSONObject(i);
            tip.setParamName(obj.getString("paramName"));
            tip.setParamType(obj.getString("paramType"));
            tip.setParamInstruction(obj.getString("paramInstruction"));
            tip.setParamLength(obj.getString("paramName").length());
            tip.setParamInOut("2");
            tip.setCreatedTime(new Date());
            interfaceParamService.post(tip);
        }
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "标准接口修改成功!", id);
    }


    @ApiOperation(value = "获取接口分类")
    @GetMapping("/getInterfaceType")
    public ResultDto getInterfaceType() {
        List<TInterfaceType> vendors = sqlQueryFactory.select(
                Projections.bean(
                        TInterfaceType.class,
                        qTInterfaceType.id,
                        qTInterfaceType.interfaceTypeCode,
                        qTInterfaceType.interfaceTypeName
                )
        ).from(qTInterfaceType).orderBy(qTInterfaceType.updatedTime.desc()).fetch();
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE,"数据获取成功!", vendors);
    }


    @ApiOperation(value = "标准接口列表")
    @GetMapping("/getInterfaceList")
    public ResultDto getInterfaceList(
              @ApiParam(value = "接口分类id") @RequestParam(value = "interfaceTypeCode", required = false) String interfaceTypeCode,
              @ApiParam(value = "接口名称") @RequestParam(value = "name", required = false) String name,
              @ApiParam(value = "页码", example = "1") @RequestParam(value = "pageNo", defaultValue = "1", required = false) Integer pageNo,
              @ApiParam(value = "每页大小", example = "10") @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
        //查询条件
        ArrayList<Predicate> list = new ArrayList<>();
        if(StringUtils.isNotEmpty(interfaceTypeCode)){
            list.add(qTInterfaceType.interfaceTypeCode.eq(interfaceTypeCode));
        }
        if(StringUtils.isNotEmpty(name)){
            list.add(qTInterface.interfaceName.like(Utils.createFuzzyText(name)));
        }
        QueryResults<TInterface> queryResults = sqlQueryFactory.select(
            Projections.bean(
                    TInterface.class,
                    qTInterface.id,
                    qTInterface.interfaceName,
                    qTInterface.interfaceUrl,
                    qTInterface.interfaceFormat,
                    qTInterface.updatedTime,
                    qTInterfaceType.interfaceTypeName,
                    sqlQueryFactory.select(qTInterfaceParam.id.count()).from(qTInterfaceParam)
                            .where((qTInterfaceParam.paramInOut.eq(Constant.ParmInOut.IN))
                            .and(qTInterfaceParam.interfaceId.eq(qTInterface.id))).as("inParamCount"),
                    sqlQueryFactory.select(qTInterfaceParam.id.count()).from(qTInterfaceParam)
                            .where((qTInterfaceParam.paramInOut.eq(Constant.ParmInOut.OUT))
                            .and(qTInterfaceParam.interfaceId.eq(qTInterface.id))).as("outParamCount")
                )
            ).from(qTInterface)
                    .leftJoin(qTInterfaceType).on(qTInterfaceType.id.eq(qTInterface.interfaceTypeId))
                    .where(list.toArray(new Predicate[list.size()]))
                    .groupBy(qTInterface.id)
                    .limit(pageSize)
                    .offset((pageNo - 1) * pageSize)
                    .orderBy(qTInterface.updatedTime.desc())
                    .fetchResults();
        //分页
        TableData<TInterface> tableData = new TableData<>(queryResults.getTotal(), queryResults.getResults());
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE,"标准接口列表获取成功!", tableData);
    }


    @ApiOperation(value = "获取接口配置列表")
    @GetMapping("/getInterfaceConfigureList")
    public ResultDto getInterfaceConfigureList(
            @ApiParam(value = "平台id") @RequestParam(value = "platformId", required = true) String platformId,
            @ApiParam(value = "启停用状态") @RequestParam(value = "status", required = false) String status,
            @ApiParam(value = "mock状态") @RequestParam(value = "mockStatus", required = false) String mockStatus,
            @ApiParam(value = "页码", example = "1") @RequestParam(value = "pageNo", defaultValue = "1", required = false) Integer pageNo,
            @ApiParam(value = "每页大小", example = "10") @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
        //获取接口配置列表信息
        QueryResults<TBusinessInterface> queryResults = businessInterfaceService.getInterfaceConfigureList(platformId, status, mockStatus, pageNo, pageSize);
        //匹配列表展示信息
        List<TBusinessInterface> list = queryResults.getResults();
        for (TBusinessInterface tbi : list) {
            //获取产品+功能
            if (StringUtils.isNotBlank(tbi.getProductFunctionLinkId())) {
                TProductFunctionLink tpfl = sqlQueryFactory.select(Projections.bean(
                        qTProductFunctionLink, qTProduct.productName.as("productName"), qTFunction.functionName.as("functionName")))
                        .from(qTProductFunctionLink)
                        .leftJoin(qTProduct).on(qTProduct.id.eq(qTProductFunctionLink.productId))
                        .leftJoin(qTFunction).on(qTFunction.id.eq(qTProductFunctionLink.functionId))
                        .where(qTProductFunctionLink.id.eq(tbi.getProductFunctionLinkId())).fetchOne();
                if (tpfl != null) {
                    tbi.setProductName(tpfl.getProductName());
                    tbi.setFunctionName(tpfl.getFunctionName());
                }
            }
            //获取标准接口
            if (StringUtils.isNotBlank(tbi.getInterfaceId())) {
                TInterface ti = this.getOne(tbi.getInterfaceId());
                if (ti != null) {
                    tbi.setInterfaceName(ti.getInterfaceName());
                }
            }
        }
        //分页
        TableData<TBusinessInterface> tableData = new TableData<>(queryResults.getTotal(), queryResults.getResults());
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE,"获取接口配置列表获取成功", tableData);
    }


    @ApiOperation(value = "获取接口配置列表")
    @GetMapping("/getInterfaceConfigureList2")
    public ResultDto getInterfaceConfigureList2(
            @ApiParam(value = "平台id") @RequestParam(value = "platformId", required = true) String platformId,
            @ApiParam(value = "启停用状态") @RequestParam(value = "status", required = false) String status,
            @ApiParam(value = "mock状态") @RequestParam(value = "mockStatus", required = false) String mockStatus,
            @ApiParam(value = "页码", example = "1") @RequestParam(value = "pageNo", defaultValue = "1", required = false) Integer pageNo,
            @ApiParam(value = "每页大小", example = "10") @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize){
        //查询条件
        ArrayList<Predicate> list = new ArrayList<>();
        list.add(qTPlatform.id.eq(platformId));
        if (StringUtils.isNotBlank(status)) {
            list.add(qTBusinessInterface.status.eq(status));
        }
        if (StringUtils.isNotBlank(mockStatus)) {
            list.add(qTBusinessInterface.mockStatus.eq(mockStatus));
        }
        QueryResults<TBusinessInterface> queryResults = sqlQueryFactory.select(
                Projections.bean(
                        TBusinessInterface.class,
                        qTBusinessInterface.id,
                        qTBusinessInterface.businessInterfaceName,
                        qTBusinessInterface.status,
                        qTBusinessInterface.mockStatus,
                        qTProduct.productName,
                        qTFunction.functionName,
                        qTInterface.interfaceName,
                        qTVendorConfig.versionId
                )
            ).from(qTBusinessInterface)
                .leftJoin(qTProductFunctionLink).on(qTBusinessInterface.productFunctionLinkId.eq(qTProductFunctionLink.id))
                .leftJoin(qTProduct).on(qTProduct.id.eq(qTProductFunctionLink.productId))
                .leftJoin(qTFunction).on(qTFunction.id.eq(qTProductFunctionLink.functionId))
                .leftJoin(qTInterface).on(qTInterface.id.eq(qTBusinessInterface.interfaceId))
                .leftJoin(qTVendorConfig).on(qTVendorConfig.id.eq(qTBusinessInterface.vendorConfigId))
                .leftJoin(qTProjectProductLink).on(qTProjectProductLink.productFunctionLinkId.eq(qTProductFunctionLink.id))
                .leftJoin(qTPlatform).on(qTPlatform.projectId.eq(qTProjectProductLink.projectId))
                .where(list.toArray(new Predicate[list.size()]))
                .limit(pageSize)
                .offset((pageNo - 1) * pageSize)
                .orderBy(qTBusinessInterface.updatedTime.desc())
                .fetchResults();
        //分页
        TableData<TBusinessInterface> tableData = new TableData<>(queryResults.getTotal(), queryResults.getResults());
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE,"获取接口配置列表获取成功", tableData);
    }


    @ApiOperation(value = "获取接口配置详情")
    @GetMapping("/getInterfaceConfigInfoById")
    public ResultDto getInterfaceConfigInfoById(@ApiParam(value = "接口配置id") @RequestParam(value = "id", required = true) String id) {
        TBusinessInterface tBusinessInterface = sqlQueryFactory.select(
                Projections.bean(
                    TBusinessInterface.class,
                        qTProductFunctionLink.productId,
                        qTProductFunctionLink.functionId,
                        qTBusinessInterface.interfaceId,
                        qTVendorConfig.vendorId,
                        qTBusinessInterface.requestType,
                        qTBusinessInterface.businessInterfaceName,
                        qTBusinessInterface.frontInterface,
                        qTBusinessInterface.afterInterface,
                        qTBusinessInterface.pluginId,
                        qTBusinessInterface.requestConstant,
                        qTBusinessInterface.inParamFormat,
                        qTBusinessInterface.inParamSchema,
                        qTBusinessInterface.inParamTemplate,
                        qTBusinessInterface.inParamFormatType,
                        qTBusinessInterface.outParamFormat,
                        qTBusinessInterface.outParamSchema,
                        qTBusinessInterface.outParamTemplate,
                        qTBusinessInterface.outParamFormatType
                )
            ).from(qTBusinessInterface)
                .leftJoin(qTProductFunctionLink).on(qTBusinessInterface.productFunctionLinkId.eq(qTProductFunctionLink.id))
                .leftJoin(qTVendorConfig).on(qTVendorConfig.id.eq(qTBusinessInterface.vendorConfigId))
                .where(qTBusinessInterface.id.eq(id))
                .fetchOne();
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE,"获取接口配置详情成功", tBusinessInterface);
    }


    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "新增/更新接口配置", notes = "新增/更新接口配置")
    @PostMapping("/saveAndUpdateInterfaceConfig")
    @AvoidRepeatCommit
    public ResultDto saveAndUpdateInterfaceConfig(@RequestBody JSONObject jsonObj) {
        if (jsonObj == null) {
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "请求参数有误!", null);
        }
        TBusinessInterface obj = JSONObject.parseObject(jsonObj.toJSONString(), TBusinessInterface.class);

        String rtnMsg = (null==obj.getId()?"新增":"修改")+"接口配置";

        if (null == obj.getId()) {
            TVendorConfig vendorConfig = vendorConfigService.getObjByPlatformAndVendor(obj.getPlatformId(), obj.getVendorId());
            if (vendorConfig != null) {
                //厂商配置id
                obj.setVendorConfigId(vendorConfig.getId());
            }
        }else {
            TBusinessInterface tbi = businessInterfaceService.getOne(obj.getId());
            if (tbi != null) {
                //厂商配置id
                obj.setVendorConfigId(tbi.getVendorConfigId());
            }
        }

        //产品与功能关联
        TProductFunctionLink tpfl = productFunctionLinkService.getObjByProductAndFunction(obj.getProductId(), obj.getFunctionId());
        if (tpfl == null) {
            tpfl = new TProductFunctionLink();
            tpfl.setId(batchUidService.getUid(qTProductFunctionLink.getTableName())+"");
            tpfl.setProductId(obj.getProductId());
            tpfl.setFunctionId(obj.getFunctionId());
            tpfl.setCreatedTime(new Date());
            productFunctionLinkService.post(tpfl);
        }
        //存储产品功能关联id
        obj.setProductFunctionLinkId(tpfl.getId());

        //获取schema
        toolsGenerate.generateSchemaToInterface(obj);

        if (StringUtils.isBlank(obj.getId())) {
            //新增接口配置
            obj.setId(batchUidService.getUid(qTBusinessInterface.getTableName())+"");
            obj.setStatus(Constant.Status.START);
            obj.setCreatedTime(new Date());
            businessInterfaceService.post(obj);
        }else {
            obj.setUpdatedTime(new Date());
            businessInterfaceService.put(obj.getId(), obj);
        }
        setRedis(obj);
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, rtnMsg+"成功", obj);
    }


    @ApiOperation(value = "根据参数格式获取jolt", notes = "根据参数格式获取jolt")
    @PostMapping("/paramFormatJolt")
    public ResultDto paramFormatJolt(String paramFormat, String content){
        String contentType = Constant.ParamFormatType.getByType(content);
        if(StringUtils.isBlank(contentType) || Constant.ParamFormatType.NONE.getType().equals(contentType)){
            throw new RuntimeException("参数类型无效");
        }
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "成功", toolsGenerate.generateJolt(paramFormat, contentType));
    }


    @ApiOperation(value = "接口配置删除", notes = "接口配置删除")
    @PostMapping("/deleteInterfaceConfigure")
    public ResultDto deleteInterfaceConfigure(@ApiParam(value = "接口配置id") @RequestParam(value = "id", required = true) String id) {
        //删除接口配置
        businessInterfaceService.delete(id);
        delRedis(id);
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "接口配置删除成功!", null);
    }


    @ApiOperation(value = "获取标准接口详情", notes = "获取标准接口详情")
    @GetMapping("/getInterfaceInfoById")
    public ResultDto getInterfaceInfoById(@ApiParam(value = "标准接口id") @RequestParam(value = "id", required = true) String id) {
        TInterface ti = this.getOne(id);
        if (ti == null) {
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "根据id未查出该标准接口!", id);
        }
        try {
            InterfaceDto iDto = new InterfaceDto();
            BeanUtils.copyProperties(ti, iDto);
            //获取产品id
            List<TProductInterfaceLink> list = productInterfaceLinkService.getObjByInterface(id);
            List<String> productIds = new ArrayList<>();
            String productIdStr = "";
            for (int i = 0; i < list.size(); i++) {
                productIds.add(list.get(i).getProductId());
                productIdStr += list.get(i).getProductId();
                if (i < list.size() - 1) {
                    productIdStr += ",";
                }
            }
            iDto.setProductId(productIdStr);
            iDto.setProductIds(productIds);
            //获取接口参数
            List<TInterfaceParam> paramsList = interfaceParamService.getParamsByInterfaceId(id);
            //入参
            List<TInterfaceParam> inParamList = new ArrayList<>();
            //出参
            List<TInterfaceParam> outParamList = new ArrayList<>();
            TInterfaceParam tip;
            for (TInterfaceParam obj : paramsList) {
                tip = new TInterfaceParam();
                tip.setParamName(obj.getParamName());
                tip.setParamType(obj.getParamType());
                tip.setParamInstruction(obj.getParamInstruction());
                tip.setParamLength(obj.getParamLength());
                tip.setParamInOut(obj.getParamInOut());
                if ("1".equals(obj.getParamInOut())) {
                    inParamList.add(tip);
                }
                if ("2".equals(obj.getParamInOut())) {
                    outParamList.add(tip);
                }
            }
            iDto.setInParamList(inParamList);
            iDto.setOutParamList(outParamList);
            return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "获取标准接口详情成功!", iDto);
        } catch (Exception e) {
            logger.error("获取标准接口详情失败!", ExceptionUtil.dealException(e));
            e.printStackTrace();
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "获取标准接口详情失败!", ExceptionUtil.dealException(e));
        }
    }


    @ApiOperation(value = "选择接口下拉(可根据当前项目操作选择)")
    @GetMapping("/getDisInterface")
    public ResultDto getDisInterface(@ApiParam(value = "项目id") @RequestParam(value = "projectId", required = false) String projectId,
                    @ApiParam(value = "操作 1获取当前项目下的接口 2获取非当前项目下的接口") @RequestParam(defaultValue = "1", value = "status", required = false) String status) {
        List<TInterface> interfaces = null;
        if (StringUtils.isNotBlank(projectId) && "1".equals(status)) {
            //返回当前项目下的接口
            interfaces = sqlQueryFactory.select(qTInterface).from(qTInterface)
                    .leftJoin(qTBusinessInterface).on(qTBusinessInterface.interfaceId.eq(qTInterface.id))
                    .leftJoin(qTProjectProductLink).on(qTProjectProductLink.productFunctionLinkId.eq(qTBusinessInterface.productFunctionLinkId))
                    .where(qTProjectProductLink.projectId.eq(projectId))
                    .orderBy(qTInterface.updatedTime.desc()).fetch();
            return new ResultDto(Constant.ResultCode.SUCCESS_CODE,"数据获取成功!", interfaces);
        }
        //获取所有接口
        interfaces = sqlQueryFactory.select(qTInterface).from(qTInterface) .orderBy(qTInterface.updatedTime.desc()).fetch();
        if (StringUtils.isBlank(projectId)) {
            return new ResultDto(Constant.ResultCode.SUCCESS_CODE,"数据获取成功!", interfaces);
        }
        //返回当前项目下的接口
        List<TInterface> tiList = sqlQueryFactory.select(qTInterface).from(qTInterface)
                    .leftJoin(qTBusinessInterface).on(qTBusinessInterface.interfaceId.eq(qTInterface.id))
                    .leftJoin(qTProjectProductLink).on(qTProjectProductLink.productFunctionLinkId.eq(qTBusinessInterface.productFunctionLinkId))
                    .where(qTProjectProductLink.projectId.eq(projectId))
                    .orderBy(qTInterface.updatedTime.desc()).fetch();
        //去除当前项目下的接口
        interfaces.removeAll(tiList);
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE,"数据获取成功!", interfaces);
    }

    /**
     * 更新redis记录
     * @param tBusinessInterface
     */
    private void setRedis(TBusinessInterface tBusinessInterface){
        Boolean flag = redisUtil.hmSet(qTBusinessInterface.getTableName(),tBusinessInterface.getId(),tBusinessInterface);
        if(!flag){
            throw new RuntimeException("redis新增或更新驱动失败");
        }
    }

    /**
     * 删除redis记录
     * @param id
     */
    private void delRedis(String id){
        Boolean flag = redisUtil.hmDel(qTBusinessInterface.getTableName(),id);
        if(!flag){
            throw new RuntimeException("redis删除驱动失败");
        }
    }
}
