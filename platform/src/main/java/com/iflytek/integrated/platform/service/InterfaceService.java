package com.iflytek.integrated.platform.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iflytek.integrated.common.Constant;
import com.iflytek.integrated.common.ResultDto;
import com.iflytek.integrated.common.utils.ExceptionUtil;
import com.iflytek.integrated.common.utils.StringUtil;
import com.iflytek.integrated.platform.entity.*;
import com.iflytek.medicalboot.core.dto.PageRequest;
import com.iflytek.medicalboot.core.id.BatchUidService;
import com.iflytek.medicalboot.core.querydsl.QuerydslService;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.StringPath;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

import static com.iflytek.integrated.platform.entity.QTBusinessInterface.qTBusinessInterface;
import static com.iflytek.integrated.platform.entity.QTInterface.qTInterface;
import static com.iflytek.integrated.platform.entity.QTInterfaceParam.qTInterfaceParam;
import static com.iflytek.integrated.platform.entity.QTInterfaceType.qTInterfaceType;
import static com.iflytek.integrated.platform.entity.QTProductInterfaceLink.qTProductInterfaceLink;

/**
* 接口管理
* @author weihe9
* @date 2020/12/12 17:16
*/
@Slf4j
@Api(tags = "接口管理")
@RestController
@RequestMapping("/{version}/pb/interfaceManage")
public class InterfaceService  extends QuerydslService<TInterface, String, TInterface, StringPath, PageRequest<TInterface>> {

    @Autowired
    private BusinessInterfaceService businessInterfaceService;
    @Autowired
    private ProductInterfaceLinkService productInterfaceLinkService;
    @Autowired
    private InterfaceParamService interfaceParamService;
    @Autowired
    private BatchUidService batchUidService;
    @Autowired
    private StringUtil stringUtil;

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
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "获取mock模板成功!", obj.getMockTemplate());
    }

    @ApiOperation(value = "保存mock模板", notes = "保存mock模板")
    @GetMapping("/saveMockTemplate")
    public ResultDto saveMockTemplate(@ApiParam(value = "接口配置id") @RequestParam(value = "id", required = true) String id,
                                     @ApiParam(value = "mock模板") @RequestParam(value = "mockTemplate", required = true) String mockTemplate) {
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



}
