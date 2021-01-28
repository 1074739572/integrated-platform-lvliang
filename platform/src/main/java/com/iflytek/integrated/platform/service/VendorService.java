package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.common.dto.TableData;
import com.iflytek.integrated.common.intercept.UserLoginIntercept;
import com.iflytek.integrated.common.utils.ExceptionUtil;
import com.iflytek.integrated.platform.dto.JoltDebuggerDto;
import com.iflytek.integrated.platform.utils.NiFiRequestUtil;
import com.iflytek.integrated.platform.utils.PlatformUtil;
import com.iflytek.integrated.platform.dto.VendorConfigDto;
import com.iflytek.integrated.platform.entity.*;
import com.iflytek.integrated.common.validator.ValidationResult;
import com.iflytek.integrated.common.validator.ValidatorHelper;
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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static com.iflytek.integrated.platform.entity.QTInterfaceParam.qTInterfaceParam;
import static com.iflytek.integrated.platform.entity.QTPlatform.qTPlatform;
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
@RequestMapping("/{version}/pt/vendorManage")
public class VendorService extends BaseService<TVendor, String, StringPath> {

    @Autowired
    private VendorDriveLinkService vendorDriveLinkService;
    @Autowired
    private HospitalVendorLinkService hospitalVendorLinkService;
    @Autowired
    private VendorConfigService vendorConfigService;
    @Autowired
    private HospitalService hospitalService;
    @Autowired
    private BusinessInterfaceService businessInterfaceService;
    @Autowired
    private BatchUidService batchUidService;
    @Autowired
    private ValidatorHelper validatorHelper;
    @Autowired
    private NiFiRequestUtil niFiRequestUtil;

    private static final Logger logger = LoggerFactory.getLogger(VendorService.class);

    public VendorService(){
        super(qTVendor, qTVendor.id);
    }

    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "新增or修改厂商", notes = "新增or修改厂商")
    @PostMapping("/saveAndUpdateVendor")
    public ResultDto<String> saveAndUpdateVendor(
            @ApiParam(value = "厂商id") @RequestParam(value = "id", required = false) String id,
            @ApiParam(value = "厂商名") @RequestParam(value = "vendorName", required = true) String vendorName,
            @ApiParam(value = "驱动-多个用,分隔") @RequestParam(value = "driveIds", required = true) String driveIds) {
        //校验是否获取到登录用户
        String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
        if(StringUtils.isBlank(loginUserName)){
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!", "没有获取到登录用户!");
        }
        if (StringUtils.isBlank(id)) {
            return saveVendor(vendorName, driveIds, loginUserName);
        }
        return updateVendor(id, vendorName, driveIds, loginUserName);
    }


    /** 新增厂商 */
    private ResultDto saveVendor(String vendorName, String driveIds, String loginUserName) {
        if (null != this.getTVendorByName(null, vendorName)) {
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "厂商名未填写或该厂商名已存在!", "厂商名未填写或该厂商名已存在!");
        }
        String vendorId = batchUidService.getUid(qTVendor.getTableName()) + "";
        TVendor tv = new TVendor();
        tv.setId(vendorId);
        tv.setVendorCode(generateCode(qTVendor.vendorCode, vendorName));
        tv.setVendorName(vendorName);
        tv.setCreatedTime(new Date());
        tv.setCreatedBy(loginUserName);
        this.post(tv);
        if (StringUtils.isNotBlank(driveIds)) {
            String[] driveIdArr = driveIds.split(",");
            for (int i = 0; i < driveIdArr.length; i++) {
                TVendorDriveLink tvdl = new TVendorDriveLink();
                tvdl.setId(batchUidService.getUid(qTVendorDriveLink.getTableName())+"");
                tvdl.setVendorId(vendorId);
                tvdl.setDriveId(driveIdArr[i]);
                tvdl.setDriveOrder(i+1);
                tvdl.setCreatedTime(new Date());
                tvdl.setCreatedBy(loginUserName);
                vendorDriveLinkService.post(tvdl);
            }
        }
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "厂商新增成功!", null);
    }

    /** 修改厂商 */
    private ResultDto updateVendor(String vendorId, String vendorName, String driveIds, String loginUserName) {
        if (null != this.getTVendorByName(vendorId, vendorName)) {
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "厂商名未填写或该厂商名已存在!", "厂商名未填写或该厂商名已存在!");
        }
        //更新厂商信息
        long l = sqlQueryFactory.update(qTVendor)
                .set(qTVendor.vendorName, vendorName)
                .set(qTVendor.updatedTime, new Date())
                .set(qTVendor.updatedBy, loginUserName)
                .where(qTVendor.id.eq(vendorId)).execute();
        if (l <= 0) {
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "厂商信息更新失败!", vendorId);
        }
        //删除关联
        vendorDriveLinkService.deleteVendorDriveLinkById(vendorId);
        //添加新关联
        String[] driveIdArr = driveIds.split(",");
        for (int i = 0; i < driveIdArr.length; i++) {
            TVendorDriveLink tvdl = new TVendorDriveLink();
            tvdl.setId(batchUidService.getUid(qTVendorDriveLink.getTableName())+"");
            tvdl.setVendorId(vendorId);
            tvdl.setDriveId(driveIdArr[i]);
            tvdl.setDriveOrder(i+1);
            tvdl.setCreatedTime(new Date());
            tvdl.setCreatedBy(loginUserName);
            vendorDriveLinkService.post(tvdl);
        }
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "厂商修改成功!", vendorId);
    }


    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "删除厂商", notes = "删除厂商")
    @PostMapping("/delVendorById")
    public ResultDto<String> delVendorById(@ApiParam(value = "厂商id") @RequestParam(value = "id", required = true) String id) {
        //厂商配置关联数据校验
        List<TVendorConfig> tvcList = vendorConfigService.getObjByVendorId(id);
        if (CollectionUtils.isNotEmpty(tvcList)) {
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "该厂商已有厂商配置数据相关联,无法删除!", "该厂商已有厂商配置数据相关联,无法删除!");
        }
        //删除厂商
        long count = this.delete(id);
        if (count < 1) {
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "删除厂商失败!", id);
        }
        //删除厂商与驱动关联
        vendorDriveLinkService.deleteVendorDriveLinkById(id);
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "厂商删除成功!", id);
    }


    @ApiOperation(value = "获取厂商管理列表", notes = "获取厂商管理列表")
    @GetMapping("/getVendorList")
    public ResultDto<TableData<TVendor>> getVendorList(
            @ApiParam(value = "厂商名") @RequestParam(value = "vendorName", required = false) String vendorName,
            @ApiParam(value = "页码", example = "1") @RequestParam(value = "pageNo", defaultValue = "1", required = false) Integer pageNo,
            @ApiParam(value = "每页大小", example = "10") @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
        ArrayList<Predicate> list = new ArrayList<>();
        if (StringUtils.isNotBlank(vendorName)) {
            list.add(qTVendor.vendorName.like(PlatformUtil.createFuzzyText(vendorName)));
        }
        QueryResults<TVendor> queryResults = sqlQueryFactory.select(qTVendor).from(qTVendor)
                .where(list.toArray(new Predicate[list.size()]))
                .limit(pageSize)
                .offset((pageNo - 1) * pageSize)
                .orderBy(qTVendor.createdTime.desc())
                .fetchResults();
        //添加厂商驱动
        List<TVendor> rtnList = queryResults.getResults();
        for (TVendor tv : rtnList) {
            List<TVendorDriveLink> tvdList = vendorDriveLinkService.getVendorDriveLinkByVendorId(tv.getId());
            String driveNameStr = "";
            for (int i = 0; i < tvdList.size(); i++) {
                TVendorDriveLink tvdl = tvdList.get(i);
                if (StringUtils.isNotBlank(tvdl.getDriveName())) {
                    driveNameStr += tvdList.get(i).getDriveName();
                    if (i <tvdList.size()- 1) {
                        driveNameStr += " | ";
                    }
                }
            }
            tv.setDriveName(driveNameStr);
        }
        TableData<TVendor> tableData = new TableData<>(queryResults.getTotal(), queryResults.getResults());
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "获取厂商管理列表!", tableData);
    }


    @ApiOperation(value = "获取厂商信息", notes = "获取厂商信息")
    @GetMapping("/getVendorInfoList")
    public ResultDto<List<VendorConfigDto>> getVendorInfoList(@ApiParam(value = "平台id") @RequestParam(value = "platformId", required = true) String platformId) {
        try {
            List<TVendorConfig> VCList = sqlQueryFactory.select(qTVendorConfig).from(qTVendorConfig)
                    .where(qTVendorConfig.platformId.eq(platformId)).fetch();
            List<VendorConfigDto> list = new ArrayList<>();
            for (TVendorConfig obj : VCList) {
                VendorConfigDto vcd = new VendorConfigDto();
                BeanUtils.copyProperties(obj, vcd);
                //厂商信息
                TVendor tv = this.getOne(vcd.getVendorId());
                if (tv != null) {
                    vcd.setVendorCode(tv.getVendorCode());
                    vcd.setVendorName(tv.getVendorName());
                }
                List<THospitalVendorLink> hvlvcList = hospitalVendorLinkService.getTHospitalVendorLinkByVendorConfigId(obj.getId());
                List<Map<String, String>> hospitalConfigList = new ArrayList<>();
                for (int i= 0; i < hvlvcList.size(); i++) {
                    Map<String, String> map = new HashMap<>();
                    //厂商医院配置
                    map.put("id", hvlvcList.get(i).getId());
                    //厂商医院id
                    map.put("vendorHospitalId", hvlvcList.get(i).getVendorHospitalId());
                    //医院信息
                    THospital h = hospitalService.getOne(hvlvcList.get(i).getHospitalId());
                    if (h != null) {
                        map.put("hospitalCode", h.getHospitalCode());
                        map.put("hospitalId", h.getId());
                    }
                    hospitalConfigList.add(map);
                }
                vcd.setHospitalConfig(hospitalConfigList);
                list.add(vcd);
            }
            return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "获取厂商信息成功!", list);
        } catch (BeansException e) {
            logger.error("获取厂商信息失败! MSG:{}", ExceptionUtil.dealException(e));
            e.printStackTrace();
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "获取厂商信息失败!", ExceptionUtil.dealException(e));
        }
    }


    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "删除平台下厂商配置信息", notes = "删除平台下厂商配置信息")
    @PostMapping("/delVendorConfig")
    public ResultDto<String> delVendorConfig(@ApiParam(value = "平台id") @RequestParam(value = "platformId", required = true) String platformId,
                                     @ApiParam(value = "厂商id") @RequestParam(value = "vendorId", required = true) String vendorId) {
        TVendorConfig tvc = vendorConfigService.getObjByPlatformAndVendor(platformId, vendorId);
        if (tvc != null) {
            List<TBusinessInterface> tbiList = businessInterfaceService.getListByVendorConfigId(tvc.getId());
            if (CollectionUtils.isNotEmpty(tbiList)) {
                return new ResultDto(Constant.ResultCode.ERROR_CODE, "该厂商已有接口配置数据相关联,无法删除!", "该厂商已有接口配置数据相关联,无法删除!");
            }
            //删除厂商配置
            vendorConfigService.delete(tvc.getId());
            //删除厂商配置关联的医院
            hospitalVendorLinkService.deleteByVendorConfigId(tvc.getId());
            return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "厂商配置删除成功!", "厂商配置删除成功!");
        }
        return new ResultDto(Constant.ResultCode.ERROR_CODE, "根据平台id与厂商id未查到该厂商配置信息!", "根据平台id与厂商id未查到该厂商配置信息!");
    }


    @ApiOperation(value = "删除厂商下医院配置信息", notes = "删除厂商下医院配置信息")
    @PostMapping("/delHospitalVendorByVendorConfig")
    public ResultDto<String> delHospitalVendorByVendorConfig(
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
    public ResultDto<List<TVendor>> getDisVendor(@ApiParam(value = "项目id") @RequestParam(value = "projectId", required = false) String projectId,
                @ApiParam(value = "操作 1获取当前项目下的厂商 2获取非当前项目下的厂商") @RequestParam(defaultValue = "1", value = "status", required = false) String status) {
        List<TVendor> vendors = null;
        if (StringUtils.isNotBlank(projectId) && Constant.Operation.CURRENT.equals(status)) {
            //返回当前项目下的厂商
            vendors = sqlQueryFactory.select(qTVendor).from(qTVendor)
                    .leftJoin(qTVendorConfig).on(qTVendorConfig.vendorId.eq(qTVendor.id))
                    .leftJoin(qTPlatform).on(qTPlatform.id.eq(qTVendorConfig.platformId))
                    .where(qTPlatform.projectId.eq(projectId))
                    .orderBy(qTVendor.createdTime.desc()).fetch();
            return new ResultDto(Constant.ResultCode.SUCCESS_CODE,"数据获取成功!", vendors);
        }
        //获取所有厂商
        vendors = sqlQueryFactory.select(qTVendor).from(qTVendor) .orderBy(qTVendor.createdTime.desc()).fetch();
        if (StringUtils.isBlank(projectId)) {
            return new ResultDto(Constant.ResultCode.SUCCESS_CODE,"数据获取成功!", vendors);
        }
        //获取当前项目下的厂商
        List<TVendor> tvList = sqlQueryFactory.select(qTVendor).from(qTVendor)
                            .leftJoin(qTVendorConfig).on(qTVendorConfig.vendorId.eq(qTVendor.id))
                            .leftJoin(qTPlatform).on(qTPlatform.id.eq(qTVendorConfig.platformId))
                            .where(qTPlatform.projectId.eq(projectId))
                            .orderBy(qTVendor.createdTime.desc()).fetch();
        //去除当前项目下的厂商
        vendors.removeAll(tvList);
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE,"数据获取成功!", vendors);
    }


    @ApiOperation(value = "根据平台id获取厂商信息")
    @GetMapping("/getDisVendorByPlatform")
    public ResultDto<List<TVendor>> getDisVendorByPlatform(@ApiParam(value = "平台id") @RequestParam(value = "platformId", required = true) String platformId) {
        List<TVendor> vendors = sqlQueryFactory.select(
                Projections.bean(TVendor.class,qTVendor.id,qTVendor.vendorName,qTVendor.vendorCode,qTVendor.createdBy,qTVendor.createdTime,
                        qTVendor.updatedBy,qTVendor.updatedTime,qTVendorConfig.connectionType.as("connectionType")))
                .from(qTVendor)
                .leftJoin(qTVendorConfig).on(qTVendorConfig.vendorId.eq(qTVendor.id))
                .where(qTVendorConfig.platformId.eq(platformId)).fetch();
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE,"数据获取成功!", vendors);
    }


    @ApiOperation(value = "厂商接口调试数据获取")
    @PostMapping("/getInterfaceDebugger")
    public ResultDto<String> getInterfaceDebugger(String interfaceId){
        if(StringUtils.isBlank(interfaceId)){
            return new ResultDto(Constant.ResultCode.ERROR_CODE,"","标准接口id必传");
        }
        try {
            //获取入参列表
            List<String> paramNames = sqlQueryFactory.select(qTInterfaceParam.paramName).from(qTInterfaceParam)
                    .where(qTInterfaceParam.interfaceId.eq(interfaceId).and(qTInterfaceParam.paramInOut.eq(Constant.ParmInOut.IN))).fetch();
            return new ResultDto(Constant.ResultCode.SUCCESS_CODE,"入参列表获取成功!", paramNames);
        }
        catch (Exception e){
            return new ResultDto(Constant.ResultCode.ERROR_CODE,"厂商接口调试数据失败!","厂商接口调试数据失败!");
        }
    }


    @ApiOperation(value = "厂商接口调试接口")
    @PostMapping("/interfaceDebugger")
    public ResultDto<Map> interfaceDebugger(@RequestBody JoltDebuggerDto dto){
        //校验参数是否完整
        ValidationResult validationResult = validatorHelper.validate(dto);
        if (validationResult.isHasErrors()) {
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "", validationResult.getErrorMsg());
        }
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE,"", niFiRequestUtil.joltDebugger(dto));
    }


    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "根据厂商配置id删除厂商配置信息")
    @PostMapping("/delVendorConfigById")
    public ResultDto<String> delVendorConfigById(@ApiParam(value = "厂商配置id") @RequestParam(value = "id", required = true) String id,
                                         @ApiParam(value = "平台id") @RequestParam(value = "platformId", required = false) String platformId) {
        TVendorConfig tvc = vendorConfigService.getOne(id);
        if (tvc == null) {
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "根据id查询不到该厂商配置信息!", id);
        }
        //获取接口配置列表信息
        List<TBusinessInterface> queryResults = businessInterfaceService.getInterfaceConfigureList(platformId);
        if (CollectionUtils.isNotEmpty(queryResults)) {
            for (TBusinessInterface tbi : queryResults) {
                String vendorConfigId = tbi.getVendorConfigId();
                if (id.equals(vendorConfigId)) {
                    return new ResultDto(Constant.ResultCode.ERROR_CODE, "该厂商配置已有关联,无法删除!", id);
                }
            }
        }
        long count = vendorConfigService.delete(id);
        if (count < 1) {
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "删除失败!", id);
        }
        //删除厂商与医院关联
        hospitalVendorLinkService.deleteByVendorConfigId(id);
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE,"删除成功!", "删除成功!");
    }


    @ApiOperation(value = "根据厂商医院配置id删除厂商医院配置信息")
    @PostMapping("/delHospitalVendorById")
    public ResultDto<String> delHospitalVendorById(@ApiParam(value = "厂商医院配置id") @RequestParam(value = "id", required = true) String id) {
        THospitalVendorLink thvl = hospitalVendorLinkService.getOne(id);
        if (thvl == null) {
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "根据id查询不到该厂商医院配置信息!", id);
        }
        long count = hospitalVendorLinkService.delete(id);
        if (count < 1) {
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "删除失败!", id);
        }
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE,"删除成功!", "删除成功!");
    }


    /**
     * 根据厂商名称获取厂商信息
     * @param vendorId
     * @param vendorName
     * @return
     */
    private TVendor getTVendorByName(String vendorId, String vendorName) {
        if (StringUtils.isBlank(vendorName)) {
            return null;
        }
        ArrayList<Predicate> list = new ArrayList<>();
        list.add(qTVendor.vendorName.eq(vendorName));
        if (StringUtils.isNotBlank(vendorId)) {
            list.add(qTVendor.id.notEqualsIgnoreCase(vendorId));
        }
        return sqlQueryFactory.select(qTVendor).from(qTVendor).where(list.toArray(new Predicate[list.size()])).fetchFirst();
    }


}
