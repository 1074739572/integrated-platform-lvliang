package com.iflytek.integrated.platform.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iflytek.integrated.common.Constant;
import com.iflytek.integrated.common.ResultDto;
import com.iflytek.integrated.common.TableData;
import com.iflytek.integrated.common.utils.ExceptionUtil;
import com.iflytek.integrated.platform.annotation.AvoidRepeatCommit;
import com.iflytek.integrated.platform.utils.Utils;
import com.iflytek.integrated.platform.entity.THospitalVendorLink;
import com.iflytek.integrated.platform.entity.TPlatform;
import com.iflytek.integrated.platform.entity.TVendorConfig;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.iflytek.integrated.platform.entity.QTHospitalVendorLink.qTHospitalVendorLink;
import static com.iflytek.integrated.platform.entity.QTPlatform.qTPlatform;
import static com.iflytek.integrated.platform.entity.QTVendorConfig.qTVendorConfig;

/**
* 平台管理
* @author weihe9
* @date 2020/12/12 16:58
*/
@Slf4j
@Api(tags = "平台管理")
@RestController
@RequestMapping("/{version}/pt/platformManage")
public class PlatformService extends QuerydslService<TPlatform, String, TPlatform, StringPath, PageRequest<TPlatform>> {

    @Autowired
    private VendorConfigService vendorConfigService;
    @Autowired
    private HospitalVendorLinkService hospitalVendorLinkService;
    @Autowired
    private BatchUidService batchUidService;
    @Autowired
    private Utils utils;

    private static final Logger logger = LoggerFactory.getLogger(PlatformService.class);

    public PlatformService(){
        super(qTPlatform, qTPlatform.id);
    }

    @ApiOperation(value = "根据项目id获取平台(分页)", notes = "根据项目id获取平台(分页)")
    @GetMapping("/getPlatformListById")
    public ResultDto getPlatformListById(
            @ApiParam(value = "项目id") @RequestParam(value = "projectId", required = true) String projectId,
            @ApiParam(value = "平台状态") @RequestParam(value = "platformStatus", required = false) String platformStatus,
            @ApiParam(value = "平台名称") @RequestParam(value = "platformName", required = false) String platformName,
            @ApiParam(value = "页码", example = "1") @RequestParam(value = "pageNo", defaultValue = "1", required = false) Integer pageNo,
            @ApiParam(value = "每页大小", example = "10") @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
        try {
            ArrayList<Predicate> list = new ArrayList<>();
            list.add(qTPlatform.projectId.eq(projectId));
            if (StringUtils.isNotBlank(platformStatus)) {
                list.add(qTPlatform.platformStatus.eq(platformStatus));
            }
            if (StringUtils.isNotBlank(platformName)) {
                list.add(qTPlatform.platformName.eq(platformName));
            }
            QueryResults<TPlatform> queryResults = sqlQueryFactory.select(
                    Projections.bean(
                        TPlatform.class,
                        qTPlatform.id,
                        qTPlatform.projectId,
                        qTPlatform.platformCode,
                        qTPlatform.platformName,
                        qTPlatform.platformStatus,
                        qTPlatform.platformType,
                        qTPlatform.createdTime,
                        qTPlatform.updatedTime,
                        qTPlatform.createdBy,
                        qTPlatform.updatedBy
                    )
            ).from(qTPlatform)
             .where(list.toArray(new Predicate[list.size()]))
             .limit(pageSize)
             .offset((pageNo - 1) * pageSize)
             .orderBy(qTPlatform.updatedTime.desc(),qTPlatform.createdTime.desc())
             .fetchResults();
            //分页
            TableData<TPlatform> tableData = new TableData<>(queryResults.getTotal(), queryResults.getResults());
            return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "根据项目id获取平台成功!", tableData);
        } catch (Exception e) {
            logger.error("根据项目id获取平台失败!", ExceptionUtil.dealException(e));
            e.printStackTrace();
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "根据项目id获取平台失败!", ExceptionUtil.dealException(e));
        }
    }


    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "新增or修改平台", notes = "新增or修改平台")
    @PostMapping("/saveAndUpdatePlatform")
    @AvoidRepeatCommit
    public ResultDto saveAndUpdatePlatform(@RequestBody JSONObject jsonObj /**, @RequestParam String loginUserName*/) {
        //校验是否获取到登录用户
        String loginUserName = "1";
        if(StringUtils.isBlank(loginUserName)){
            throw new RuntimeException("没有获取到登录用户");
        }
        if (StringUtils.isBlank(jsonObj.getString("id"))) {
            return savePlatform(jsonObj,loginUserName);
        }
        return updatePlatform(jsonObj,loginUserName);
    }

    /** 新增平台 */
    private ResultDto savePlatform(JSONObject jsonObj, String loginUserName) {
        TPlatform tp = new TPlatform();
        String platformId = batchUidService.getUid(qTPlatform.getTableName()) + "";
        tp.setId(platformId);
        tp.setPlatformCode(utils.generateCode(qTPlatform, qTPlatform.platformCode, jsonObj.getString("platformName")));
        tp.setProjectId(jsonObj.getString("projectId"));
        tp.setPlatformName(jsonObj.getString("platformName"));
        tp.setPlatformType(jsonObj.getString("platformType"));
        tp.setPlatformStatus(Constant.Status.START);
        tp.setCreatedTime(new Date());
        tp.setCreatedBy(loginUserName);
        this.post(tp);
        //关联厂商
        JSONArray jsonArr = jsonObj.getJSONArray("vendorInfo");
        if (jsonArr.isEmpty()) {
            return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "新增平台成功!", tp);
        }
        for (int i = 0; i < jsonArr.size(); i++) {
            JSONObject obj = jsonArr.getJSONObject(i);
            TVendorConfig tvc = new TVendorConfig();
            String vendorConfigId = batchUidService.getUid(qTVendorConfig.getTableName()) + "";
            tvc.setId(vendorConfigId);
            tvc.setPlatformId(platformId);
            tvc.setVendorId(obj.getString("vendorId"));
            tvc.setVersionId(obj.getString("versionId"));
            tvc.setConnectionType(obj.getString("connectionType"));
            tvc.setAddressUrl(obj.getString("addressUrl"));
            tvc.setEndpointUrl(obj.getString("endpointUrl"));
            tvc.setNamespaceUrl(obj.getString("namespaceUrl"));
            tvc.setDatabaseName(obj.getString("databaseName"));
            tvc.setDatabaseUrl(obj.getString("databaseUrl"));
            tvc.setDatabaseDriver(obj.getString("databaseDriver"));
            tvc.setJsonParams(obj.getString("jsonParams"));
            tvc.setUserName(obj.getString("userName"));
            tvc.setUserPassword(obj.getString("userPassword"));
            tvc.setCreatedTime(new Date());
            tvc.setCreatedBy(loginUserName);
            vendorConfigService.post(tvc);
            JSONArray hospitalArr = obj.getJSONArray("hospitalConfig");
            for (int j = 0; j < hospitalArr.size(); j++) {
                JSONObject hObj = hospitalArr.getJSONObject(j);
                THospitalVendorLink hvl = new THospitalVendorLink();
                hvl.setId(batchUidService.getUid(qTHospitalVendorLink.getTableName()) + "");
                hvl.setVendorConfigId(vendorConfigId);
                hvl.setHospitalId(hObj.getString("hospitalId"));
                hvl.setVendorHospitalId(hObj.getString("vendorHospitalId"));
                hvl.setCreatedTime(new Date());
                hvl.setCreatedBy(loginUserName);
                hospitalVendorLinkService.post(hvl);
            }
        }
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "新增平台成功!", tp);
    }

    /** 修改平台 */
    private ResultDto updatePlatform(JSONObject jsonObj, String loginUserName) {
        //删除平台下厂商配置信息  编辑平台 flag=1  编辑厂商信息 flag=2
        String flag = jsonObj.getString("flag");
//        if ("1".equals(flag)) {
//
//        }

        String platformId = jsonObj.getString("id");
        sqlQueryFactory.update(qTPlatform).set(qTPlatform.platformName, jsonObj.getString("platformName"))
                .set(qTPlatform.platformType, jsonObj.getString("platformType"))
                .set(qTPlatform.projectId, jsonObj.getString("projectId"))
                .set(qTPlatform.updatedTime, new Date())
                .set(qTPlatform.updatedBy, StringUtils.isBlank(loginUserName)?"":loginUserName)
                .where(qTPlatform.id.eq(platformId)).execute();
        //删除平台下厂商医院配置信息
        List<TVendorConfig> tvcList = vendorConfigService.getObjByPlatformId(platformId);
        if (!CollectionUtils.isEmpty(tvcList)) {
            for (int i = 0; i < tvcList.size(); i++) {
                hospitalVendorLinkService.deleteByVendorConfigId(tvcList.get(i).getId());
            }
        }

        if ("2".equals(flag)) {
            vendorConfigService.delVendorConfigAll(platformId);
        }

        JSONArray jsonArr = jsonObj.getJSONArray("vendorInfo");
        if (jsonArr.isEmpty()) {
            return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "修改平台成功!", platformId);
        }
        for (int i = 0; i < jsonArr.size(); i++) {
            JSONObject obj = jsonArr.getJSONObject(i);
            TVendorConfig tvc = new TVendorConfig();
            //判断厂商配置是新增or修改
            String vendorConfigId = obj.getString("id");
            if (StringUtils.isBlank(vendorConfigId)) {
                //新增厂商
                vendorConfigId = batchUidService.getUid(qTVendorConfig.getTableName()) + "";
                tvc.setId(vendorConfigId);
                tvc.setPlatformId(platformId);
                tvc.setVendorId(obj.getString("vendorId"));
                tvc.setVersionId(obj.getString("versionId"));
                tvc.setConnectionType(obj.getString("connectionType"));
                tvc.setAddressUrl(obj.getString("addressUrl"));
                tvc.setEndpointUrl(obj.getString("endpointUrl"));
                tvc.setNamespaceUrl(obj.getString("namespaceUrl"));
                tvc.setDatabaseName(obj.getString("databaseName"));
                tvc.setDatabaseUrl(obj.getString("databaseUrl"));
                tvc.setDatabaseDriver(obj.getString("databaseDriver"));
                tvc.setJsonParams(obj.getString("jsonParams"));
                tvc.setUserName(obj.getString("userName"));
                tvc.setUserPassword(obj.getString("userPassword"));
                tvc.setCreatedTime(new Date());
                tvc.setCreatedBy(loginUserName);
                vendorConfigService.post(tvc);
                JSONArray hospitalArr = obj.getJSONArray("hospitalConfig");
                for (int j = 0; j < hospitalArr.size(); j++) {
                    JSONObject hObj = hospitalArr.getJSONObject(j);
                    THospitalVendorLink hvl = new THospitalVendorLink();
                    hvl.setId(batchUidService.getUid(qTHospitalVendorLink.getTableName()) + "");
                    hvl.setVendorConfigId(vendorConfigId);
                    hvl.setHospitalId(hObj.getString("hospitalId"));
                    hvl.setVendorHospitalId(hObj.getString("vendorHospitalId"));
                    hvl.setCreatedTime(new Date());
                    hvl.setCreatedBy(loginUserName);
                    hospitalVendorLinkService.post(hvl);
                }
            }else {
                //编辑厂商
                tvc.setId(vendorConfigId);
                tvc.setPlatformId(platformId);
                tvc.setVendorId(obj.getString("vendorId"));
                tvc.setVersionId(obj.getString("versionId"));
                tvc.setConnectionType(obj.getString("connectionType"));
                tvc.setAddressUrl(obj.getString("addressUrl"));
                tvc.setEndpointUrl(obj.getString("endpointUrl"));
                tvc.setNamespaceUrl(obj.getString("namespaceUrl"));
                tvc.setDatabaseName(obj.getString("databaseName"));
                tvc.setDatabaseUrl(obj.getString("databaseUrl"));
                tvc.setDatabaseDriver(obj.getString("databaseDriver"));
                tvc.setJsonParams(obj.getString("jsonParams"));
                tvc.setUserName(obj.getString("userName"));
                tvc.setUserPassword(obj.getString("userPassword"));
                tvc.setUpdatedTime(new Date());
                tvc.setUpdatedBy(loginUserName);
                vendorConfigService.put(vendorConfigId, tvc);
//                vendorConfigService.post(tvc);
                JSONArray hospitalArr = obj.getJSONArray("hospitalConfig");
                for (int j = 0; j < hospitalArr.size(); j++) {
                    JSONObject hObj = hospitalArr.getJSONObject(j);
                    THospitalVendorLink hvl = new THospitalVendorLink();
                    hvl.setId(batchUidService.getUid(qTHospitalVendorLink.getTableName()) + "");
                    hvl.setVendorConfigId(vendorConfigId);
                    hvl.setHospitalId(hObj.getString("hospitalId"));
                    hvl.setVendorHospitalId(hObj.getString("vendorHospitalId"));
                    hvl.setCreatedTime(new Date());
                    hvl.setCreatedBy(loginUserName);
                    hospitalVendorLinkService.post(hvl);
                }
            }

        }
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "修改平台成功!", platformId);
    }
//    private ResultDto updatePlatform(JSONObject jsonObj, String loginUserName) {
//        String platformId = jsonObj.getString("id");
//        sqlQueryFactory.update(qTPlatform).set(qTPlatform.platformName, jsonObj.getString("platformName"))
//                .set(qTPlatform.platformType, jsonObj.getString("platformType"))
//                .set(qTPlatform.projectId, jsonObj.getString("projectId"))
//                .set(qTPlatform.updatedTime, new Date())
//                .set(qTPlatform.updatedBy, StringUtils.isBlank(loginUserName)?"":loginUserName)
//                .where(qTPlatform.id.eq(platformId)).execute();
//        //删除平台下厂商医院配置信息
//        List<TVendorConfig> tvcList = vendorConfigService.getObjByPlatformId(platformId);
//        if (!CollectionUtils.isEmpty(tvcList)) {
//            for (int i = 0; i < tvcList.size(); i++) {
//                hospitalVendorLinkService.deleteByVendorConfigId(tvcList.get(i).getId());
//            }
//        }
//        //删除平台下厂商配置信息  编辑平台 flag=1  编辑厂商信息 flag=2
//        String flag = jsonObj.getString("flag");
//        if ("2".equals(flag)) {
//            vendorConfigService.delVendorConfigAll(platformId);
//        }
//
//        JSONArray jsonArr = jsonObj.getJSONArray("vendorInfo");
//        if (jsonArr.isEmpty()) {
//            return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "修改平台成功!", platformId);
//        }
//        for (int i = 0; i < jsonArr.size(); i++) {
//            JSONObject obj = jsonArr.getJSONObject(i);
//            TVendorConfig tvc = new TVendorConfig();
//            String vendorConfigId = batchUidService.getUid(qTVendorConfig.getTableName()) + "";
//            tvc.setId(vendorConfigId);
//            tvc.setPlatformId(platformId);
//            tvc.setVendorId(obj.getString("vendorId"));
//            tvc.setVersionId(obj.getString("versionId"));
//            tvc.setConnectionType(obj.getString("connectionType"));
//            tvc.setAddressUrl(obj.getString("addressUrl"));
//            tvc.setEndpointUrl(obj.getString("endpointUrl"));
//            tvc.setNamespaceUrl(obj.getString("namespaceUrl"));
//            tvc.setDatabaseName(obj.getString("databaseName"));
//            tvc.setDatabaseUrl(obj.getString("databaseUrl"));
//            tvc.setDatabaseDriver(obj.getString("databaseDriver"));
//            tvc.setJsonParams(obj.getString("jsonParams"));
//            tvc.setUserName(obj.getString("userName"));
//            tvc.setUserPassword(obj.getString("userPassword"));
//            tvc.setCreatedTime(new Date());
//            tvc.setCreatedBy(loginUserName);
//            vendorConfigService.post(tvc);
//            JSONArray hospitalArr = obj.getJSONArray("hospitalConfig");
//            for (int j = 0; j < hospitalArr.size(); j++) {
//                JSONObject hObj = hospitalArr.getJSONObject(j);
//                THospitalVendorLink hvl = new THospitalVendorLink();
//                hvl.setId(batchUidService.getUid(qTHospitalVendorLink.getTableName()) + "");
//                hvl.setVendorConfigId(vendorConfigId);
//                hvl.setHospitalId(hObj.getString("hospitalId"));
//                hvl.setVendorHospitalId(hObj.getString("vendorHospitalId"));
//                hvl.setCreatedTime(new Date());
//                hvl.setCreatedBy(loginUserName);
//                hospitalVendorLinkService.post(hvl);
//            }
//        }
//        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "修改平台成功!", platformId);
//    }


    @ApiOperation(value = "更改启停用状态", notes = "更改启停用状态")
    @PostMapping("/updateStatus")
    @AvoidRepeatCommit
    public ResultDto updateStatus(
            @ApiParam(value = "平台id") @RequestParam(value = "id", required = true) String id,  String loginUserName,//@RequestParam
            @ApiParam(value = "平台状态 1启用 2停用") @RequestParam(value = "platformStatus", required = true) String platformStatus) {
        sqlQueryFactory.update(qTPlatform)
                .set(qTPlatform.platformStatus, platformStatus)
                .set(qTPlatform.updatedTime, new Date())
                .set(qTPlatform.updatedBy, StringUtils.isBlank(loginUserName)?"":loginUserName)
                .where(qTPlatform.id.eq(id)).execute();
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "平台状态更改成功!", id);
    }


    @ApiOperation(value = "删除平台", notes = "删除平台")
    @PostMapping("/deletePlatform")
    public ResultDto deletePlatform(@ApiParam(value = "平台id") @RequestParam(value = "id", required = true) String id) {
        //删除平台
        this.delete(id);
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "删除平台成功!", id);
    }


}
