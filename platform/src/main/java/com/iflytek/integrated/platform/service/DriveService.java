package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.common.Constant;
import com.iflytek.integrated.common.ResultDto;
import com.iflytek.integrated.common.TableData;
import com.iflytek.integrated.common.utils.ExceptionUtil;
import com.iflytek.integrated.common.utils.Utils;
import com.iflytek.integrated.platform.entity.TDrive;
import com.iflytek.integrated.platform.validator.ValidationResult;
import com.iflytek.integrated.platform.validator.ValidatorHelper;
import com.iflytek.medicalboot.core.dto.PageRequest;
import com.iflytek.medicalboot.core.id.BatchUidService;
import com.iflytek.medicalboot.core.querydsl.QuerydslService;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.StringPath;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.iflytek.integrated.platform.entity.QTDrive.qTDrive;

/**
 * 驱动管理
 * @author czzhan
 */
@Slf4j
@RestController
public class DriveService extends QuerydslService<TDrive, String, TDrive, StringPath, PageRequest<TDrive>> {
    public DriveService(){
        super(qTDrive,qTDrive.id);
    }

    private static final Logger logger = LoggerFactory.getLogger(DriveService.class);

    @Autowired
    private BatchUidService batchUidService;
    @Autowired
    private ValidatorHelper validatorHelper;

    @ApiOperation(value = "获取驱动下拉")
    @GetMapping("/{version}/pb/driveManage/getAllDrive")
    public ResultDto getAllDrive() {
        List<TDrive> drives = sqlQueryFactory.select(
                Projections.bean(
                        TDrive.class,
                        qTDrive.id,
                        qTDrive.driveCode,
                        qTDrive.driveName
                )
        ).from(qTDrive).orderBy(qTDrive.updatedTime.desc()).fetch();
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE,"",drives);
    }

    @ApiOperation(value = "驱动管理列表")
    @GetMapping("/{version}/pb/driveManage/getDriveList")
    public ResultDto getDriveList(String driveName,
                          @RequestParam(defaultValue = "1")Integer pageNo,
                          @RequestParam(defaultValue = "10")Integer pageSize) {
        try {
            //查询条件
            ArrayList<Predicate> list = new ArrayList<>();
            //判断条件是否为空
            if(!StringUtils.isEmpty(driveName)){
                list.add(qTDrive.driveName.like(Utils.createFuzzyText(driveName)));
            }
            //根据查询条件获取驱动列表
            QueryResults<TDrive> queryResults = sqlQueryFactory.select(
                    Projections.bean(
                            TDrive.class,
                            qTDrive.id,
                            qTDrive.driveCode,
                            qTDrive.driveName,
                            qTDrive.updatedTime
                    ))
                    .from(qTDrive)
                    .where(list.toArray(new Predicate[list.size()]))
                    .limit(pageSize)
                    .offset((pageNo - 1) * pageSize)
                    .orderBy(qTDrive.updatedTime.desc())
                    .fetchResults();
            //分页
            TableData<TDrive> tableData = new TableData<>(queryResults.getTotal(), queryResults.getResults());
            return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "", tableData);
        }catch (Exception e){
            logger.error("获取驱动管理列表失败!", ExceptionUtil.dealException(e));
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "", ExceptionUtil.dealException(e));
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "驱动管理删除")
    @DeleteMapping("/{version}/pb/driveManage/delDriveById")
    public ResultDto delDriveById(String id){
        if(StringUtils.isEmpty(id)){
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "", "id不能为空");
        }
        //查看驱动是否存在
        TDrive drive = sqlQueryFactory.select(qTDrive).from(qTDrive).where(qTDrive.id.eq(id)).fetchOne();
        if(drive == null || StringUtils.isEmpty(drive.getId())){
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "", "没有找到该驱动，删除失败");
        }
        //删除驱动
        Long lon = sqlQueryFactory.delete(qTDrive).where(qTDrive.id.eq(drive.getId())).execute();
        if(lon <= 0){
            throw new RuntimeException("驱动管理,驱动删除失败");
        }
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "", "驱动管理,驱动删除成功");
    }

    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "驱动新增/编辑")
    @PostMapping("/{version}/pb/driveManage/saveAndUpdateDrive")
    public ResultDto saveAndUpdateDrive(@RequestBody TDrive drive){
        //校验参数是否完整
        ValidationResult validationResult = validatorHelper.validate(drive);
        if (validationResult.isHasErrors()) {
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "", validationResult.getErrorMsg());
        }
        //校验是否存在重复驱动
        isExistence(drive.getId(),drive.getDriveName(),drive.getDriveCode());
        if(StringUtils.isEmpty(drive.getId())){
            //新增驱动
            Long lon = sqlQueryFactory.insert(qTDrive)
                    .set(qTDrive.id,batchUidService.getUid(qTDrive.getTableName())+"")
                    .set(qTDrive.driveName,drive.getDriveName())
                    .set(qTDrive.driveCode,drive.getDriveCode())
                    .set(qTDrive.driveInstruction,drive.getDriveInstruction())
                    .set(qTDrive.driveContent,drive.getDriveContent())
                    .set(qTDrive.createdBy,"")
                    .set(qTDrive.createdTime,new Date()).execute();
            if(lon <= 0){
                throw new RuntimeException("新增驱动失败");
            }
        }
        else{
            //编辑驱动
            Long lon = sqlQueryFactory.update(qTDrive)
                    .set(qTDrive.driveName,drive.getDriveName())
                    .set(qTDrive.driveCode,drive.getDriveCode())
                    .set(qTDrive.driveInstruction,drive.getDriveInstruction())
                    .set(qTDrive.driveContent,drive.getDriveContent())
                    .set(qTDrive.updatedBy,"")
                    .set(qTDrive.updatedTime,new Date())
                    .where(qTDrive.id.eq(drive.getId())).execute();
            if(lon <= 0){
                throw new RuntimeException("编辑驱动失败");
            }
        }
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE,"","驱动新增或编辑成功");
    }

    /**
     * 校验是否有重复驱动
     * @param id
     * @param driveName
     * @param driveCode
     */
    private void isExistence(String id, String driveName, String driveCode){
        //校验是否存在重复驱动
        ArrayList<Predicate> list = new ArrayList<>();
        list.add(qTDrive.driveName.eq(driveName)
                .or(qTDrive.driveCode.eq(driveCode)));
        if(!StringUtils.isEmpty(id)){
            list.add(qTDrive.id.notEqualsIgnoreCase(id));
        }
        List<String> plugins = sqlQueryFactory.select(qTDrive.id).from(qTDrive)
                .where(list.toArray(new Predicate[list.size()])).fetch();
        if(CollectionUtils.isNotEmpty(plugins)){
            throw new RuntimeException("驱动名称或编码已存在");
        }
    }


}
