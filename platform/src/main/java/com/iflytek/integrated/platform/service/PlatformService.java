package com.iflytek.integrated.platform.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iflytek.integrated.common.Constant;
import com.iflytek.integrated.common.ResultDto;
import com.iflytek.integrated.common.TableData;
import com.iflytek.integrated.common.utils.ExceptionUtil;
import com.iflytek.integrated.common.utils.StringUtil;
import com.iflytek.integrated.platform.entity.THospitalVendorLink;
import com.iflytek.integrated.platform.entity.TPlatform;
import com.iflytek.integrated.platform.entity.TVendorConfig;
import com.iflytek.medicalboot.core.dto.PageRequest;
import com.iflytek.medicalboot.core.id.BatchUidService;
import com.iflytek.medicalboot.core.querydsl.QuerydslService;
import com.querydsl.core.QueryResults;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

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
@RequestMapping("/{version}/pb/platformManage")
public class PlatformService extends QuerydslService<TPlatform, String, TPlatform, StringPath, PageRequest<TPlatform>> {

    @Autowired
    private VendorConfigService vendorConfigService;
    @Autowired
    private HospitalVendorLinkService hospitalVendorLinkService;
    @Autowired
    private BatchUidService batchUidService;
    @Autowired
    private StringUtil stringUtil;

    private static final Logger logger = LoggerFactory.getLogger(PlatformService.class);

    public PlatformService(){
        super(qTPlatform, qTPlatform.id);
    }

    @ApiOperation(value = "根据项目id获取平台(分页)", notes = "根据项目id获取平台(分页)")
    @GetMapping("/getPlatformListById")
    public ResultDto getPlatformListById(
            @ApiParam(value = "项目id") @RequestParam(value = "projectId", required = true) String projectId,
            @ApiParam(value = "页码", example = "1") @RequestParam(value = "pageNo", defaultValue = "1", required = false) Integer pageNo,
            @ApiParam(value = "每页大小", example = "10") @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
        try {
            QueryResults<TPlatform> queryResults = sqlQueryFactory.select(
                    Projections.bean(
                        TPlatform.class,
                        qTPlatform.id,
                        qTPlatform.platformCode,
                        qTPlatform.platformName,
                        qTPlatform.platformStatus,
                        qTPlatform.platformType
                    )
            ).from(qTPlatform)
             .where(qTPlatform.projectId.eq(projectId))
             .limit(pageSize)
             .offset((pageNo - 1) * pageSize)
             .orderBy(qTPlatform.updatedTime.desc())
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
    @GetMapping("/saveAndUpdatePlatform")
    public ResultDto saveAndUpdatePlatform(
            @ApiParam(value = "平台id") @RequestParam(value = "id", required = false) String id,
            @ApiParam(value = "平台名") @RequestParam(value = "vendorName", required = true) String platformName,
            @ApiParam(value = "平台类型") @RequestParam(value = "driveIds", required = true) String platformType,
            @ApiParam(value = "厂商信息") @RequestParam(value = "vendorInfo", required = false) String vendorInfo) {
        if (StringUtils.isBlank(id)) {
            return savePlatform(platformName, platformType, vendorInfo);
        }
        return updatePlatform(id, platformName, platformType, vendorInfo);
    }


    /** 新增平台 */
    private ResultDto savePlatform(String platformName, String platformType, String vendorInfo) {
        try {
            TPlatform tp = new TPlatform();
            String platformId = batchUidService.getUid(qTPlatform.getTableName()) + "";
            tp.setId(platformId);
            tp.setPlatformCode(stringUtil.recountNew(Constant.AppCode.PLATFORM, 4));
            tp.setPlatformName(platformName);
            tp.setPlatformType(platformType);
            tp.setPlatformStatus(Constant.Status.START);
            tp.setCreatedTime(new Date());
            this.post(tp);
            if (StringUtils.isBlank(vendorInfo)) {
                return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "新增平台成功!", tp);
            }
            JSONArray jsonArr = JSON.parseArray(vendorInfo);
            for (int i = 0; i < jsonArr.size(); i++) {
                JSONObject jsonObj = jsonArr.getJSONObject(i);
                TVendorConfig tvc = new TVendorConfig();
                String vendorConfigId = batchUidService.getUid(qTVendorConfig.getTableName()) + "";
                tvc.setId(vendorConfigId);
                tvc.setPlatformId(platformId);
                tvc.setVendorId(jsonObj.getString("vendorId"));
                tvc.setVersionId(jsonObj.getString("versionId"));
                tvc.setConnectionType(jsonObj.getString("connectionType"));
                tvc.setAddressUrl(jsonObj.getString("addressUrl"));
                tvc.setEndpointUrl(jsonObj.getString("endpointUrl"));
                tvc.setNamespaceUrl(jsonObj.getString("namespaceUrl"));
                tvc.setDatabaseName(jsonObj.getString("databaseName"));
                tvc.setDatabaseUrl(jsonObj.getString("databaseUrl"));
                tvc.setDatabaseDriver(jsonObj.getString("databaseDriver"));
                tvc.setJsonParams(jsonObj.getString("jsonParams"));
                tvc.setUserName(jsonObj.getString("username"));
                tvc.setUserPassword(jsonObj.getString("password"));
                tvc.setCreatedTime(new Date());
                vendorConfigService.post(tvc);
                JSONArray hospitalArr = jsonObj.getJSONArray("hospitalConfig");
                for (int j = 0; j < hospitalArr.size(); j++) {
                    JSONObject hObj = hospitalArr.getJSONObject(j);
                    THospitalVendorLink hvl = new THospitalVendorLink();
                    hvl.setId(batchUidService.getUid(qTHospitalVendorLink.getTableName()) + "");
                    hvl.setVendorConfigId(vendorConfigId);
                    hvl.setHospitalId(hObj.getString("hospitalId"));
                    hvl.setVendorHospitalId(hObj.getString("vendorHospitalId"));
                    hvl.setCreatedTime(new Date());
                    hospitalVendorLinkService.post(hvl);
                }
            }
            return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "新增平台成功!", tp);
        } catch (Exception e) {
            logger.error("新增平台失败!", ExceptionUtil.dealException(e));
            e.printStackTrace();
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "新增平台失败!", ExceptionUtil.dealException(e));
        }
    }

    /** 修改平台 */
    private ResultDto updatePlatform(String id, String platformName, String platformType, String vendorInfo) {
        return null;
    }


    @ApiOperation(value = "更改启停用状态", notes = "更改启停用状态")
    @GetMapping("/updateStatus")
    public ResultDto updateStatus(
            @ApiParam(value = "平台id") @RequestParam(value = "id", required = true) String id,
            @ApiParam(value = "平台状态 1启用 2停用") @RequestParam(value = "platformStatus", required = true) String platformStatus) {
        sqlQueryFactory.update(qTPlatform).set(qTPlatform.platformStatus, platformStatus).set(qTPlatform.updatedTime, new Date())
                .where(qTPlatform.id.eq(id)).execute();
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "平台状态更改成功!", id);
    }


    @ApiOperation(value = "删除平台", notes = "删除平台")
    @GetMapping("/deletePlatform")
    public ResultDto deletePlatform(@ApiParam(value = "平台id") @RequestParam(value = "id", required = true) String id) {
        //删除平台
        this.delete(id);
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "删除平台成功!", id);
    }


}


