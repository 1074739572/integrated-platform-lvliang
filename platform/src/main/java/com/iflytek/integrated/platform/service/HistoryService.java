package com.iflytek.integrated.platform.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.common.dto.TableData;
import com.iflytek.integrated.common.utils.ExceptionUtil;
import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.platform.dto.HisRollbackBIDto;
import com.iflytek.integrated.platform.dto.HisRollbackDto;
import com.iflytek.integrated.platform.dto.InterfaceDto;
import com.iflytek.integrated.platform.entity.TBusinessInterface;
import com.iflytek.integrated.platform.entity.TDrive;
import com.iflytek.integrated.platform.entity.THistory;
import com.iflytek.integrated.platform.entity.TInterface;
import com.iflytek.integrated.platform.entity.TPlugin;
import com.iflytek.integrated.platform.entity.TSys;
import com.iflytek.integrated.platform.entity.TSysRegistry;
import com.iflytek.integrated.platform.entity.TType;
import com.iflytek.integrated.platform.utils.NiFiRequestUtil;
import com.iflytek.medicalboot.core.id.BatchUidService;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.StringPath;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.iflytek.integrated.platform.entity.QTBusinessInterface.qTBusinessInterface;
import static com.iflytek.integrated.platform.entity.QTHistory.qtHistory;

@Slf4j
@Api(tags = "??????????????????")
@RestController
@RequestMapping("/{version}/pt/history")
public class HistoryService extends BaseService<THistory, String, StringPath> {

    private static final Logger logger = LoggerFactory.getLogger(InterfaceService.class);

    public HistoryService() {
        super(qtHistory, qtHistory.pkId);
    }

    @Autowired
    private DriveService driveService;
    @Autowired
    private PluginService pluginService;
    @Autowired
    private BusinessInterfaceService businessInterfaceService;
    @Autowired
    private BatchUidService batchUidService;
    @Autowired
    private NiFiRequestUtil niFiRequestUtil;
    @Autowired
    private InterfaceService interfaceService;
    @Autowired
    private SysService sysService;
    @Autowired
    private SysRegistryService sysRegistryService;
    @Autowired
    private TypeService typeService;

    @ApiOperation(value = "????????????", notes = "????????????????????????")
    @GetMapping("/getHisList")
    public ResultDto getHisList(
            @ApiParam(value = "????????????/??????/?????????id") @RequestParam(value = "recordId", required = true) String recordId,
            @ApiParam(value = "?????????????????????1???????????? 2?????? 3?????? 4???????????????") @RequestParam(value = "hisType", required = true) Integer hisType,
            @ApiParam(value = "??????") @RequestParam(defaultValue = "1") Integer pageNo,
            @ApiParam(value = "????????????") @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        try {
            QueryResults<THistory> queryResults = sqlQueryFactory
                    .select(Projections.bean(THistory.class, qtHistory.pkId, qtHistory.hisType, qtHistory.hisShow,
                            qtHistory.hisContent,
                            qtHistory.createdBy, qtHistory.createdTime, qtHistory.originId, qtHistory.recordId))
                    .from(qtHistory)
                    .where(qtHistory.recordId.eq(recordId).and(qtHistory.hisType.eq(hisType)))
                    .limit(pageSize)
                    .offset((pageNo - 1) * pageSize)
                    .orderBy(qtHistory.createdTime.desc())
                    .fetchResults();
            //??????
            TableData<TPlugin> tableData = new TableData(queryResults.getTotal(), queryResults.getResults());
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "??????????????????!", tableData);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, e.getMessage());
        }
    }


    @ApiOperation(value = "????????????", notes = "????????????????????????")
    @GetMapping("/getByKey")
    public ResultDto getByKey(
            @ApiParam(value = "??????") @RequestParam(value = "pkId", required = true) String pkId
    ) {
        THistory tHistory = sqlQueryFactory
                .select(Projections.bean(THistory.class, qtHistory.pkId, qtHistory.hisType, qtHistory.hisShow, qtHistory.hisContent,
                        qtHistory.createdBy, qtHistory.createdTime, qtHistory.originId, qtHistory.recordId))
                .from(qtHistory)
                .where(qtHistory.pkId.eq(pkId))
                .fetchFirst();

        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "??????????????????!", tHistory);
    }


    @ApiOperation(value = "??????????????????", notes = "??????????????????")
    @PostMapping("/rollback")
    @Transactional(rollbackFor = Exception.class)
    public ResultDto rollback(
            @RequestBody HisRollbackDto hisRollbackDto,@RequestParam("loginUserName") String loginUserName
    ) {
        //??????????????????
        THistory tHistory = sqlQueryFactory
                .select(Projections.bean(THistory.class, qtHistory.pkId, qtHistory.hisType, qtHistory.hisContent, qtHistory.recordId))
                .from(qtHistory)
                .where(qtHistory.pkId.eq(hisRollbackDto.getPkId().toString()))
                .fetchFirst();
        if (tHistory == null) {
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "??????????????????????????????");
        }
        String hisType = hisRollbackDto.getHisType().toString();
        String hisContent = tHistory.getHisContent();
        JSONObject jsonObject = JSON.parseObject(hisContent);
        switch (hisType) {
            case "2":   //????????????
                TDrive tDrive = driveService.getOne(tHistory.getRecordId());
                if (tDrive == null) {
                    throw new RuntimeException("????????????????????????");
                }
                //????????????
                TType tType = typeService.getOne(tDrive.getTypeId());
                tDrive.setDriveTypeName(tType.getTypeName());
                if ("1".equals(tDrive.getDriveCallType())) {
                    tDrive.setDriveCallTypeName("?????????");
                }
                if ("2".equals(tDrive.getDriveCallType())) {
                    tDrive.setDriveCallTypeName("????????????");
                }
                insertHis(tDrive, Integer.valueOf(hisType), loginUserName, tHistory.getPkId(), tHistory.getRecordId(), null);
                //??????
                tDrive.setDriveName(jsonObject.getString("driveName"));
                tDrive.setDriveCode(jsonObject.getString("driveCode"));
                tDrive.setTypeId(jsonObject.getString("typeId"));
                tDrive.setDriveInstruction(jsonObject.getString("driveInstruction"));
                tDrive.setDriveContent(jsonObject.getString("driveContent"));
                tDrive.setDriveCallType(jsonObject.getString("driveCallType"));
                tDrive.setDependentPath(jsonObject.getString("dependentPath"));
                tDrive.setUpdatedBy(loginUserName);
                tDrive.setUpdatedTime(new Date());
                driveService.put(tDrive.getId(), tDrive);
                driveService.cacheDelete(tDrive.getId());
                break;
            case "3":   //????????????
                TPlugin tPlugin = pluginService.getOne(tHistory.getRecordId());
                if (tPlugin == null) {
                    throw new RuntimeException("????????????????????????");
                }
                //????????????
                TType tt = typeService.getOne(tPlugin.getTypeId());
                tPlugin.setPluginTypeName(tt.getTypeName());
                insertHis(tPlugin, Integer.valueOf(hisType), loginUserName, tHistory.getPkId(), tHistory.getRecordId(), null);
                //??????
                tPlugin.setPluginName(jsonObject.getString("pluginName"));
                tPlugin.setPluginCode(jsonObject.getString("pluginCode"));
                tPlugin.setTypeId(jsonObject.getString("typeId"));
                tPlugin.setPluginInstruction(jsonObject.getString("pluginInstruction"));
                tPlugin.setPluginContent(jsonObject.getString("pluginContent"));
                tPlugin.setDependentPath(jsonObject.getString("dependentPath"));
                tPlugin.setUpdatedBy(loginUserName);
                tPlugin.setUpdatedTime(new Date());
                pluginService.put(tPlugin.getId(), tPlugin);
                //????????????
                pluginService.cacheDelete(tPlugin.getId());
                break;
            default:
                throw new RuntimeException("???????????????????????????");
        }
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "????????????????????????!", null);
    }


    @ApiOperation(value = "????????????????????????????????????", notes = "????????????????????????????????????")
    @PostMapping("/rollback/businessInterface")
    @Transactional(rollbackFor = Exception.class)
    public ResultDto rollback(
            @RequestBody HisRollbackBIDto dto,@RequestParam("loginUserName") String loginUserName
    ) {
        if (dto == null || StringUtils.isBlank(dto.getPkId())
                || StringUtils.isBlank(dto.getInterfaceId())) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "????????????!");
        }
        //??????????????????
        THistory tHistory = sqlQueryFactory
                .select(Projections.bean(THistory.class, qtHistory.pkId, qtHistory.hisType, qtHistory.hisShow, qtHistory.hisContent, qtHistory.recordId))
                .from(qtHistory)
                .where(qtHistory.pkId.eq(dto.getPkId()))
                .fetchFirst();
        if (tHistory == null) {
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "??????????????????????????????");
        }
        String hisContent = tHistory.getHisContent();
        JSONArray jsonArray = JSON.parseArray(hisContent);
        if (jsonArray == null) {
            throw new RuntimeException("?????????????????????");
        }

        //??????
        String oldRecordId = tHistory.getRecordId();
        String lastRecordId = dto.getInterfaceId();
        String hisReqInterfaceId = oldRecordId;
        boolean noModReq = hisReqInterfaceId.equals(dto.getInterfaceId());
        if (noModReq) {
            //?????????????????????
        } else {
            //?????????????????????????????????????????????????????????????????????????????????
            TInterface tInterface = interfaceService.getOne(dto.getInterfaceId());
            if (tInterface == null) {
                return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "???????????????????????????????????????!");
            }
            List tbiList = businessInterfaceService.getListByCondition(dto.getInterfaceId());
            if (tbiList != null && tbiList.size() > 0) {
                return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "????????????????????????????????????!");
            }
            this.updateRecordId(oldRecordId, lastRecordId);
        }

        //??????history
        List<TBusinessInterface> list = businessInterfaceService.getListByCondition(hisReqInterfaceId);
        String businessInterfaceName = "";
        String versionId = "";
        Integer interfaceSlowFlag = null;
        Integer replayFlag = null;
        String requestInterfaceName = "";
        String typeId = "";
        String requestSysId = "";
        for (int i = 0; i < list.size(); i++) {
            TBusinessInterface tbi = list.get(i);
            TInterface tInterface = interfaceService.getOne(tbi.getRequestInterfaceId());
            if (StringUtils.isNotBlank(tbi.getSysRegistryId())) {
                TSysRegistry tSysRegistry = sysRegistryService.getOne(tbi.getSysRegistryId());
                TSys requestedSys = sysService.getOne(tSysRegistry.getSysId());
                businessInterfaceName += (requestedSys.getSysName() + "/" + tbi.getBusinessInterfaceName() + ",");
            }
            if (StringUtils.isBlank(typeId)) {
                typeId = tInterface.getTypeId();
            }
            //?????????????????????
            requestInterfaceName = tInterface.getInterfaceName();
            if (StringUtils.isBlank(typeId)) {
                typeId = tInterface.getTypeId();
            }
            if (interfaceSlowFlag != null) {
                interfaceSlowFlag = tbi.getInterfaceSlowFlag();
            }
            if (replayFlag != null) {
                replayFlag = tbi.getReplayFlag();
            }
        }
        if (businessInterfaceName.endsWith(",")) {
            businessInterfaceName = businessInterfaceName.substring(0, businessInterfaceName.length() - 1);
        }
        if (versionId.endsWith(",")) {
            versionId = versionId.substring(0, versionId.length() - 1);
        }
        //??????????????????
        Map map = new HashMap();
        map.put("requestInterfaceId", hisReqInterfaceId);
        map.put("businessInterfaceName", businessInterfaceName);
        map.put("requestInterfaceName", requestInterfaceName);
        map.put("requestSysId", requestSysId);
        map.put("requestInterfaceTypeId", typeId);
        map.put("interfaceSlowFlag", interfaceSlowFlag);
        map.put("replayFlag", replayFlag);
        String hisShow = JSON.toJSONString(map);
        this.insertHis(list, 1, loginUserName, lastRecordId, lastRecordId, hisShow);

        //????????????
        businessInterfaceService.cacheDelete(list.get(0).getId());

        //??????
        businessInterfaceService.delObjByCondition(hisReqInterfaceId);
        for (Object obj : jsonArray) {
            TBusinessInterface jObj = JSON.parseObject(obj.toString(),TBusinessInterface.class);
//            jObj.setId(batchUidService.getUid(qTBusinessInterface.getTableName()) + "");
            jObj.setCreatedBy(loginUserName);
            // ??????schema
            niFiRequestUtil.generateSchemaToInterface(jObj);
            businessInterfaceService.post(jObj);
        }

        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "????????????????????????!", null);
    }


    @ApiOperation(value = "??????????????????-????????????", notes = "??????????????????-????????????")
    @GetMapping("/rollback/interface")
    @Transactional(rollbackFor = Exception.class)
    public ResultDto rollbackInterface(@RequestParam String id,@RequestParam("loginUserName") String loginUserName) {
        THistory ti = this.getOne(id);
        if (ti == null) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "??????id??????????????????!", null);
        }
        try {
            //???????????????????????????????????????
            interfaceService.write2His(ti.getRecordId(),loginUserName);

            //????????????
            interfaceService.cacheDelete(ti.getRecordId());

            //????????????????????????????????????
            InterfaceDto tInterface=JSONObject.parseObject(ti.getHisContent(),InterfaceDto.class);
            interfaceService.updateInterface(tInterface,loginUserName,false);

            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "????????????!", null);
        } catch (Exception e) {
            logger.error("????????????! MSG:{}", ExceptionUtil.dealException(e));
            e.printStackTrace();
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "????????????!");
        }
    }


    //??????????????????
    public void insertHis(Object obj, Integer hisType, String createdBy, String originId, String recordId, String hisShow) {
        THistory tHistory = new THistory();
        tHistory.setPkId(batchUidService.getUid(qtHistory.getTableName()) + "");
        tHistory.setHisType(hisType);
        tHistory.setHisContent(JSON.toJSONString(obj));
        tHistory.setCreatedBy(createdBy);
        tHistory.setCreatedTime(new Date());
        tHistory.setOriginId(originId);
        tHistory.setRecordId(recordId);
        tHistory.setHisShow(hisShow);
        this.post(tHistory);
    }


    /**
     * ??????recordId
     *
     * @param old  ???recordId
     * @param last ???recordId
     * @return
     */
    public long updateRecordId(String old, String last) {
        return sqlQueryFactory.update(qtHistory).set(qtHistory.recordId, last).where(qtHistory.recordId.eq(old)).execute();
    }


}
