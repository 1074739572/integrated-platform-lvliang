package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.common.Constant;
import com.iflytek.integrated.common.ResultDto;
import com.iflytek.integrated.common.utils.ExceptionUtil;
import com.iflytek.integrated.common.utils.Utils;
import com.iflytek.integrated.platform.dto.VendorConfigDto;
import com.iflytek.integrated.platform.entity.*;
import com.iflytek.medicalboot.core.dto.PageRequest;
import com.iflytek.medicalboot.core.id.BatchUidService;
import com.iflytek.medicalboot.core.querydsl.QuerydslService;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.StringPath;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.*;

import static com.iflytek.integrated.platform.entity.QTPlatform.qTPlatform;
import static com.iflytek.integrated.platform.entity.QTProject.qTProject;
import static com.iflytek.integrated.platform.entity.QTVendor.qTVendor;
import static com.iflytek.integrated.platform.entity.QTVendorConfig.qTVendorConfig;
import static com.iflytek.integrated.platform.entity.QTVendorDriveLink.qTVendorDriveLink;

/**
* 厂商管理
* @author weihe9
* @date 2020/12/12 17:16
*/
@Slf4j
@Api(tags = "厂商管理")
@RestController
@RequestMapping("/{version}/pb/vendorManage")
public class VendorService extends QuerydslService<TVendor, String, TVendor, StringPath, PageRequest<TVendor>> {

    @Autowired
    private VendorDriveLinkService vendorDriveLinkService;
    @Autowired
    private HospitalVendorLinkService hospitalVendorLinkService;
    @Autowired
    private VendorConfigService vendorConfigService;
    @Autowired
    private HospitalService hospitalService;
    @Autowired
    private BatchUidService batchUidService;
    @Autowired
    private Utils utils;

    private static final Logger logger = LoggerFactory.getLogger(VendorService.class);

    public VendorService(){
        super(qTVendor, qTVendor.id);
    }

    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "新增or修改厂商", notes = "新增or修改厂商")
    @GetMapping("/saveAndUpdateVendor")
    public ResultDto saveAndUpdateVendor(
            @ApiParam(value = "厂商id") @RequestParam(value = "id", required = false) String id,
            @ApiParam(value = "厂商名") @RequestParam(value = "vendorName", required = true) String vendorName,
            @ApiParam(value = "驱动-多个用,分隔") @RequestParam(value = "driveIds", required = true) String driveIds) {
        if (StringUtils.isBlank(id)) {
            return saveVendor(vendorName, driveIds);
        }
        return updateVendor(id, vendorName, driveIds);
    }


    /** 新增厂商 */
    private ResultDto saveVendor(String vendorName, String driveIds) {
        try {
            String vendorId = batchUidService.getUid(qTVendor.getTableName()) + "";
            TVendor tv = new TVendor();
            tv.setId(vendorId);
            tv.setVendorCode(utils.generateCode(qTVendor, qTVendor.vendorCode, vendorName));
            tv.setVendorName(vendorName);
            tv.setCreatedTime(new Date());
            this.post(tv);
            String[] driveIdArr = driveIds.split(",");
            for (int i = 0; i < driveIdArr.length; i++) {
                TVendorDriveLink tvdl = new TVendorDriveLink();
                tvdl.setId(batchUidService.getUid(qTVendorDriveLink.getTableName())+"");
                tvdl.setVendorId(vendorId);
                tvdl.setDriveId(driveIdArr[i]);
                tvdl.setCreatedTime(new Date());
                vendorDriveLinkService.post(tvdl);
            }
            return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "厂商新增成功!", null);
        } catch (Exception e) {
            logger.error("厂商新增失败!", ExceptionUtil.dealException(e));
            e.printStackTrace();
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "厂商新增失败!", ExceptionUtil.dealException(e));
        }
    }

    /** 修改厂商 */
    private ResultDto updateVendor(String vendorId, String vendorName, String driveIds) {
        try {
            //更新厂商信息
            sqlQueryFactory.update(qTVendor).set(qTVendor.vendorName, vendorName).set(qTVendor.updatedTime, new Date())
                    .where(qTVendor.id.eq(vendorId)).execute();
            //删除关联
            vendorDriveLinkService.deleteVendorDriveLinkById(vendorId);
            //添加新关联
            String[] driveIdArr = driveIds.split(",");
            for (int i = 0; i < driveIdArr.length; i++) {
                TVendorDriveLink tvdl = new TVendorDriveLink();
                tvdl.setId(batchUidService.getUid(qTVendorDriveLink.getTableName())+"");
                tvdl.setVendorId(vendorId);
                tvdl.setDriveId(driveIdArr[i]);
                tvdl.setCreatedTime(new Date());
                vendorDriveLinkService.post(tvdl);
            }
            return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "厂商修改成功!", null);
        } catch (Exception e) {
            logger.error("厂商修改失败!", ExceptionUtil.dealException(e));
            e.printStackTrace();
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "厂商修改失败!", ExceptionUtil.dealException(e));
        }
    }


    @ApiOperation(value = "删除厂商", notes = "删除厂商")
    @GetMapping("/delVendorById")
    public ResultDto delVendorById(@ApiParam(value = "厂商id") @RequestParam(value = "id", required = true) String id) {
        try {
            //删除厂商
            this.delete(id);
            //删除厂商与驱动关联
            vendorDriveLinkService.deleteVendorDriveLinkById(id);
        } catch (Exception e) {
            logger.error("厂商删除失败!", ExceptionUtil.dealException(e));
            e.printStackTrace();
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "厂商删除失败!", ExceptionUtil.dealException(e));
        }
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "厂商删除成功!", null);
    }

    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "获取厂商管理列表", notes = "获取厂商管理列表")
    @GetMapping("/getVendorList")
    public ResultDto getVendorList(
            @ApiParam(value = "厂商名") @RequestParam(value = "vendorName", required = false) String vendorName,
            @ApiParam(value = "页码", example = "1") @RequestParam(value = "pageNo", defaultValue = "1", required = false) Integer pageNo,
            @ApiParam(value = "每页大小", example = "10") @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
        ArrayList<Predicate> list = new ArrayList<>();
        if (StringUtils.isNotBlank(vendorName))
            list.add(qTVendor.vendorName.like(Utils.createFuzzyText(vendorName)));
        List<TVendor> rtnList = sqlQueryFactory.select(qTVendor).from(qTVendor)
                .where(list.toArray(new Predicate[list.size()]))
                .limit(pageSize).offset((pageNo - 1) * pageSize)
                .orderBy(qTVendor.updatedTime.desc())
                .fetch();
        //添加厂商驱动
        for (TVendor tv : rtnList) {
            List<TVendorDriveLink> tvdList = vendorDriveLinkService.getVendorDriveLinkByVendorId(tv.getId());
            String driveNameStr = "";
            for (int i = 0; i < tvdList.size(); i++) {
                driveNameStr += tvdList.get(i).getDriveName();
                if (i <tvdList.size()- 1) driveNameStr += " | ";
            }
            tv.setDriveName(driveNameStr);
        }
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "获取厂商管理列表!", rtnList);
    }


    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "获取厂商信息", notes = "获取厂商信息")
    @GetMapping("/getVendorInfoList")
    public ResultDto getVendorInfoList(@ApiParam(value = "平台id") @RequestParam(value = "platformId", required = true) String platformId) {
        try {
            List<TVendorConfig> VCList = sqlQueryFactory.select(qTVendorConfig).from(qTVendorConfig)
                    .where(qTVendorConfig.platformId.eq(platformId)).fetch();
            List<VendorConfigDto> list = new ArrayList<>();
            for (TVendorConfig obj : VCList) {
                VendorConfigDto vcd = new VendorConfigDto();
                BeanUtils.copyProperties(obj, vcd);
                TVendor tv = this.getOne(vcd.getVendorId());//厂商信息
                vcd.setVendorCode(tv.getVendorCode());
                vcd.setVendorName(tv.getVendorName());
                List<THospitalVendorLink> hvlvcList = hospitalVendorLinkService.getTHospitalVendorLinkByVendorConfigId(obj.getId());
                List<Map<String, String>> hospitalConfigList = new ArrayList<>();
                for (int i= 0; i < hvlvcList.size(); i++) {
                    Map<String, String> map = new HashMap<>();
                    map.put("vendorHospitalId", hvlvcList.get(i).getVendorHospitalId());//厂商医院id
                    THospital h = hospitalService.getOne(hvlvcList.get(i).getHospitalId());//医院信息
                    map.put("hospitalCode", h.getHospitalCode());
                    map.put("hospitalName", h.getHospitalName());
                    hospitalConfigList.add(map);
                }
                vcd.setHospitalConfig(hospitalConfigList);
                list.add(vcd);
            }
            return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "获取厂商信息成功!", list);
        } catch (BeansException e) {
            logger.error("获取厂商信息失败!", ExceptionUtil.dealException(e));
            e.printStackTrace();
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "获取厂商信息失败!", ExceptionUtil.dealException(e));
        }
    }


    @ApiOperation(value = "删除平台下厂商配置信息", notes = "删除平台下厂商配置信息")
    @GetMapping("/delVendorConfig")
    public ResultDto delVendorConfig(@ApiParam(value = "平台id") @RequestParam(value = "platformId", required = true) String platformId,
                                     @ApiParam(value = "厂商id") @RequestParam(value = "vendorId", required = true) String vendorId) {
        TVendorConfig tvc = vendorConfigService.getObjByPlatformAndVendor(platformId, vendorId);
        if (tvc != null) {
            //删除厂商配置
            vendorConfigService.delete(tvc.getId());
            //删除厂商配置关联的医院
            hospitalVendorLinkService.deleteByVendorConfigId(tvc.getId());
            return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "厂商配置删除成功!", null);
        }
        return new ResultDto(Constant.ResultCode.ERROR_CODE, "根据平台与厂商id未查到该厂商配置信息!", null);
    }


    @ApiOperation(value = "删除厂商下医院配置信息", notes = "删除厂商下医院配置信息")
    @GetMapping("/delHospitalVendorByVendorConfig")
    public ResultDto delHospitalVendorByVendorConfig(
            @ApiParam(value = "平台id") @RequestParam(value = "platformId", required = true) String platformId,
            @ApiParam(value = "厂商id") @RequestParam(value = "vendorId", required = true) String vendorId) {
        TVendorConfig tvc = vendorConfigService.getObjByPlatformAndVendor(platformId, vendorId);
        if (tvc != null) {
            //删除厂商配置关联的医院
            hospitalVendorLinkService.deleteByVendorConfigId(tvc.getId());
            return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "厂商下医院配置信息删除成功!", null);
        }
        return new ResultDto(Constant.ResultCode.ERROR_CODE, "根据平台与厂商id未查到该厂商配置信息!", null);
    }


    @ApiOperation(value = "选择厂商下拉(可根据当前项目操作选择)")
    @GetMapping("/getDisVendor")
    public ResultDto getDisVendor(@ApiParam(value = "项目id") @RequestParam(value = "projectId", required = false) String projectId,
                @ApiParam(value = "操作 1获取当前平台下的厂商 2获取非当前平台下的厂商") @RequestParam(defaultValue = "1", value = "status", required = false) String status) {
        List<TVendor> vendors = null;
        if (StringUtils.isNotBlank(projectId) && "1".equals(status)) {
            //返回当前项目下的厂商
            vendors = sqlQueryFactory.select(qTVendor).from(qTVendor)
                    .leftJoin(qTVendorConfig).on(qTVendorConfig.vendorId.eq(qTVendor.id))
                    .leftJoin(qTPlatform).on(qTPlatform.id.eq(qTVendorConfig.platformId))
                    .where(qTPlatform.projectId.eq(projectId))
                    .orderBy(qTVendor.updatedTime.desc()).fetch();
            return new ResultDto(Constant.ResultCode.SUCCESS_CODE,"数据获取成功!", vendors);
        }
        //获取所有厂商
        vendors = sqlQueryFactory.select(qTVendor).from(qTVendor) .orderBy(qTVendor.updatedTime.desc()).fetch();
        if (StringUtils.isBlank(projectId)) {
            return new ResultDto(Constant.ResultCode.SUCCESS_CODE,"数据获取成功!", vendors);
        }
        //获取当前项目下的厂商
        List<TVendor> tvList = sqlQueryFactory.select(qTVendor).from(qTVendor)
                            .leftJoin(qTVendorConfig).on(qTVendorConfig.vendorId.eq(qTVendor.id))
                            .leftJoin(qTPlatform).on(qTPlatform.id.eq(qTVendorConfig.platformId))
                            .where(qTPlatform.projectId.eq(projectId))
                            .orderBy(qTVendor.updatedTime.desc()).fetch();
        //去除当前项目下的厂商
        vendors.removeAll(tvList);
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE,"数据获取成功!", vendors);
    }


}
