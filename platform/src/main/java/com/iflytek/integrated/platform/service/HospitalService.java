package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.common.*;
import com.iflytek.integrated.common.utils.ExceptionUtil;
import com.iflytek.integrated.common.utils.Utils;
import com.iflytek.integrated.platform.entity.THospital;
import com.iflytek.integrated.platform.validator.ValidationResult;
import com.iflytek.integrated.platform.validator.ValidatorHelper;
import com.iflytek.medicalboot.core.dto.PageRequest;
import com.iflytek.medicalboot.core.id.UidService;
import com.iflytek.medicalboot.core.querydsl.QuerydslService;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.StringPath;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
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
@RestController
public class HospitalService extends QuerydslService<THospital, String, THospital, StringPath, PageRequest<THospital>> {
    public HospitalService(){
        super(qTHospital,qTHospital.id);
    }
    private static final Logger logger = LoggerFactory.getLogger(HospitalService.class);

    @Autowired
    private UidService uidService;
    @Autowired
    private ValidatorHelper validatorHelper;

    @ApiOperation(value = "获取医院管理列表")
    @GetMapping("/{version}/pb/hospitalManage/getHospitalList")
    public ResultDto getHospitalListPage(String hospitalName,String areaCode,Integer pageNo,Integer pageSize){
        try {
            //查询条件
            ArrayList<Predicate> list = new ArrayList<>();
            list.add(qTHospital.status.eq(Constant.Status.YES));
            //如果区域id不为空，关联区域表查询所属区域下的医院
            String supCode = Utils.subAreaCode(areaCode);
            if(!StringUtils.isEmpty(supCode)){
                list.add(qTArea.areaCode.like(Utils.rightCreateFuzzyText(supCode)));
            }
            if(!StringUtils.isEmpty(hospitalName)){
                list.add(qTHospital.hospitalName.like(Utils.createFuzzyText(hospitalName)));
            }
            //根据查询条件获取医院列表
            QueryResults<THospital> queryResults = sqlQueryFactory.select(
                Projections.bean(
                        THospital.class,
                        qTHospital.id,
                        qTHospital.hospitalName,
                        qTHospital.hospitalCode,
                        qTHospital.updatedTime
                        )
                ).from(qTHospital)
                    .leftJoin(qTArea).on(qTArea.id.eq(qTHospital.areaId))
                    .where(list.toArray(new Predicate[list.size()]))
                    .limit(pageSize)
                    .offset((pageNo - 1) * pageSize)
                    .orderBy(qTHospital.updatedTime.desc())
                    .fetchResults();
            //分页
            TableData<THospital> tableData = new TableData<>(queryResults.getTotal(), queryResults.getResults());
            return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "", tableData);
        }catch (Exception e){
            logger.error("获取医院管理列表失败!", ExceptionUtil.dealException(e));
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "", ExceptionUtil.dealException(e));
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "医院管理删除")
    @DeleteMapping("/{version}/pb/hospitalManage/delHospitalById")
    public ResultDto delHospitalById(String id){
        if(StringUtils.isEmpty(id)){
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "", "医院id为空");
        }
        //判断是否存在医院
        List<String> hospitals = sqlQueryFactory.select(qTHospital.id).from(qTHospital)
                .where(qTHospital.id.eq(id).and(qTHospital.status.eq(Constant.Status.YES))).fetch();
        if(hospitals == null || hospitals.size() == 0){
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "", "不存在该医院");
        }
        //逻辑删除
        Long lon = sqlQueryFactory.update(qTHospital).set(qTHospital.status, Constant.Status.NO)
                .where(qTHospital.id.eq(id)).execute();
        if(lon > 0){
            return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "", "医院管理删除成功");
        }
        throw new RuntimeException("医院管理删除失败");
    }

    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "医院管理新增/编辑")
    @PostMapping("/{version}/pb/hospitalManage/saveAndUpdateHospital")
    public ResultDto saveAndUpdateHospital(@RequestBody THospital hospital){
        //校验参数是否完整
        ValidationResult validationResult = validatorHelper.validate(hospital);
        if (validationResult.isHasErrors()) {
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "", validationResult.getErrorMsg());
        }
        if(isExistence(hospital.getId(),hospital.getHospitalName(),hospital.getHospitalCode())){
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "", "医院名称或编码已存在");
        }
        if(StringUtils.isEmpty(hospital.getId())){
            //没有id，新增医院
            return insertHospital(hospital.getAreaId(),hospital.getHospitalName(),hospital.getHospitalCode());
        }
        else {
            //存在id时，编辑医院
            return updateHospital(hospital.getId(),hospital.getAreaId(),hospital.getHospitalName(),hospital.getHospitalCode());
        }
    }

    /**
     * 新增医院
     * @param areaId
     * @param hospitalName
     * @param hospitalCode
     * @return
     */
    private ResultDto insertHospital(String areaId, String hospitalName, String hospitalCode){
        Long lon = sqlQueryFactory.insert(qTHospital)
                .set(qTHospital.id, uidService.getUID()+"")
                .set(qTHospital.areaId,areaId)
                .set(qTHospital.status,Constant.Status.YES)
                .set(qTHospital.hospitalCode,hospitalCode)
                .set(qTHospital.hospitalName,hospitalName)
                .set(qTHospital.createdBy,"")
                .set(qTHospital.createdTime,new Date()).execute();
        if(lon > 0){
            return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "", "医院管理新增成功");
        }
        throw new RuntimeException("医院管理新增失败");
    }

    /**
     * 编辑医院
     * @param id
     * @param areaId
     * @param hospitalName
     * @param hospitalCode
     * @return
     */
    private ResultDto updateHospital(String id, String areaId, String hospitalName, String hospitalCode){
        Long lon = sqlQueryFactory.update(qTHospital)
                .set(qTHospital.areaId,areaId)
                .set(qTHospital.hospitalCode,hospitalCode)
                .set(qTHospital.hospitalName,hospitalName)
                .set(qTHospital.updatedBy,"")
                .set(qTHospital.updatedTime,new Date())
                .where(qTHospital.id.eq(id)).execute();
        if(lon > 0){
            return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "", "医院管理编辑成功");
        }
        throw new RuntimeException("医院管理编辑失败");
    }

    @ApiOperation(value = "医院配置")
    @GetMapping("/{version}/pb/hospitalManage/getAllHospital")
    public ResultDto getAllHospital(){
        List<THospital> hospitals = sqlQueryFactory.select(
                Projections.bean(
                        THospital.class,
                        qTHospital.hospitalName,
                        qTHospital.hospitalCode
                )
            ).from(qTHospital).orderBy(qTHospital.updatedTime.desc()).fetch();
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE,"",hospitals);
    }

    /**
     * 校验是否存在重复的医院
     * @param id
     * @param hospitalName
     * @param hospitalCode
     * @return
     */
    private boolean isExistence(String id,String hospitalName, String hospitalCode){
        //校验是否存在重复医院
        ArrayList<Predicate> list = new ArrayList<>();
        list.add(qTHospital.status.eq(Constant.Status.YES));
        list.add(qTHospital.hospitalCode.eq(hospitalCode)
                .or(qTHospital.hospitalName.eq(hospitalName)));
        if(!StringUtils.isEmpty(id)){
            list.add(qTHospital.id.notEqualsIgnoreCase(id));
        }
        List<String> hospitals = sqlQueryFactory.select(qTHospital.id).from(qTHospital)
                .where(list.toArray(new Predicate[list.size()])).fetch();
        if(hospitals == null || hospitals.size() == 0){
            return false;
        }
        return true;
    }
}
