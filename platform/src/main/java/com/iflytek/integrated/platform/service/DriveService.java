package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.common.dto.TableData;
import com.iflytek.integrated.common.utils.ExceptionUtil;
import com.iflytek.integrated.common.validator.ValidationResult;
import com.iflytek.integrated.common.validator.ValidatorHelper;
import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.common.CacheDeleteService;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.platform.common.RedisService;
import com.iflytek.integrated.platform.dto.CacheDeleteDto;
import com.iflytek.integrated.platform.dto.DriveDto;
import com.iflytek.integrated.platform.dto.GroovyValidateDto;
import com.iflytek.integrated.platform.entity.TBusinessInterface;
import com.iflytek.integrated.platform.entity.TDrive;
import com.iflytek.integrated.platform.entity.TSysDriveLink;
import com.iflytek.integrated.platform.entity.TSysPublish;
import com.iflytek.integrated.platform.entity.TType;
import com.iflytek.integrated.platform.utils.NiFiRequestUtil;
import com.iflytek.integrated.platform.utils.PlatformUtil;
import com.iflytek.medicalboot.core.id.BatchUidService;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.iflytek.integrated.platform.entity.QTDrive.qTDrive;
import static com.iflytek.integrated.platform.entity.QTSysDriveLink.qTSysDriveLink;
import static com.iflytek.integrated.platform.entity.QTType.qTType;

/**
 * 驱动管理
 *
 * @author czzhan
 */
@Slf4j
@Api(tags = "驱动管理")
@RestController
@RequestMapping("/{version}/pt/driveManage")
public class DriveService extends BaseService<TDrive, String, StringPath> {
    public DriveService() {
        super(qTDrive, qTDrive.id);
    }

    private static final Logger logger = LoggerFactory.getLogger(DriveService.class);

    @Autowired
    private SysDriveLinkService sysDriveLinkService;

    @Autowired
    private BatchUidService batchUidService;
    @Autowired
    private ValidatorHelper validatorHelper;
    @Autowired
    private NiFiRequestUtil niFiRequestUtil;

    @Autowired
    private HistoryService historyService;
    @Autowired
    private TypeService typeService;
    @Autowired
    private BusinessInterfaceService businessInterfaceService;
    @Autowired
    private SysPublishService sysPublishService;
    @Autowired
    private CacheDeleteService cacheDeleteService;

    @ApiOperation(value = "获取驱动下拉")
    @GetMapping("/getAllDrive")
    public ResultDto<List<TDrive>> getAllDrive() {
        List<TDrive> drives = sqlQueryFactory
                .select(Projections.bean(TDrive.class, qTDrive.id, qTDrive.driveCode, qTDrive.driveName)).from(qTDrive)
                .orderBy(qTDrive.createdTime.desc()).fetch();
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "驱动下拉数据获取成功!", drives);
    }

    @ApiOperation(value = "根据系统ID获取驱动")
    @GetMapping("/getAllDriveBySysId")
    public ResultDto<List<TDrive>> getAllDriveByVendorId(
            @ApiParam(value = "系统id") @RequestParam(value = "sysId", required = true) String sysId) {
        List<TDrive> list = sqlQueryFactory.select(qTDrive).from(qTSysDriveLink).leftJoin(qTDrive)
                .on(qTDrive.id.eq(qTSysDriveLink.driveId)).where(qTSysDriveLink.sysId.eq(sysId)).fetch();
        List<TDrive> drives = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(list)) {
            for (TDrive td : list) {
                if (StringUtils.isNotBlank(td.getId())) {
                    drives.add(td);
                }
            }
        }
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "根据系统获取驱动成功", drives);
    }

    @ApiOperation(value = "驱动管理列表")
    @GetMapping("/getDriveList")
    public ResultDto<TableData<TDrive>> getDriveList(
            @ApiParam(value = "驱动名称") @RequestParam(value = "driveName", required = false) String driveName,
            @ApiParam(value = "驱动分类id") @RequestParam(value = "typeId", required = false) String typeId,
            @RequestParam(defaultValue = "1") Integer pageNo, @RequestParam(defaultValue = "10") Integer pageSize, @RequestParam(value = "id", required = false) String id) {
        try {
            // 查询条件
            ArrayList<Predicate> list = new ArrayList<>();
            // 判断条件是否为空
            if (StringUtils.isNotEmpty(driveName)) {
                list.add(qTDrive.driveName.like(PlatformUtil.createFuzzyText(driveName)));
            }
            if (StringUtils.isNotEmpty(typeId)) {
                list.add(qTDrive.typeId.eq(typeId));
            }
            if (StringUtils.isNotEmpty(id)) {
                list.add(qTDrive.id.eq(id));
            }
            // 根据查询条件获取驱动列表
            QueryResults<TDrive> queryResults = sqlQueryFactory
                    .select(Projections.bean(TDrive.class, qTDrive.id, qTDrive.driveCode, qTDrive.driveName,
                            qTDrive.driveContent, qTDrive.driveInstruction, qTDrive.createdTime, qTDrive.typeId,
                            qTDrive.dependentPath, qTDrive.driveCallType,
                            new CaseBuilder().when(qTDrive.driveCallType.eq("1")).then("请求方").otherwise("被请求方")
                                    .as("driveCallTypeName"),
                            qTType.typeName.as("driveTypeName")))
                    .from(qTDrive).where(list.toArray(new Predicate[list.size()])).leftJoin(qTType)
                    .on(qTType.id.eq(qTDrive.typeId)).limit(pageSize).offset((pageNo - 1) * pageSize)
                    .orderBy(qTDrive.createdTime.desc()).fetchResults();
            // 分页
            TableData<TDrive> tableData = new TableData<>(queryResults.getTotal(), queryResults.getResults());
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "驱动管理列表获取成功!", tableData);
        } catch (Exception e) {
            logger.error("获取驱动管理列表失败! MSG:{}", ExceptionUtil.dealException(e));
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "驱动管理列表获取失败!");
        }
    }

    @ApiOperation(value = "校验groovy脚本格式是否正确")
    @PostMapping("/groovyValidate")
    public ResultDto<GroovyValidateDto> groovyValidate(String content) {
        GroovyValidateDto result = niFiRequestUtil.groovyUrl(content);
        if (StringUtils.isNotBlank(result.getValidResult()) && StringUtils.isBlank(result.getError())) {
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "", result);
        } else {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "驱动删除")
    @PostMapping("/delDriveById")
    public ResultDto<String> delDriveById(
            @ApiParam(value = "驱动id") @RequestParam(value = "id", required = true) String id) {
        // 查看驱动是否存在
        TDrive drive = sqlQueryFactory.select(qTDrive).from(qTDrive).where(qTDrive.id.eq(id)).fetchFirst();
        if (drive == null || StringUtils.isEmpty(drive.getId())) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有找到该驱动，删除失败!", "没有找到该驱动,删除失败!");
        }
        // 校验该驱动是否有厂商关联
        List<TSysDriveLink> tvdlList = sysDriveLinkService.getSysDriveLinkByDriveId(id);
        if (CollectionUtils.isNotEmpty(tvdlList)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "该驱动已有系统相关联,无法删除!", "该驱动已有系统相关联,无法删除!");
        }
        // 缓存删除
        cacheDelete(drive.getId());

        // 删除驱动
        Long lon = sqlQueryFactory.delete(qTDrive).where(qTDrive.id.eq(drive.getId())).execute();
        if (lon <= 0) {
            throw new RuntimeException("驱动删除失败!");
        }
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "驱动删除成功!", null);
    }

    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "驱动新增/编辑")
    @PostMapping("/saveAndUpdateDrive")
    public ResultDto<String> saveAndUpdateDrive(@RequestBody TDrive drive, @RequestParam("loginUserName") String loginUserName) {
        // 校验参数是否完整
        ValidationResult validationResult = validatorHelper.validate(drive);
        if (validationResult.isHasErrors()) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "", validationResult.getErrorMsg());
        }
        // 校验是否获取到登录用户
        if (StringUtils.isBlank(loginUserName)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!", "没有获取到登录用户!");
        }
        // 校验是否存在重复驱动，驱动代码格式是否正确
        Map<String, Object> isExist = this.isExistence(drive.getId(), drive.getDriveName(), drive.getDriveContent());
        boolean bool = (boolean) isExist.get("isExist");
        if (bool) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, isExist.get("message") + "");
        }
        if (StringUtils.isEmpty(drive.getId())) {
            // 新增驱动
            drive.setId(batchUidService.getUid(qTDrive.getTableName()) + "");
            drive.setDriveCode(generateCode(qTDrive.driveCode, qTDrive, drive.getDriveName()));
            drive.setCreatedTime(new Date());
            drive.setCreatedBy(loginUserName);
            this.post(drive);
            TType tType = typeService.getOne(drive.getTypeId());
            drive.setDriveTypeName(tType.getTypeName());
            if ("1".equals(drive.getDriveCallType())) {
                drive.setDriveCallTypeName("请求方");
            }
            if ("2".equals(drive.getDriveCallType())) {
                drive.setDriveCallTypeName("被请求方");
            }
            historyService.insertHis(drive, 2, loginUserName, null, drive.getId(), null);
            //删除缓存
            cacheDelete(drive.getId());
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "驱动新增成功", null);
        }

        //删除缓存
        cacheDelete(drive.getId());

        //插入历史
        TDrive tDrive = this.getOne(drive.getId());
        TType tType = typeService.getOne(tDrive.getTypeId());
        tDrive.setDriveTypeName(tType.getTypeName());
        if ("1".equals(tDrive.getDriveCallType())) {
            tDrive.setDriveCallTypeName("请求方");
        }
        if ("2".equals(tDrive.getDriveCallType())) {
            tDrive.setDriveCallTypeName("被请求方");
        }
        historyService.insertHis(tDrive, 2, loginUserName, drive.getId(), drive.getId(), null);
        // 编辑驱动
        drive.setUpdatedBy(loginUserName);
        drive.setUpdatedTime(new Date());
        Long lon = this.put(drive.getId(), drive);
        if (lon <= 0) {
            throw new RuntimeException("驱动编辑失败!");
        }
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "驱动编辑成功!", null);
    }

    public void cacheDelete(String driveId) {
        //根据驱动id查询系统
        List<TSysDriveLink> beans = sysDriveLinkService.getSysDriveLinkByDriveId(driveId);
        if (CollectionUtils.isEmpty(beans)) {
            return;
        }

        List<String> sysIdList = new ArrayList<>();
        List<String> funIdList = new ArrayList<>();
        //驱动还应该关注集成配置的被请求方
        //根据被请求方系统查询集成配置 如存在 去获取集成配置的服务  并且将服务发布系统全部加入
        sysIdList = beans.stream().filter(e -> StringUtils.isNotEmpty(e.getSysId()))
                .map(TSysDriveLink::getSysId)
                .collect(Collectors.toList());

        //根据系统找到注册方  由注册放找到集成配置
        List<TBusinessInterface> businessInterfaces = businessInterfaceService.getListByRegSysIdList(sysIdList);
        if (CollectionUtils.isNotEmpty(businessInterfaces)) {
            //将服务id加入
            funIdList = businessInterfaces.stream().map(TBusinessInterface::getRequestInterfaceId).collect(Collectors.toList());
            //将服务发布的系统全部加入
            List<TSysPublish> all = sysPublishService.getAll();
            if (CollectionUtils.isNotEmpty(all)) {
                sysIdList.addAll(all.stream().map(TSysPublish::getSysId).collect(Collectors.toList()));
            }
        }

        CacheDeleteDto sysKeyDto = new CacheDeleteDto();
        sysKeyDto.setSysIds(sysIdList);
        sysKeyDto.setInterfaceIds(funIdList);

        //需要生成两种类型的key
        sysKeyDto.setCacheTypeList(Arrays.asList(
                Constant.CACHE_KEY_PREFIX.DRIVERS_TYPE,
                Constant.CACHE_KEY_PREFIX.COMMON_TYPE,
                Constant.CACHE_KEY_PREFIX.MQ_TYPE));

        //获得到系统编码的缓存键集合
        cacheDeleteService.cacheKeyDelete(sysKeyDto);
    }

    @ApiOperation(value = "新增厂商弹窗展示的驱动选择信息")
    @GetMapping("/getDriveChoiceList")
    public ResultDto<List<DriveDto>> getDriveChoiceList() {
        // 获取驱动类型list
        List<TType> typeList = sqlQueryFactory.select(qTType).from(qTType)
                .where(qTType.type.eq(Constant.TypeStatus.DRIVE)).orderBy(qTType.createdTime.desc()).fetch();

        List<DriveDto> rtnArr = new ArrayList<>();
        if (typeList != null && typeList.size() > 0) {
            for (TType tt : typeList) {
                DriveDto jsonObj = new DriveDto();
                jsonObj.setId(tt.getId());
                jsonObj.setName(tt.getTypeName());
                List<TDrive> driveList = sqlQueryFactory.select(qTDrive).from(qTDrive).where(qTDrive.typeId.eq(tt.getId()))
                        .orderBy(qTDrive.createdTime.desc()).fetch();

                List<TDrive> arr = new ArrayList<>();
                for (TDrive td : driveList) {
                    td.setId(td.getId());
                    td.setName(td.getDriveName());
                    arr.add(td);
                }

                jsonObj.setChildren(arr);
                rtnArr.add(jsonObj);
            }
        }

        if (rtnArr != null && rtnArr.size() > 0) {
            Iterator iterator = rtnArr.iterator();
            while (iterator.hasNext()) {
                DriveDto jsonObj = (DriveDto) iterator.next();
                if (jsonObj != null) {
                    //若当前分类下driveList为空，则删除当前分类
                    if (jsonObj.getChildren() == null || jsonObj.getChildren().size() == 0) {
                        iterator.remove();
                    }
                }
            }
        }

        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取驱动选择信息成功", rtnArr);
    }

    /**
     * 校验是否有重复驱动，代码格式是否正确
     *
     * @param id
     * @param driveName
     * @param driveContent
     */
    private Map<String, Object> isExistence(String id, String driveName, String driveContent) {
        Map<String, Object> rtnMap = new HashMap<>();
        // 默认false
        rtnMap.put("isExist", false);

        // 校验是否存在重复驱动
        ArrayList<Predicate> list = new ArrayList<>();
        list.add(qTDrive.driveName.eq(driveName));
        if (StringUtils.isNotEmpty(id)) {
            list.add(qTDrive.id.notEqualsIgnoreCase(id));
        }
        List<String> plugins = sqlQueryFactory.select(qTDrive.id).from(qTDrive)
                .where(list.toArray(new Predicate[list.size()])).fetch();
        if (CollectionUtils.isNotEmpty(plugins)) {
            rtnMap.put("isExist", true);
            rtnMap.put("message", "驱动名称已存在!");
        }
        GroovyValidateDto result = niFiRequestUtil.groovyUrl(driveContent);
        if (StringUtils.isNotBlank(result.getError()) || StringUtils.isBlank(result.getValidResult())) {
            rtnMap.put("isExist", true);
            rtnMap.put("message", "驱动内容格式错误!");
        }
        return rtnMap;
    }

}
