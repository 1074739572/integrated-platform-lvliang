package com.iflytek.integrated.platform.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.common.intercept.UserLoginIntercept;
import com.iflytek.integrated.platform.dto.GroovyValidateDto;
import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.common.dto.TableData;
import com.iflytek.integrated.common.utils.ExceptionUtil;
import com.iflytek.integrated.platform.entity.TType;
import com.iflytek.integrated.platform.entity.TVendorDriveLink;
import com.iflytek.integrated.platform.utils.ToolsGenerate;
import com.iflytek.integrated.platform.utils.Utils;
import com.iflytek.integrated.platform.entity.TDrive;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static com.iflytek.integrated.platform.entity.QTDrive.qTDrive;
import static com.iflytek.integrated.platform.entity.QTType.qTType;
import static com.iflytek.integrated.platform.entity.QTVendorDriveLink.qTVendorDriveLink;

/**
 * 驱动管理
 * @author czzhan
 */
@Slf4j
@Api(tags = "驱动管理")
@RestController
@RequestMapping("/{version}/pt/driveManage")
public class DriveService extends BaseService<TDrive, String, StringPath> {
    public DriveService(){
        super(qTDrive,qTDrive.id);
    }

    private static final Logger logger = LoggerFactory.getLogger(DriveService.class);

    @Autowired
    private VendorDriveLinkService vendorDriveLinkService;
    @Autowired
    private BatchUidService batchUidService;
    @Autowired
    private ValidatorHelper validatorHelper;
    @Autowired
    private ToolsGenerate toolsGenerate;

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
        ).from(qTDrive).orderBy(qTDrive.createdTime.desc()).fetch();
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
    public ResultDto<TableData<TDrive>> getDriveList(@ApiParam(value = "驱动名称") @RequestParam(value = "driveName", required = false) String driveName,
                                  @ApiParam(value = "驱动分类id") @RequestParam(value = "typeId", required = false) String typeId,
                                  @RequestParam(defaultValue = "1")Integer pageNo,
                                  @RequestParam(defaultValue = "10")Integer pageSize) {
        try {
            //查询条件
            ArrayList<Predicate> list = new ArrayList<>();
            //判断条件是否为空
            if(StringUtils.isNotEmpty(driveName)) {
                list.add(qTDrive.driveName.like(Utils.createFuzzyText(driveName)));
            }
            if(StringUtils.isNotEmpty(typeId)) {
                list.add(qTDrive.typeId.eq(typeId));
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
                            qTDrive.createdTime,
                            qTDrive.typeId,
                            qTType.typeName.as("driveTypeName")
                    ))
                    .from(qTDrive)
                    .where(list.toArray(new Predicate[list.size()]))
                    .leftJoin(qTType).on(qTType.id.eq(qTDrive.typeId))
                    .limit(pageSize)
                    .offset((pageNo - 1) * pageSize)
                    .orderBy(qTDrive.createdTime.desc())
                    .fetchResults();
            //分页
            TableData<TDrive> tableData = new TableData<>(queryResults.getTotal(), queryResults.getResults());
            return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "", tableData);
        }catch (Exception e){
            logger.error("获取驱动管理列表失败! MSG:{}", ExceptionUtil.dealException(e));
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "", ExceptionUtil.dealException(e));
        }
    }

    @ApiOperation(value = "校验groovy脚本格式是否正确")
    @PostMapping("/groovyValidate")
    public ResultDto groovyValidate(String content){
        GroovyValidateDto result = toolsGenerate.groovyUrl(content);
        if(StringUtils.isNotBlank(result.getValidResult()) && StringUtils.isBlank(result.getError())){
            return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "", result);
        }
        else {
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "", result);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "驱动管理删除")
    @PostMapping("/delDriveById")
    public ResultDto delDriveById(@ApiParam(value = "驱动id") @RequestParam(value = "id", required = true) String id){
        //查看驱动是否存在
        TDrive drive = sqlQueryFactory.select(qTDrive).from(qTDrive).where(qTDrive.id.eq(id)).fetchFirst();
        if(drive == null || StringUtils.isEmpty(drive.getId())){
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "没有找到该驱动，删除失败!", "没有找到该驱动,删除失败!");
        }
        //校验该驱动是否有厂商关联
        List<TVendorDriveLink> tvdlList = vendorDriveLinkService.getVendorDriveLinkByDriveId(id);
        if (CollectionUtils.isNotEmpty(tvdlList)) {
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "该驱动已有厂商相关联,无法删除!", "该驱动已有厂商相关联,无法删除!");
        }
        //删除驱动
        Long lon = sqlQueryFactory.delete(qTDrive).where(qTDrive.id.eq(drive.getId())).execute();
        if(lon <= 0){
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "驱动删除失败!", "驱动删除失败!");
        }
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "驱动删除成功!", id);
    }

    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "驱动新增/编辑")
    @PostMapping("/saveAndUpdateDrive")
    public ResultDto saveAndUpdateDrive(@RequestBody TDrive drive){
        //校验参数是否完整
        ValidationResult validationResult = validatorHelper.validate(drive);
        if (validationResult.isHasErrors()) {
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "", validationResult.getErrorMsg());
        }
        //校验是否获取到登录用户
        String loginUserName = UserLoginIntercept.LOGIN_USER.getLoginUserName();
        if(StringUtils.isBlank(loginUserName)){
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!", "没有获取到登录用户!");
        }
        //校验是否存在重复驱动，驱动代码格式是否正确
        Map<String, Object> isExist = this.isExistence(drive.getId(), drive.getDriveName(), drive.getDriveContent());
        boolean bool = (boolean) isExist.get("isExist");
        if (bool) {
            return new ResultDto(Constant.ResultCode.ERROR_CODE, isExist.get("message")+"", isExist.get("message"));
        }
        if(StringUtils.isEmpty(drive.getId())){
            //新增驱动
            drive.setId(batchUidService.getUid(qTDrive.getTableName())+"");
            drive.setCreatedTime(new Date());
            drive.setCreatedBy(loginUserName);
            this.post(drive);
            return new ResultDto(Constant.ResultCode.SUCCESS_CODE,"驱动新增成功", null);
        }
        //编辑驱动
        drive.setUpdatedBy(loginUserName);
        drive.setUpdatedTime(new Date());
        Long lon = this.put(drive.getId(), drive);
        if(lon <= 0){
            return new ResultDto(Constant.ResultCode.ERROR_CODE,"驱动编辑失败!", drive);
        }
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE,"驱动编辑成功!", drive.getId());
    }

    @ApiOperation(value = "新增厂商弹窗展示的驱动选择信息")
    @GetMapping("/getDriveChoiceList")
    public ResultDto getDriveChoiceList() {
        //获取驱动类型list
        List<TType> typeList = sqlQueryFactory.select(qTType).from(qTType).where(qTType.type.eq(Constant.TypeStatus.DRIVE)).orderBy(qTType.createdTime.desc()).fetch();

        JSONArray rtnArr = new JSONArray();
        for (TType tt : typeList) {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("id", tt.getId());
            jsonObj.put("name", tt.getTypeName());
            List<TDrive> driveList = sqlQueryFactory.select(qTDrive).from(qTDrive).where(qTDrive.typeId.eq(tt.getId())).orderBy(qTDrive.createdTime.desc()).fetch();
            JSONArray arr = new JSONArray();
            for (TDrive td : driveList) {
                JSONObject obj = new JSONObject();
                obj.put("id", td.getId());
                obj.put("name", td.getDriveName());
                arr.add(obj);
            }
            jsonObj.put("children", arr);
            rtnArr.add(jsonObj);
        }
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE,"获取驱动选择信息成功", rtnArr);
    }

    /**
     * 校验是否有重复驱动，代码格式是否正确
     * @param id
     * @param driveName
     * @param driveContent
     */
    private Map<String, Object> isExistence(String id, String driveName, String driveContent){
        Map<String, Object> rtnMap = new HashMap<>();
        //默认false
        rtnMap.put("isExist", false);

        //校验是否存在重复驱动
        ArrayList<Predicate> list = new ArrayList<>();
        list.add(qTDrive.driveName.eq(driveName));
        if(StringUtils.isNotEmpty(id)){
            list.add(qTDrive.id.notEqualsIgnoreCase(id));
        }
        List<String> plugins = sqlQueryFactory.select(qTDrive.id).from(qTDrive)
                .where(list.toArray(new Predicate[list.size()])).fetch();
        if(CollectionUtils.isNotEmpty(plugins)){
            rtnMap.put("isExist", true);
            rtnMap.put("message", "驱动名称已存在!");
        }
        GroovyValidateDto result = toolsGenerate.groovyUrl(driveContent);
        if(StringUtils.isNotBlank(result.getError()) || StringUtils.isBlank(result.getValidResult())){
            rtnMap.put("isExist", true);
            rtnMap.put("message", "驱动内容格式错误!");
        }
        return rtnMap;
    }


}
