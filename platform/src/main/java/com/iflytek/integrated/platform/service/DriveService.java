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
 * ????????????
 *
 * @author czzhan
 */
@Slf4j
@Api(tags = "????????????")
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

    @ApiOperation(value = "??????????????????")
    @GetMapping("/getAllDrive")
    public ResultDto<List<TDrive>> getAllDrive() {
        List<TDrive> drives = sqlQueryFactory
                .select(Projections.bean(TDrive.class, qTDrive.id, qTDrive.driveCode, qTDrive.driveName)).from(qTDrive)
                .orderBy(qTDrive.createdTime.desc()).fetch();
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "??????????????????????????????!", drives);
    }

    @ApiOperation(value = "????????????ID????????????")
    @GetMapping("/getAllDriveBySysId")
    public ResultDto<List<TDrive>> getAllDriveByVendorId(
            @ApiParam(value = "??????id") @RequestParam(value = "sysId", required = true) String sysId) {
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
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "??????????????????????????????", drives);
    }

    @ApiOperation(value = "??????????????????")
    @GetMapping("/getDriveList")
    public ResultDto<TableData<TDrive>> getDriveList(
            @ApiParam(value = "????????????") @RequestParam(value = "driveName", required = false) String driveName,
            @ApiParam(value = "????????????id") @RequestParam(value = "typeId", required = false) String typeId,
            @RequestParam(defaultValue = "1") Integer pageNo, @RequestParam(defaultValue = "10") Integer pageSize, @RequestParam(value = "id", required = false) String id) {
        try {
            // ????????????
            ArrayList<Predicate> list = new ArrayList<>();
            // ????????????????????????
            if (StringUtils.isNotEmpty(driveName)) {
                list.add(qTDrive.driveName.like(PlatformUtil.createFuzzyText(driveName)));
            }
            if (StringUtils.isNotEmpty(typeId)) {
                list.add(qTDrive.typeId.eq(typeId));
            }
            if (StringUtils.isNotEmpty(id)) {
                list.add(qTDrive.id.eq(id));
            }
            // ????????????????????????????????????
            QueryResults<TDrive> queryResults = sqlQueryFactory
                    .select(Projections.bean(TDrive.class, qTDrive.id, qTDrive.driveCode, qTDrive.driveName,
                            qTDrive.driveContent, qTDrive.driveInstruction, qTDrive.createdTime, qTDrive.typeId,
                            qTDrive.dependentPath, qTDrive.driveCallType,
                            new CaseBuilder().when(qTDrive.driveCallType.eq("1")).then("?????????").otherwise("????????????")
                                    .as("driveCallTypeName"),
                            qTType.typeName.as("driveTypeName")))
                    .from(qTDrive).where(list.toArray(new Predicate[list.size()])).leftJoin(qTType)
                    .on(qTType.id.eq(qTDrive.typeId)).limit(pageSize).offset((pageNo - 1) * pageSize)
                    .orderBy(qTDrive.createdTime.desc()).fetchResults();
            // ??????
            TableData<TDrive> tableData = new TableData<>(queryResults.getTotal(), queryResults.getResults());
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "??????????????????????????????!", tableData);
        } catch (Exception e) {
            logger.error("??????????????????????????????! MSG:{}", ExceptionUtil.dealException(e));
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "??????????????????????????????!");
        }
    }

    @ApiOperation(value = "??????groovy????????????????????????")
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
    @ApiOperation(value = "????????????")
    @PostMapping("/delDriveById")
    public ResultDto<String> delDriveById(
            @ApiParam(value = "??????id") @RequestParam(value = "id", required = true) String id) {
        // ????????????????????????
        TDrive drive = sqlQueryFactory.select(qTDrive).from(qTDrive).where(qTDrive.id.eq(id)).fetchFirst();
        if (drive == null || StringUtils.isEmpty(drive.getId())) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "????????????????????????????????????!", "?????????????????????,????????????!");
        }
        // ????????????????????????????????????
        List<TSysDriveLink> tvdlList = sysDriveLinkService.getSysDriveLinkByDriveId(id);
        if (CollectionUtils.isNotEmpty(tvdlList)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "??????????????????????????????,????????????!", "??????????????????????????????,????????????!");
        }
        // ????????????
        cacheDelete(drive.getId());

        // ????????????
        Long lon = sqlQueryFactory.delete(qTDrive).where(qTDrive.id.eq(drive.getId())).execute();
        if (lon <= 0) {
            throw new RuntimeException("??????????????????!");
        }
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "??????????????????!", null);
    }

    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "????????????/??????")
    @PostMapping("/saveAndUpdateDrive")
    public ResultDto<String> saveAndUpdateDrive(@RequestBody TDrive drive, @RequestParam("loginUserName") String loginUserName) {
        // ????????????????????????
        ValidationResult validationResult = validatorHelper.validate(drive);
        if (validationResult.isHasErrors()) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "", validationResult.getErrorMsg());
        }
        // ?????????????????????????????????
        if (StringUtils.isBlank(loginUserName)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "???????????????????????????!", "???????????????????????????!");
        }
        // ???????????????????????????????????????????????????????????????
        Map<String, Object> isExist = this.isExistence(drive.getId(), drive.getDriveName(), drive.getDriveContent());
        boolean bool = (boolean) isExist.get("isExist");
        if (bool) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, isExist.get("message") + "");
        }
        if (StringUtils.isEmpty(drive.getId())) {
            // ????????????
            drive.setId(batchUidService.getUid(qTDrive.getTableName()) + "");
            drive.setDriveCode(generateCode(qTDrive.driveCode, qTDrive, drive.getDriveName()));
            drive.setCreatedTime(new Date());
            drive.setCreatedBy(loginUserName);
            this.post(drive);
            TType tType = typeService.getOne(drive.getTypeId());
            drive.setDriveTypeName(tType.getTypeName());
            if ("1".equals(drive.getDriveCallType())) {
                drive.setDriveCallTypeName("?????????");
            }
            if ("2".equals(drive.getDriveCallType())) {
                drive.setDriveCallTypeName("????????????");
            }
            historyService.insertHis(drive, 2, loginUserName, null, drive.getId(), null);
            //????????????
            cacheDelete(drive.getId());
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "??????????????????", null);
        }

        //????????????
        cacheDelete(drive.getId());

        //????????????
        TDrive tDrive = this.getOne(drive.getId());
        TType tType = typeService.getOne(tDrive.getTypeId());
        tDrive.setDriveTypeName(tType.getTypeName());
        if ("1".equals(tDrive.getDriveCallType())) {
            tDrive.setDriveCallTypeName("?????????");
        }
        if ("2".equals(tDrive.getDriveCallType())) {
            tDrive.setDriveCallTypeName("????????????");
        }
        historyService.insertHis(tDrive, 2, loginUserName, drive.getId(), drive.getId(), null);
        // ????????????
        drive.setUpdatedBy(loginUserName);
        drive.setUpdatedTime(new Date());
        Long lon = this.put(drive.getId(), drive);
        if (lon <= 0) {
            throw new RuntimeException("??????????????????!");
        }
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "??????????????????!", null);
    }

    public void cacheDelete(String driveId) {
        //????????????id????????????
        List<TSysDriveLink> beans = sysDriveLinkService.getSysDriveLinkByDriveId(driveId);
        if (CollectionUtils.isEmpty(beans)) {
            return;
        }

        List<String> sysIdList = new ArrayList<>();
        List<String> funIdList = new ArrayList<>();
        //????????????????????????????????????????????????
        //?????????????????????????????????????????? ????????? ??????????????????????????????  ???????????????????????????????????????
        sysIdList = beans.stream().filter(e -> StringUtils.isNotEmpty(e.getSysId()))
                .map(TSysDriveLink::getSysId)
                .collect(Collectors.toList());

        //???????????????????????????  ??????????????????????????????
        List<TBusinessInterface> businessInterfaces = businessInterfaceService.getListByRegSysIdList(sysIdList);
        if (CollectionUtils.isNotEmpty(businessInterfaces)) {
            //?????????id??????
            funIdList = businessInterfaces.stream().map(TBusinessInterface::getRequestInterfaceId).collect(Collectors.toList());
            //????????????????????????????????????
            List<TSysPublish> all = sysPublishService.getAll();
            if (CollectionUtils.isNotEmpty(all)) {
                sysIdList.addAll(all.stream().map(TSysPublish::getSysId).collect(Collectors.toList()));
            }
        }

        CacheDeleteDto sysKeyDto = new CacheDeleteDto();
        sysKeyDto.setSysIds(sysIdList);
        sysKeyDto.setInterfaceIds(funIdList);

        //???????????????????????????key
        sysKeyDto.setCacheTypeList(Arrays.asList(
                Constant.CACHE_KEY_PREFIX.DRIVERS_TYPE,
                Constant.CACHE_KEY_PREFIX.COMMON_TYPE,
                Constant.CACHE_KEY_PREFIX.MQ_TYPE));

        //???????????????????????????????????????
        cacheDeleteService.cacheKeyDelete(sysKeyDto);
    }

    @ApiOperation(value = "?????????????????????????????????????????????")
    @GetMapping("/getDriveChoiceList")
    public ResultDto<List<DriveDto>> getDriveChoiceList() {
        // ??????????????????list
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
                    //??????????????????driveList??????????????????????????????
                    if (jsonObj.getChildren() == null || jsonObj.getChildren().size() == 0) {
                        iterator.remove();
                    }
                }
            }
        }

        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "??????????????????????????????", rtnArr);
    }

    /**
     * ??????????????????????????????????????????????????????
     *
     * @param id
     * @param driveName
     * @param driveContent
     */
    private Map<String, Object> isExistence(String id, String driveName, String driveContent) {
        Map<String, Object> rtnMap = new HashMap<>();
        // ??????false
        rtnMap.put("isExist", false);

        // ??????????????????????????????
        ArrayList<Predicate> list = new ArrayList<>();
        list.add(qTDrive.driveName.eq(driveName));
        if (StringUtils.isNotEmpty(id)) {
            list.add(qTDrive.id.notEqualsIgnoreCase(id));
        }
        List<String> plugins = sqlQueryFactory.select(qTDrive.id).from(qTDrive)
                .where(list.toArray(new Predicate[list.size()])).fetch();
        if (CollectionUtils.isNotEmpty(plugins)) {
            rtnMap.put("isExist", true);
            rtnMap.put("message", "?????????????????????!");
        }
        GroovyValidateDto result = niFiRequestUtil.groovyUrl(driveContent);
        if (StringUtils.isNotBlank(result.getError()) || StringUtils.isBlank(result.getValidResult())) {
            rtnMap.put("isExist", true);
            rtnMap.put("message", "????????????????????????!");
        }
        return rtnMap;
    }

}
