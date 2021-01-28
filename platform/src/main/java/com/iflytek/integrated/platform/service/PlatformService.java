package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.common.dto.TableData;
import com.iflytek.integrated.common.intercept.UserLoginIntercept;
import com.iflytek.integrated.common.utils.ExceptionUtil;
import com.iflytek.integrated.platform.dto.PlatformDto;
import com.iflytek.integrated.platform.dto.VendorConfigDto;
import com.iflytek.integrated.platform.entity.TBusinessInterface;
import com.iflytek.integrated.platform.entity.TVendorConfig;
import com.iflytek.integrated.platform.entity.THospitalVendorLink;
import com.iflytek.integrated.platform.entity.TPlatform;
import com.iflytek.medicalboot.core.id.BatchUidService;
import com.querydsl.core.QueryResults;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
public class PlatformService extends BaseService<TPlatform, String, StringPath> {

    @Autowired
    private VendorConfigService vendorConfigService;
    @Autowired
    private HospitalVendorLinkService hospitalVendorLinkService;
    @Autowired
    private BusinessInterfaceService businessInterfaceService;
    @Autowired
    private BatchUidService batchUidService;

    private static final Logger logger = LoggerFactory.getLogger(PlatformService.class);

    public PlatformService(){
        super(qTPlatform, qTPlatform.id);
    }


    @ApiOperation(value = "根据项目id获取平台(分页)", notes = "根据项目id获取平台(分页)")
    @GetMapping("/getPlatformListById")
    public ResultDto<TableData<TPlatform>> getPlatformListById(
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
             .orderBy(qTPlatform.createdTime.desc())
             .fetchResults();
            //分页
            TableData<TPlatform> tableData = new TableData<>(queryResults.getTotal(), queryResults.getResults());
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "根据项目id获取平台成功!", tableData);
        } catch (Exception e) {
            logger.error("根据项目id获取平台失败! MSG:{}", ExceptionUtil.dealException(e));
            e.printStackTrace();
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "根据项目id获取平台失败!");
        }
    }


    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "新增or修改平台", notes = "新增or修改平台")
    @PostMapping("/saveAndUpdatePlatform")
    public ResultDto<String> saveAndUpdatePlatform(@RequestBody PlatformDto dto) {
        if (dto == null) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "数据传入错误!", "数据传入错误!");
        }
        //校验是否获取到登录用户
        String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
        if(StringUtils.isBlank(loginUserName)){
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!", "没有获取到登录用户!");
        }
        //平台名称校验
        String platformId = dto.getId();
        String platformName = dto.getPlatformName();
        boolean isExist = getPlatformNameIsExistByProjectId(platformId, dto.getProjectId(), platformName);
        if (isExist) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "平台名称为空或此项目下该名称已存在!", platformName);
        }
        if (StringUtils.isBlank(platformId)) {
            return savePlatform(dto, loginUserName);
        }
        return updatePlatform(dto, loginUserName);
    }

    /** 新增平台 */
    private ResultDto savePlatform(PlatformDto dto, String loginUserName) {
        TPlatform tp = new TPlatform();
        String platformId = batchUidService.getUid(qTPlatform.getTableName()) + "";
        tp.setId(platformId);
        tp.setPlatformCode(generateCode(qTPlatform.platformCode, qTPlatform, dto.getPlatformName()));
        tp.setProjectId(dto.getProjectId());
        tp.setPlatformName(dto.getPlatformName());
        tp.setPlatformType(dto.getPlatformType());
        tp.setPlatformStatus(Constant.Status.START);
        tp.setCreatedTime(new Date());
        tp.setCreatedBy(loginUserName);
        this.post(tp);
        //关联厂商
        List<VendorConfigDto> jsonArr = dto.getVendorInfo();
        if (CollectionUtils.isEmpty(jsonArr)) {
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "新增平台成功!", null);
        }
        for (int i = 0; i < jsonArr.size(); i++) {
            VendorConfigDto obj = jsonArr.get(i);
            TVendorConfig tvc = new TVendorConfig();
            String vendorConfigId = batchUidService.getUid(qTVendorConfig.getTableName()) + "";
            tvc.setId(vendorConfigId);
            tvc.setPlatformId(platformId);
            tvc.setVendorId(obj.getVendorId());
            tvc.setVersionId(obj.getVersionId());
            tvc.setConnectionType(obj.getConnectionType());
            tvc.setAddressUrl(obj.getAddressUrl());
            tvc.setEndpointUrl(obj.getEndpointUrl());
            tvc.setNamespaceUrl(obj.getNamespaceUrl());
            tvc.setDatabaseName(obj.getDatabaseName());
            tvc.setDatabaseUrl(obj.getDatabaseUrl());
            tvc.setDatabaseDriver(obj.getDatabaseDriver());
            tvc.setDriverUrl(obj.getDriverUrl());
            tvc.setJsonParams(obj.getJsonParams());
            tvc.setUserName(obj.getUserName());
            tvc.setUserPassword(obj.getUserPassword());
            tvc.setCreatedTime(new Date());
            tvc.setCreatedBy(loginUserName);
            vendorConfigService.post(tvc);
            List<Map<String, String>> hospitalArr = obj.getHospitalConfig();
            for (int j = 0; j < hospitalArr.size(); j++) {
                Map<String, String> hObj = hospitalArr.get(j);
                THospitalVendorLink hvl = new THospitalVendorLink();
                hvl.setId(batchUidService.getUid(qTHospitalVendorLink.getTableName()) + "");
                hvl.setVendorConfigId(vendorConfigId);
                hvl.setHospitalId(hObj.get("hospitalId"));
                hvl.setVendorHospitalId(hObj.get("vendorHospitalId"));
                hvl.setCreatedTime(new Date());
                hvl.setCreatedBy(loginUserName);
                hospitalVendorLinkService.post(hvl);
            }
        }
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "新增平台成功!", "新增平台成功!");
    }

    /** 修改平台 */
    private ResultDto updatePlatform(PlatformDto dto, String loginUserName) {
        //平台id
        String platformId = dto.getId();
        sqlQueryFactory.update(qTPlatform).set(qTPlatform.platformName, dto.getPlatformName())
                .set(qTPlatform.platformType, dto.getPlatformType())
                .set(qTPlatform.projectId, dto.getProjectId())
                .set(qTPlatform.updatedTime, new Date())
                .set(qTPlatform.updatedBy, loginUserName)
                .where(qTPlatform.id.eq(platformId)).execute();

        //获取更改前的厂商信息
        List<TVendorConfig> tvcList = vendorConfigService.getObjByPlatformId(platformId);
        //前台传递的新厂商信息
        List<VendorConfigDto> jsonArr = dto.getVendorInfo();
        for (int i = 0; i < jsonArr.size(); i++) {
            VendorConfigDto obj = jsonArr.get(i);
            String vendorConfigId = obj.getId();
            //新增厂商信息
            if (StringUtils.isBlank(vendorConfigId)) {
                TVendorConfig tvc = new TVendorConfig();
                vendorConfigId = batchUidService.getUid(qTVendorConfig.getTableName()) + "";
                tvc.setId(vendorConfigId);
                tvc.setPlatformId(platformId);
                tvc.setVendorId(obj.getVendorId());
                tvc.setVersionId(obj.getVersionId());
                tvc.setConnectionType(obj.getConnectionType());
                tvc.setAddressUrl(obj.getAddressUrl());
                tvc.setEndpointUrl(obj.getEndpointUrl());
                tvc.setNamespaceUrl(obj.getNamespaceUrl());
                tvc.setDatabaseName(obj.getDatabaseName());
                tvc.setDatabaseUrl(obj.getDatabaseUrl());
                tvc.setDatabaseDriver(obj.getDatabaseDriver());
                tvc.setDriverUrl(obj.getDriverUrl());
                tvc.setJsonParams(obj.getJsonParams());
                tvc.setUserName(obj.getUserName());
                tvc.setUserPassword(obj.getUserPassword());
                tvc.setCreatedTime(new Date());
                tvc.setCreatedBy(loginUserName);
                vendorConfigService.post(tvc);
                //医院配置
                List<Map<String, String>> hospitalArr = obj.getHospitalConfig();
                for (int j = 0; j < hospitalArr.size(); j++) {
                    Map<String, String> hObj = hospitalArr.get(j);
                    THospitalVendorLink hvl = new THospitalVendorLink();
                    hvl.setId(batchUidService.getUid(qTHospitalVendorLink.getTableName()) + "");
                    hvl.setVendorConfigId(vendorConfigId);
                    hvl.setHospitalId(hObj.get("hospitalId"));
                    hvl.setVendorHospitalId(hObj.get("vendorHospitalId"));
                    hvl.setCreatedTime(new Date());
                    hvl.setCreatedBy(loginUserName);
                    hospitalVendorLinkService.post(hvl);
                }

            } else {
                TVendorConfig tvc = vendorConfigService.getOne(vendorConfigId);
                //编辑厂商信息
                tvc.setId(vendorConfigId);
                tvc.setPlatformId(platformId);
                tvc.setVendorId(obj.getVendorId());
                tvc.setVersionId(obj.getVersionId());
                tvc.setConnectionType(obj.getConnectionType());
                tvc.setAddressUrl(obj.getAddressUrl());
                tvc.setEndpointUrl(obj.getEndpointUrl());
                tvc.setNamespaceUrl(obj.getNamespaceUrl());
                tvc.setDatabaseName(obj.getDatabaseName());
                tvc.setDatabaseUrl(obj.getDatabaseUrl());
                tvc.setDatabaseDriver(obj.getDatabaseDriver());
                tvc.setDriverUrl(obj.getDriverUrl());
                tvc.setJsonParams(obj.getJsonParams());
                tvc.setUserName(obj.getUserName());
                tvc.setUserPassword(obj.getUserPassword());
                tvc.setUpdatedTime(new Date());
                tvc.setUpdatedBy(loginUserName);
                vendorConfigService.put(vendorConfigId, tvc);
                //医院配置
                List<Map<String, String>> hospitalArr = obj.getHospitalConfig();
                for (int j = 0; j < hospitalArr.size(); j++) {
                    Map<String, String> hObj = hospitalArr.get(j);
                    String hvlId = hObj.get("id");
                    if (StringUtils.isBlank(hvlId)) {
                        //新增医院配置
                        THospitalVendorLink hvl = new THospitalVendorLink();
                        hvl.setId(batchUidService.getUid(qTHospitalVendorLink.getTableName()) + "");
                        hvl.setVendorConfigId(vendorConfigId);
                        hvl.setHospitalId(hObj.get("hospitalId"));
                        hvl.setVendorHospitalId(hObj.get("vendorHospitalId"));
                        hvl.setCreatedTime(new Date());
                        hvl.setCreatedBy(loginUserName);
                        hospitalVendorLinkService.post(hvl);
                    }else {
                        //编辑医院配置
                        THospitalVendorLink hvl = hospitalVendorLinkService.getOne(hvlId);
                        hvl.setId(hvlId);
                        hvl.setVendorConfigId(vendorConfigId);
                        hvl.setHospitalId(hObj.get("hospitalId"));
                        hvl.setVendorHospitalId(hObj.get("vendorHospitalId"));
                        hvl.setUpdatedTime(new Date());
                        hvl.setUpdatedBy(loginUserName);
                        hospitalVendorLinkService.put(hvlId, hvl);
                    }
                }
            }
        }
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "修改平台成功!", platformId);
    }


    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "修改厂商信息接口", notes = "修改厂商信息接口")
    @PostMapping("/updateVendorConfig")
    public ResultDto<String> updateVendorConfig(@RequestBody VendorConfigDto dto) {
        if (dto == null) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "数据传入有误!", "数据传入有误!");
        }
        //校验是否获取到登录用户
        String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
        if(StringUtils.isBlank(loginUserName)){
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!", "没有获取到登录用户!");
        }
        //平台id
        String platformId = dto.getPlatformId();
        //获取更改前的厂商信息
        List<TVendorConfig> tvcList = vendorConfigService.getObjByPlatformId(platformId);
        //前台传递的新厂商信息
        List<VendorConfigDto> jsonArr = dto.getVendorInfo();
        for (int i = 0; i < jsonArr.size(); i++) {
            VendorConfigDto obj = jsonArr.get(i);
            String vendorConfigId = obj.getId();
            //新增厂商信息
            if (StringUtils.isBlank(vendorConfigId)) {
                TVendorConfig tvc = new TVendorConfig();
                vendorConfigId = batchUidService.getUid(qTVendorConfig.getTableName()) + "";
                tvc.setId(vendorConfigId);
                tvc.setPlatformId(platformId);
                tvc.setVendorId(obj.getVendorId());
                tvc.setVersionId(obj.getVersionId());
                tvc.setConnectionType(obj.getConnectionType());
                tvc.setAddressUrl(obj.getAddressUrl());
                tvc.setEndpointUrl(obj.getEndpointUrl());
                tvc.setNamespaceUrl(obj.getNamespaceUrl());
                tvc.setDatabaseName(obj.getDatabaseName());
                tvc.setDatabaseUrl(obj.getDatabaseUrl());
                tvc.setDatabaseDriver(obj.getDatabaseDriver());
                tvc.setDriverUrl(obj.getDriverUrl());
                tvc.setJsonParams(obj.getJsonParams());
                tvc.setUserName(obj.getUserName());
                tvc.setUserPassword(obj.getUserPassword());
                tvc.setCreatedTime(new Date());
                tvc.setCreatedBy(loginUserName);
                vendorConfigService.post(tvc);
                //医院配置
                List<Map<String, String>> hospitalArr = obj.getHospitalConfig();
                for (int j = 0; j < hospitalArr.size(); j++) {
                    Map<String, String> hObj = hospitalArr.get(j);
                    THospitalVendorLink hvl = new THospitalVendorLink();
                    hvl.setId(batchUidService.getUid(qTHospitalVendorLink.getTableName()) + "");
                    hvl.setVendorConfigId(vendorConfigId);
                    hvl.setHospitalId(hObj.get("hospitalId"));
                    hvl.setVendorHospitalId(hObj.get("vendorHospitalId"));
                    hvl.setCreatedTime(new Date());
                    hvl.setCreatedBy(loginUserName);
                    hospitalVendorLinkService.post(hvl);
                }
            } else {
                TVendorConfig tvc = vendorConfigService.getOne(vendorConfigId);
                //编辑厂商信息
                tvc.setId(vendorConfigId);
                tvc.setPlatformId(platformId);
                tvc.setVendorId(obj.getVendorId());
                tvc.setVersionId(obj.getVersionId());
                tvc.setConnectionType(obj.getConnectionType());
                tvc.setAddressUrl(obj.getAddressUrl());
                tvc.setEndpointUrl(obj.getEndpointUrl());
                tvc.setNamespaceUrl(obj.getNamespaceUrl());
                tvc.setDatabaseName(obj.getDatabaseName());
                tvc.setDatabaseUrl(obj.getDatabaseUrl());
                tvc.setDatabaseDriver(obj.getDatabaseDriver());
                tvc.setDriverUrl(obj.getDriverUrl());
                tvc.setJsonParams(obj.getJsonParams());
                tvc.setUserName(obj.getUserName());
                tvc.setUserPassword(obj.getUserPassword());
                tvc.setUpdatedTime(new Date());
                tvc.setUpdatedBy(loginUserName);
                vendorConfigService.put(vendorConfigId, tvc);
                //医院配置
                List<Map<String, String>> hospitalArr = obj.getHospitalConfig();
                for (int j = 0; j < hospitalArr.size(); j++) {
                    Map<String, String> hObj = hospitalArr.get(j);
                    String hvlId = hObj.get("id");
                    if (StringUtils.isBlank(hvlId)) {
                        //新增医院配置
                        THospitalVendorLink hvl = new THospitalVendorLink();
                        hvl.setId(batchUidService.getUid(qTHospitalVendorLink.getTableName()) + "");
                        hvl.setVendorConfigId(vendorConfigId);
                        hvl.setHospitalId(hObj.get("hospitalId"));
                        hvl.setVendorHospitalId(hObj.get("vendorHospitalId"));
                        hvl.setCreatedTime(new Date());
                        hvl.setCreatedBy(loginUserName);
                        hospitalVendorLinkService.post(hvl);
                    }else {
                        //编辑医院配置
                        THospitalVendorLink hvl = hospitalVendorLinkService.getOne(hvlId);
                        hvl.setId(hvlId);
                        hvl.setVendorConfigId(vendorConfigId);
                        hvl.setHospitalId(hObj.get("hospitalId"));
                        hvl.setVendorHospitalId(hObj.get("vendorHospitalId"));
                        hvl.setUpdatedTime(new Date());
                        hvl.setUpdatedBy(loginUserName);
                        hospitalVendorLinkService.put(hvlId, hvl);
                    }
                }
            }
        }
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "修改厂商信息成功!", platformId);
    }


    @ApiOperation(value = "更改启停用状态", notes = "更改启停用状态")
    @PostMapping("/updateStatus")
    public ResultDto<String> updateStatus(
            @ApiParam(value = "平台id") @RequestParam(value = "id", required = true) String id,
            @ApiParam(value = "平台状态 1启用 2停用") @RequestParam(value = "platformStatus", required = true) String platformStatus) {
        String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
        sqlQueryFactory.update(qTPlatform)
                .set(qTPlatform.platformStatus, platformStatus)
                .set(qTPlatform.updatedTime, new Date())
                .set(qTPlatform.updatedBy, loginUserName)
                .where(qTPlatform.id.eq(id)).execute();
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "平台状态更改成功!", id);
    }


    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "删除平台", notes = "删除平台")
    @PostMapping("/deletePlatform")
    public ResultDto<String> deletePlatform(@ApiParam(value = "平台id") @RequestParam(value = "id", required = true) String id) {
        TPlatform tp = this.getOne(id);
        if (tp == null) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "该平台不存在!", "该平台不存在!");
        }
        //删除平台
        long count = this.delete(id);
        if (count <= 0) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "平台删除失败!", "平台删除失败!");
        }
        //获取平台下所有厂商配置
        List<TVendorConfig> tvcList = vendorConfigService.getObjByPlatformId(id);
        count = 0;
        for (TVendorConfig tvc : tvcList) {
            //删除医院与厂商配置关联信息
            count += hospitalVendorLinkService.deleteByVendorConfigId(tvc.getId());
        }
        //删除平台下所有关联的接口配置
        List<TBusinessInterface> tbiList = businessInterfaceService.getListByPlatform(id);
        for (TBusinessInterface tbi : tbiList) {
            businessInterfaceService.delete(tbi.getId());
        }
        //删除平台下的所有厂商配置信息
        long vcCount = vendorConfigService.delVendorConfigAll(id);

        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "平台"+tp.getPlatformName()+"删除成功,同时删除该平台下"+vcCount+"条厂商配置,"+count+"条厂商医院配置!",
                id);
    }

    /**
     * 获取项目下所有平台信息
     * @param projectId
     * @return
     */
    public List<TPlatform> getListByProjectId(String projectId) {
        List<TPlatform> list = sqlQueryFactory.select(qTPlatform).from(qTPlatform)
                .where(qTPlatform.projectId.eq(projectId)).fetch();
        return list;
    }

    /**
     * 校验项目下是否有相同平台名称
     * @param platformId
     * @param projectId
     * @param platformName
     * @return
     */
    public boolean getPlatformNameIsExistByProjectId(String platformId, String projectId, String platformName) {
        if (StringUtils.isBlank(platformName)) {
            return true;
        }
        ArrayList<Predicate> list = new ArrayList<>();
        list.add(qTPlatform.projectId.eq(projectId));
        list.add(qTPlatform.platformName.eq(platformName.trim()));
        if(StringUtils.isNotEmpty(platformId)) {
            list.add(qTPlatform.id.notEqualsIgnoreCase(platformId));
        }
        TPlatform tp = sqlQueryFactory.select(qTPlatform).from(qTPlatform)
                .where(list.toArray(new Predicate[list.size()]))
                .fetchFirst();
        if (tp != null) {
            return true;
        }
        return false;
    }


}
