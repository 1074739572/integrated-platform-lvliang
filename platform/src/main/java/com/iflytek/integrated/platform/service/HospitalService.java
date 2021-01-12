package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.common.*;
import com.iflytek.integrated.common.bean.UserLoginIntercept;
import com.iflytek.integrated.common.utils.ExceptionUtil;
import com.iflytek.integrated.platform.entity.THospitalVendorLink;
import com.iflytek.integrated.platform.utils.Utils;
import com.iflytek.integrated.platform.entity.THospital;
import com.iflytek.integrated.platform.validator.ValidationResult;
import com.iflytek.integrated.platform.validator.ValidatorHelper;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.iflytek.integrated.platform.entity.QTHospital.qTHospital;
import static com.iflytek.integrated.platform.entity.QTArea.qTArea;

/**
 * 医院管理
 * @author czzhan
 */
@Slf4j
@Api(tags = "医院管理")
@RestController
@RequestMapping("/{version}/pt/hospitalManage")
public class HospitalService extends QuerydslService<THospital, String, THospital, StringPath, PageRequest<THospital>> {
    public HospitalService(){
        super(qTHospital,qTHospital.id);
    }
    private static final Logger logger = LoggerFactory.getLogger(HospitalService.class);

    @Autowired
    private BatchUidService batchUidService;
    @Autowired
    private ValidatorHelper validatorHelper;
    @Autowired
    private HospitalVendorLinkService hospitalVendorLinkService;

    @ApiOperation(value = "获取医院管理列表")
    @GetMapping("/getHospitalList")
    public ResultDto getHospitalListPage(@RequestParam(value = "hospitalName", required = false) String hospitalName,
                                         @RequestParam(value = "areaCode", required = false) String areaCode,
                                         @RequestParam(defaultValue = "1")Integer pageNo,
                                         @RequestParam(defaultValue = "10")Integer pageSize){
        try {
            //查询条件
            ArrayList<Predicate> list = new ArrayList<>();
            list.add(qTHospital.status.eq(Constant.Status.YES));
            //如果区域id不为空，关联区域表查询所属区域下的医院
            String supCode = Utils.subAreaCode(areaCode);
            if(StringUtils.isNotEmpty(supCode)){
                list.add(qTArea.areaCode.like(Utils.rightCreateFuzzyText(supCode)));
            }
            if(StringUtils.isNotEmpty(hospitalName)){
                list.add(qTHospital.hospitalName.like(Utils.createFuzzyText(hospitalName)));
            }
            //根据查询条件获取医院列表
            QueryResults<THospital> queryResults = sqlQueryFactory.select(
                Projections.bean(
                        THospital.class,
                        qTHospital.id,
                        qTHospital.hospitalName,
                        qTHospital.hospitalCode,
                        qTHospital.updatedTime,
                        qTHospital.areaId
                    )
                ).from(qTHospital)
                    .leftJoin(qTArea).on(qTArea.id.eq(qTHospital.areaId))
                    .where(list.toArray(new Predicate[list.size()]))
                    .limit(pageSize)
                    .offset((pageNo - 1) * pageSize)
                    .orderBy(qTHospital.createdTime.desc())
                    .fetchResults();
            //分页
            TableData<THospital> tableData = new TableData<>(queryResults.getTotal(), queryResults.getResults());
            return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "获取医院管理列表成功", tableData);
        }catch (Exception e){
            logger.error("获取医院管理列表失败!", ExceptionUtil.dealException(e));
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "获取医院管理列表失败", ExceptionUtil.dealException(e));
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "医院管理删除")
    @PostMapping("/delHospitalById")
    public ResultDto delHospitalById(@ApiParam(value = "医院id") @RequestParam(value = "id", required = true) String id){
        //判断是否存在医院
        List<String> hospitals = sqlQueryFactory.select(qTHospital.id).from(qTHospital)
                .where(qTHospital.id.eq(id).and(qTHospital.status.eq(Constant.Status.YES))).fetch();
        if(hospitals == null || hospitals.size() == 0){
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "不存在该医院", "不存在该医院");
        }
        //删除前校验该医院是否关联厂商
        List<THospitalVendorLink> thvlList = hospitalVendorLinkService.getThvlListByHospitalId(id);
        if (CollectionUtils.isNotEmpty(thvlList)) {
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "该医院已有厂商相关联,无法删除!", "该医院已有厂商相关联,无法删除!");
        }
        //逻辑删除
        Long lon = sqlQueryFactory.update(qTHospital).set(qTHospital.status, Constant.Status.NO)
                .where(qTHospital.id.eq(id)).execute();
        if(lon <= 0){
            throw new RuntimeException("医院管理删除失败");
        }
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "医院管理删除成功", "医院管理删除成功");
    }

    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "医院管理新增/编辑")
    @PostMapping("/saveAndUpdateHospital")
    public ResultDto saveAndUpdateHospital(@RequestBody THospital hospital){
        //校验参数是否完整
        ValidationResult validationResult = validatorHelper.validate(hospital);
        if (validationResult.isHasErrors()) {
            return new ResultDto(Constant.ResultCode.ERROR_CODE, validationResult.getErrorMsg(), validationResult.getErrorMsg());
        }
        //校验是否获取到登录用户
        String loginUserName = UserLoginIntercept.LOGIN_USER.getLoginUserName();
        if(StringUtils.isBlank(loginUserName)){
            throw new RuntimeException("没有获取到登录用户");
        }
        //校验是否有重复医院
        isExistence(hospital.getId(),hospital.getHospitalName(),hospital.getHospitalCode());
        if(StringUtils.isEmpty(hospital.getId())){
            //没有id，新增医院
            hospital.setId(batchUidService.getUid(qTHospital.getTableName())+"");
            hospital.setStatus(Constant.Status.YES);
            hospital.setCreatedTime(new Date());
            hospital.setCreatedBy(loginUserName);
            this.post(hospital);
        }
        else {
            //存在id时，编辑医院
            hospital.setUpdatedTime(new Date());
            hospital.setUpdatedBy(loginUserName);
            Long lon = this.put(hospital.getId(),hospital);
            if(lon <= 0){
                throw new RuntimeException("医院管理编辑失败");
            }
        }
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "医院管理新增或编辑成功", "医院管理新增或编辑成功");
    }

    @ApiOperation(value = "医院配置")
    @GetMapping("/getAllHospital")
    public ResultDto getAllHospital(){
        List<THospital> hospitals = sqlQueryFactory.select(
                Projections.bean(
                        THospital.class,
                        qTHospital.id,
                        qTHospital.hospitalName,
                        qTHospital.hospitalCode
                )
            ).from(qTHospital).where(qTHospital.status.eq(Constant.Status.YES)).orderBy(qTHospital.createdTime.desc()).fetch();
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE,"医院配置信息获取成功", hospitals);
    }

    /**
     * 校验是否存在重复的医院
     * @param id
     * @param hospitalName
     * @param hospitalCode
     * @return
     */
    private void isExistence(String id,String hospitalName, String hospitalCode){
        //校验是否存在重复医院
        ArrayList<Predicate> list = new ArrayList<>();
        list.add(qTHospital.status.eq(Constant.Status.YES));
        list.add(qTHospital.hospitalCode.eq(hospitalCode)
                .or(qTHospital.hospitalName.eq(hospitalName)));
        if(StringUtils.isNotEmpty(id)){
            list.add(qTHospital.id.notEqualsIgnoreCase(id));
        }
        List<String> hospitals = sqlQueryFactory.select(qTHospital.id).from(qTHospital)
                .where(list.toArray(new Predicate[list.size()])).fetch();
        if(CollectionUtils.isNotEmpty(hospitals)){
            throw new RuntimeException("医院名称或编码已存在");
        }
    }

}
