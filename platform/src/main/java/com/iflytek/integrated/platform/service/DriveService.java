package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.common.Constant;
import com.iflytek.integrated.common.utils.RedisUtil;
import com.iflytek.integrated.platform.annotation.AvoidRepeatCommit;
import com.iflytek.integrated.platform.dto.GroovyValidateDto;
import com.iflytek.integrated.common.ResultDto;
import com.iflytek.integrated.common.TableData;
import com.iflytek.integrated.common.utils.ExceptionUtil;
import com.iflytek.integrated.platform.utils.ToolsGenerate;
import com.iflytek.integrated.platform.utils.Utils;
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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.iflytek.integrated.platform.entity.QTDrive.qTDrive;
import static com.iflytek.integrated.platform.entity.QTVendorDriveLink.qTVendorDriveLink;

/**
 * 驱动管理
 * @author czzhan
 */
@Slf4j
@Api(tags = "驱动管理")
@CrossOrigin
@RestController
@RequestMapping("/v1/pb/driveManage")
public class DriveService extends QuerydslService<TDrive, String, TDrive, StringPath, PageRequest<TDrive>> {
    public DriveService(){
        super(qTDrive,qTDrive.id);
    }

    private static final Logger logger = LoggerFactory.getLogger(DriveService.class);

    @Autowired
    private BatchUidService batchUidService;
    @Autowired
    private ValidatorHelper validatorHelper;
    @Autowired
    private ToolsGenerate toolsGenerate;
    @Resource
    private RedisUtil redisUtil;

    @ApiOperation(value = "获取驱动下拉")
    @GetMapping("/getAllDrive")
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

    @ApiOperation(value = "根据厂商获取驱动")
    @GetMapping("/getAllDriveByVendorId")
    public ResultDto getAllDriveByVendorId(@ApiParam(value = "厂商id") @RequestParam(value = "vendorId", required = true) String vendorId) {
        List<TDrive> list = sqlQueryFactory.select(qTDrive).from(qTVendorDriveLink)
                .leftJoin(qTDrive).on(qTDrive.id.eq(qTVendorDriveLink.driveId))
                .where(qTVendorDriveLink.vendorId.eq(vendorId)).fetch();
        List<TDrive> drives = new ArrayList<>();
        for (TDrive td : list) {
            if (StringUtils.isNotBlank(td.getId())) {
                drives.add(td);
            }
        }
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE,"根据厂商获取驱动成功", drives);
    }

    @ApiOperation(value = "驱动管理列表")
    @GetMapping("/getDriveList")
    public ResultDto getDriveList(String driveName,
                          @RequestParam(defaultValue = "1")Integer pageNo,
                          @RequestParam(defaultValue = "10")Integer pageSize) {
        try {
            //查询条件
            ArrayList<Predicate> list = new ArrayList<>();
            //判断条件是否为空
            if(StringUtils.isNotEmpty(driveName)){
                list.add(qTDrive.driveName.like(Utils.createFuzzyText(driveName)));
            }
            //根据查询条件获取驱动列表
            QueryResults<TDrive> queryResults = sqlQueryFactory.select(
                    Projections.bean(
                            TDrive.class,
                            qTDrive.id,
                            qTDrive.driveCode,
                            qTDrive.driveName,
                            qTDrive.driveContent,
                            qTDrive.driveInstruction,
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

    @ApiOperation(value = "校验groovy脚本格式是否正确")
    @PostMapping("/groovyValidate")
    public ResultDto groovyValidate(String content){
        GroovyValidateDto result = toolsGenerate.groovyUrl(content);
        if(GroovyValidateDto.RESULT.SUCCESS.getType().equals(result.getValidResult())){
            return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "", result);
        }
        else {
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "", result);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "驱动管理删除")
    @PostMapping("/delDriveById")
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
        delRedis(id);
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "", "驱动管理,驱动删除成功");
    }

    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "驱动新增/编辑")
    @PostMapping("/saveAndUpdateDrive")
    @AvoidRepeatCommit
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
            drive.setId(batchUidService.getUid(qTDrive.getTableName())+"");
            drive.setCreatedTime(new Date());
            this.post(drive);
            setRedis(drive);
            return new ResultDto(Constant.ResultCode.SUCCESS_CODE,"驱动新增成功", drive);
        }
        //编辑驱动
        drive.setUpdatedTime(new Date());
        Long lon = this.put(drive.getId(), drive);
        if(lon <= 0){
            throw new RuntimeException("驱动编辑失败");
        }
        setRedis(drive);
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE,"驱动编辑成功", drive);
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
        if(StringUtils.isNotEmpty(id)){
            list.add(qTDrive.id.notEqualsIgnoreCase(id));
        }
        List<String> plugins = sqlQueryFactory.select(qTDrive.id).from(qTDrive)
                .where(list.toArray(new Predicate[list.size()])).fetch();
        if(CollectionUtils.isNotEmpty(plugins)){
            throw new RuntimeException("驱动名称或编码已存在");
        }
    }

    /**
     * 更新redis记录
     * @param drive
     */
    private void setRedis(TDrive drive){
        Boolean flag = redisUtil.hmSet(qTDrive.getTableName(),drive.getId(),drive);
        if(!flag){
            throw new RuntimeException("redis新增或更新驱动失败");
        }
    }

    /**
     * 删除redis记录
     * @param id
     */
    private void delRedis(String id){
        Boolean flag = redisUtil.hmDel(qTDrive.getTableName(),id);
        if(!flag){
            throw new RuntimeException("redis删除驱动失败");
        }
    }
}
