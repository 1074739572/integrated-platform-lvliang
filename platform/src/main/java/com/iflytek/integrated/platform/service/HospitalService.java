package com.iflytek.integrated.platform.service;

import com.alibaba.fastjson.JSONObject;
import com.iflytek.integrated.common.*;
import com.iflytek.integrated.platform.entity.THospital;
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

import javax.servlet.http.HttpServletRequest;

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

    @ApiOperation(value = "获取医院管理列表")
    @GetMapping("/{version}/pb/hospitalManage/getHospitalList")
    public ResultDto getHospitalListPage(HttpServletRequest request,
            @RequestParam(value = "hospitalName", required = false) String hospitalName,
            @RequestParam(value = "areaCode", required = false) String areaCode,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize){
        try {
            //评价查询条件
            ArrayList<Predicate> list = new ArrayList<>();
            list.add(qTHospital.status.equalsIgnoreCase(Constant.YES));
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
            return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "数据获取成功", tableData);
        }catch (Exception e){
            logger.error("获取医院管理列表失败!", e);
            return new ResultDto(Constant.ResultCode.ERROR_CODE, ExceptionUtil.dealException(e), null);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "医院管理删除")
    @DeleteMapping("/{version}/pb/hospitalManage/delHospitalById")
    public ResultDto delHospitalById(@RequestParam(value = "id", required = true) String id){
        //判断是否存在医院
        List<String> hospitals = sqlQueryFactory.select(qTHospital.id).from(qTHospital)
                .where(qTHospital.id.eq(id).and(qTHospital.status.eq(Constant.YES))).fetch();
        if(hospitals == null || hospitals.size() == 0){
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "不存在该医院", null);
        }
        //逻辑删除
        Long lon = sqlQueryFactory.update(qTHospital).set(qTHospital.status, Constant.NO)
                .where(qTHospital.id.eq(id)).execute();
        if(lon > 0){
            return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "医院管理删除成功", null);
        }else {
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "医院管理删除失败", null);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "医院管理新增/编辑")
    @PostMapping("/{version}/pb/hospitalManage/saveAndUpdateHospital")
    public ResultDto saveAndUpdateHospital(
            @RequestParam(value = "id", required = false) String id,
            @RequestParam(value = "areaId", required = true) String areaId,
            @RequestParam(value = "hospitalName", required = true) String hospitalName,
            @RequestParam(value = "hospitalCode", required = true) String hospitalCode){
        if(isExistence(id,hospitalName,hospitalCode)){
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "医院名称或编码已存在", null);
        }
        if(StringUtils.isEmpty(id)){
            //没有id，新增医院
            return insertHospital(areaId,hospitalName,hospitalCode);
        }
        else {
            //存在id时，编辑医院
            return updateHospital(id,areaId,hospitalName,hospitalCode);
        }
    }


    /**
     * 新增医院
     * @param areaId
     * @param hospitalName
     * @param hospitalCode
     * @return
     */
    private ResultDto insertHospital(
            @RequestParam(value = "areaId", required = true) String areaId,
            @RequestParam(value = "hospitalName", required = true) String hospitalName,
            @RequestParam(value = "hospitalCode", required = true) String hospitalCode){
        Long lon = sqlQueryFactory.insert(qTHospital)
                .set(qTHospital.id, JSONObject.toJSONString(uidService.getUID()))
                .set(qTHospital.areaId,areaId)
                .set(qTHospital.status,Constant.YES)
                .set(qTHospital.hospitalCode,hospitalCode)
                .set(qTHospital.hospitalName,hospitalName)
                .set(qTHospital.createdTime,new Date()).execute();
        if(lon > 0){
            return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "医院管理新增成功", null);
        }else {
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "医院管理新增失败", null);
        }
    }

    /**
     * 编辑医院
     * @param id
     * @param areaId
     * @param hospitalName
     * @param hospitalCode
     * @return
     */
    private ResultDto updateHospital(
            @RequestParam(value = "id", required = true) String id,
            @RequestParam(value = "areaId", required = true) String areaId,
            @RequestParam(value = "hospitalName", required = true) String hospitalName,
            @RequestParam(value = "hospitalCode", required = true) String hospitalCode){
        Long lon = sqlQueryFactory.update(qTHospital)
                .set(qTHospital.areaId,areaId)
                .set(qTHospital.hospitalCode,hospitalCode)
                .set(qTHospital.hospitalName,hospitalName)
                .set(qTHospital.updatedTime,new Date())
                .where(qTHospital.id.eq(id)).execute();
        if(lon > 0){
            return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "医院管理编辑成功", null);
        }else {
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "医院管理编辑失败", null);
        }
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
        list.add(qTHospital.status.equalsIgnoreCase(Constant.YES));
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
