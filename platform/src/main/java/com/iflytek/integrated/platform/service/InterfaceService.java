package com.iflytek.integrated.platform.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iflytek.integrated.common.Constant;
import com.iflytek.integrated.common.ResultDto;
import com.iflytek.integrated.common.TableData;
import com.iflytek.integrated.common.utils.ExceptionUtil;
import com.iflytek.integrated.common.utils.Utils;
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
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import java.util.*;

import static com.iflytek.integrated.platform.entity.QTBusinessInterface.qTBusinessInterface;
import static com.iflytek.integrated.platform.entity.QTInterface.qTInterface;
import static com.iflytek.integrated.platform.entity.QTInterfaceParam.qTInterfaceParam;
import static com.iflytek.integrated.platform.entity.QTInterfaceType.qTInterfaceType;
import static com.iflytek.integrated.platform.entity.QTPlatform.qTPlatform;
import static com.iflytek.integrated.platform.entity.QTProductFunctionLink.qTProductFunctionLink;
import static com.iflytek.integrated.platform.entity.QTProductInterfaceLink.qTProductInterfaceLink;
import static com.iflytek.integrated.platform.entity.QTProduct.qTProduct;
import static com.iflytek.integrated.platform.entity.QTFunction.qTFunction;
import static com.iflytek.integrated.platform.entity.QTProjectProductLink.qTProjectProductLink;
import static com.iflytek.integrated.platform.entity.QTVendorConfig.qTVendorConfig;

/**
* 接口管理
* @author weihe9
* @date 2020/12/12 17:16
*/
@Slf4j
@Api(tags = "接口管理")
@RestController
@RequestMapping("/{version}/pb/interfaceManage")
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

    private static final Logger logger = LoggerFactory.getLogger(InterfaceService.class);

    public InterfaceService(){
        super(qTInterface, qTInterface.id);
    }

    @ApiOperation(value = "更改mock状态", notes = "更改mock状态")
    @GetMapping("/updateMockStatus")
    public ResultDto updateMockStatus(@ApiParam(value = "接口配置") @RequestParam(value = "id", required = true) String id,
                                      @ApiParam(value = "更改后的状态") @RequestParam(value = "mockStatus", required = true) String mockStatus) {
        businessInterfaceService.updateMockStatus(id, mockStatus);
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "更改mock状态成功!", id);
    }

    @ApiOperation(value = "获取mock模板", notes = "获取mock模板")
    @GetMapping("/getMockTemplate")
    public ResultDto getMockTemplate(@ApiParam(value = "接口配置id") @RequestParam(value = "id", required = true) String id) {
        TBusinessInterface obj = businessInterfaceService.getOne(id);
        if(obj == null || StringUtils.isEmpty(obj.getId())){
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "", "没有找到接口");
        }
        String template = obj.getMockTemplate();
        if(StringUtils.isEmpty(template)){
            //如果没有出参模板，获取出参格式
            template = obj.getOutParamFormat();
        }
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "获取mock模板成功!", template);
    }

    @ApiOperation(value = "保存mock模板", notes = "保存mock模板")
    @GetMapping("/saveMockTemplate")
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
            List<TBusinessInterface> list = sqlQueryFactory.select(Projections.bean(TBusinessInterface.class,
                    qTBusinessInterface.id,
                    qTBusinessInterface.interfaceId,
                    qTBusinessInterface.businessInterfaceName,
                    qTInterface.interfaceName.as("interfaceName")))
                    .from(qTBusinessInterface)
                    .where(qTBusinessInterface.id.eq(id))
                    .leftJoin(qTInterface).on(qTBusinessInterface.interfaceId.eq(qTInterface.id)).limit(1).fetch();
            JSONObject jsonObj = new JSONObject();
            if (!CollectionUtils.isEmpty(list)) {
                TBusinessInterface obj = list.get(0);
                jsonObj.put("id", obj.getId());
                jsonObj.put("businessInterfaceName", obj.getBusinessInterfaceName());
                jsonObj.put("interfaceName", obj.getInterfaceName());
                String interfaceId = obj.getInterfaceId();
                jsonObj.put("interfaceId", interfaceId);
                //获取入参
                List<TInterfaceParam> fetch = sqlQueryFactory.select(qTInterfaceParam).from(qTInterfaceParam)
                        .where(qTInterfaceParam.interfaceId.eq(interfaceId).and(qTInterfaceParam.paramInOut.eq("1"))).fetch();
                JSONArray arr = new JSONArray();
                for (TInterfaceParam tip : fetch) {
                    arr.add(tip.getParamName());
                }
                jsonObj.put("inParam", arr);
            }
            return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "获取接口调试显示数据成功!", id);
        } catch (Exception e) {
            logger.error("获取接口调试显示数据失败!", ExceptionUtil.dealException(e));
            e.printStackTrace();
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "获取接口调试显示数据失败!", ExceptionUtil.dealException(e));
        }
    }


    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "标准接口删除", notes = "标准接口删除")
    @GetMapping("/delInterfaceById")
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
    @GetMapping("/saveAndUpdateInterface")
    public ResultDto saveAndUpdateInterface(@ApiParam(value = "标准接口id") @RequestParam(value = "id", required = false) String id,
                                            @ApiParam(value = "产品id 多个以,分隔") @RequestParam(value = "id", required = true) String productIds,
                                            @ApiParam(value = "接口分类id") @RequestParam(value = "id", required = true) String interfaceTypeId,
                                            @ApiParam(value = "接口名") @RequestParam(value = "id", required = true) String interfaceName,
                                            @ApiParam(value = "接口url") @RequestParam(value = "id", required = true) String interfaceURL,
                                            @ApiParam(value = "出参格式") @RequestParam(value = "id", required = true) String interfaceFormat,
                                            @ApiParam(value = "出入参数组") @RequestParam(value = "id", required = false) String paramInOut) {
        if (StringUtils.isBlank(id)) {
            return this.saveInterface(productIds, interfaceTypeId, interfaceName, interfaceURL, interfaceFormat, paramInOut);
        }
        return this.updateInterface(id, productIds, interfaceTypeId, interfaceName, interfaceURL, interfaceFormat, paramInOut);
    }

    /** 新增标准接口 */
    private ResultDto saveInterface(String productIds, String interfaceTypeId, String interfaceName, String interfaceURL, String interfaceFormat, String paramInOut) {
        try {
            Utils.strIsJsonOrXml(interfaceFormat);
            String interfaceId = batchUidService.getUid(qTInterface.getTableName()) + "";
            //新增标准接口
            TInterface ti = new TInterface();
            ti.setId(interfaceId);
            ti.setInterfaceName(interfaceName);
            ti.setInterfaceTypeId(interfaceTypeId);
            ti.setInterfaceUrl(interfaceURL);
            ti.setInterfaceFormat(interfaceFormat);
            ti.setCreatedTime(new Date());
            this.post(ti);
            //新增产品与接口关联
            String[] productIdArr = productIds.split(",");
            for (int i = 0; i < productIdArr.length; i++) {
                TProductInterfaceLink tpil = new TProductInterfaceLink();
                tpil.setId(batchUidService.getUid(qTProductInterfaceLink.getTableName()) + "");
                tpil.setProductId(productIdArr[i]);
                tpil.setInterfaceId(interfaceId);
                tpil.setCreatedTime(new Date());
                productInterfaceLinkService.post(tpil);
            }
            //新增接口参数
            if (StringUtils.isNotBlank(paramInOut)) {
                JSONArray jsonArr = JSON.parseArray(paramInOut);
                for (int j = 0; j < jsonArr.size(); j++) {
                    JSONObject jsonObj = jsonArr.getJSONObject(j);
                    TInterfaceParam tip = new TInterfaceParam();
                    tip.setId(batchUidService.getUid(qTInterfaceParam.getTableName()) + "");
                    tip.setParamName(jsonObj.getString("name"));
                    tip.setParamInstruction(jsonObj.getString("instruction"));
                    tip.setInterfaceId(interfaceId);
                    tip.setParamType(jsonObj.getString("type"));
                    tip.setParamLength(jsonObj.getString("name").length());
                    tip.setParamInOut(jsonObj.getString("inOut"));
                    tip.setCreatedTime(new Date());
                    interfaceParamService.post(tip);
                }
            }
            return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "标准接口新增成功!", interfaceId);
        } catch (Exception e) {
            logger.error("标准接口新增失败!", ExceptionUtil.dealException(e));
            e.printStackTrace();
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "标准接口新增失败!", ExceptionUtil.dealException(e));
        }
    }

    /** 新增标准接口 */
    private ResultDto updateInterface(String id, String productIds, String interfaceTypeId, String interfaceName, String interfaceURL, String interfaceFormat, String paramInOut) {
        try {
            //修改标准接口信息
            sqlQueryFactory.update(qTInterface).set(qTInterface.interfaceName, interfaceName)
                    .set(qTInterface.interfaceTypeId, interfaceTypeId).set(qTInterface.interfaceUrl, interfaceURL)
                    .set(qTInterface.interfaceFormat, interfaceFormat).set(qTInterface.updatedTime, new Date())
                    .where(qTInterface.id.eq(id)).execute();
            //替换产品与接口关联
            productInterfaceLinkService.deleteProductInterfaceLinkById(id);
            String[] productIdArr = productIds.split(",");
            for (int i = 0; i < productIdArr.length; i++) {
                TProductInterfaceLink tpil = new TProductInterfaceLink();
                tpil.setId(batchUidService.getUid(qTProductInterfaceLink.getTableName()) + "");
                tpil.setProductId(productIdArr[i]);
                tpil.setInterfaceId(id);
                tpil.setCreatedTime(new Date());
                productInterfaceLinkService.post(tpil);
            }
            //替换接口参数
            interfaceParamService.deleteProductInterfaceLinkById(id);
            if (StringUtils.isNotBlank(paramInOut)) {
                JSONArray jsonArr = JSON.parseArray(paramInOut);
                for (int j = 0; j < jsonArr.size(); j++) {
                    JSONObject jsonObj = jsonArr.getJSONObject(j);
                    TInterfaceParam tip = new TInterfaceParam();
                    tip.setId(batchUidService.getUid(qTInterfaceParam.getTableName()) + "");
                    tip.setParamName(jsonObj.getString("name"));
                    tip.setParamInstruction(jsonObj.getString("instruction"));
                    tip.setInterfaceId(id);
                    tip.setParamType(jsonObj.getString("type"));
                    tip.setParamLength(jsonObj.getString("name").length());
                    tip.setParamInOut(jsonObj.getString("inOut"));
                    tip.setCreatedTime(new Date());
                    interfaceParamService.post(tip);
                }
            }
            return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "标准接口修改成功!", id);
        } catch (Exception e) {
            logger.error("标准接口修改失败!", ExceptionUtil.dealException(e));
            e.printStackTrace();
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "标准接口修改失败!", ExceptionUtil.dealException(e));
        }
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
                    qTInterfaceType.interfaceTypeName,
                    sqlQueryFactory.select(qTInterfaceParam.id.count()).from(qTInterfaceParam)
                            .where((qTInterfaceParam.paramInOut.eq(Constant.ParmInOut.IN)).
                            and(qTInterfaceParam.interfaceId.eq(qTInterface.id))).as("inParamCount"),
                    sqlQueryFactory.select(qTInterfaceParam.id.count()).from(qTInterfaceParam)
                            .where((qTInterfaceParam.paramInOut.eq(Constant.ParmInOut.OUT)).
                            and(qTInterfaceParam.interfaceId.eq(qTInterface.id))).as("outParamCount")
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
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE,"", tableData);
    }


    @ApiOperation(value = "获取接口配置详情")
    @GetMapping("/getInterfaceConfigInfoById")
    public ResultDto getInterfaceConfigInfoById(String id){
        if(StringUtils.isEmpty(id)){
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "", "id不能为空");
        }
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
                        qTBusinessInterface.outParamTemplate
                )
            ).from(qTBusinessInterface)
                .leftJoin(qTProductFunctionLink).on(qTBusinessInterface.productFunctionLinkId.eq(qTProductFunctionLink.id))
                .leftJoin(qTVendorConfig).on(qTVendorConfig.id.eq(qTBusinessInterface.vendorConfigId))
                .where(qTBusinessInterface.id.eq(id))
                .fetchOne();
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE,"",tBusinessInterface);
    }

    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "新增/更新接口配置", notes = "新增/更新接口配置")
    @GetMapping("/saveAndUpdateInterfaceConfig")
    public ResultDto saveAndUpdateInterfaceConfig(@RequestBody TBusinessInterface obj) {
        if (obj == null) {
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "请求参数有误!", null);
        }
        String rtnMsg = (null==obj.getId()?"新增":"修改")+"接口配置";
        try {
            //厂商配置
            TVendorConfig vendorConfig = vendorConfigService.getObjByPlatformAndVendor(obj.getPlatformId(), obj.getVendorId());
            obj.setVendorConfigId(vendorConfig.getId());//厂商配置id
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
            obj.setProductFunctionLinkId(tpfl.getId());//存储产品功能关联id

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
            return new ResultDto(Constant.ResultCode.SUCCESS_CODE, rtnMsg+"成功", obj);
        } catch (Exception e) {
            logger.error(rtnMsg, ExceptionUtil.dealException(e));
            e.printStackTrace();
            return new ResultDto(Constant.ResultCode.ERROR_CODE, rtnMsg+"失败", ExceptionUtil.dealException(e));
        }
    }


    @ApiOperation(value = "获取标准接口详情", notes = "获取标准接口详情")
    @GetMapping("/getInterfaceInfoById")
    public ResultDto getInterfaceInfoById(@ApiParam(value = "标准接口id") @RequestParam(value = "id", required = true) String id) {
        try {
            TInterface ti = this.getOne(id);
            InterfaceDto iDto = new InterfaceDto();
            BeanUtils.copyProperties(ti, iDto);
            //获取产品id
            TBusinessInterface tbi = businessInterfaceService.getProductIdByInterfaceId(id);
            iDto.setProductId(tbi.getProductId());
            iDto.setOutParamFormat(tbi.getOutParamFormat());
            //获取接口参数
            List<TInterfaceParam> paramsList = interfaceParamService.getParamsByInterfaceId(id);
            List<Map<String, String>> inParamList = new ArrayList<>();//入参
            List<Map<String, String>> outParamList = new ArrayList<>();//出参
            Map<String, String> map;
            for (TInterfaceParam obj : paramsList) {
                map = new HashMap<>();
                map.put("paramName", obj.getParamName());
                map.put("paramInstruction", obj.getParamInstruction());
                map.put("paramType", obj.getParamType());
                map.put("paramLength", obj.getParamLength()+"");
                map.put("paramInOut", obj.getParamInOut());
                if ("1".equals(obj.getParamInOut())) {
                    inParamList.add(map);
                }
                if ("2".equals(obj.getParamInOut())) {
                    outParamList.add(map);
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


}
