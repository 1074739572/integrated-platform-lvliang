package com.iflytek.integrated.platform.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonObject;
import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.common.intercept.UserLoginIntercept;
import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.platform.dto.HisRollbackBIDto;
import com.iflytek.integrated.platform.dto.HisRollbackDto;
import com.iflytek.integrated.platform.entity.*;
import com.iflytek.integrated.platform.utils.NiFiRequestUtil;
import com.iflytek.medicalboot.core.id.BatchUidService;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.StringPath;

import static com.iflytek.integrated.platform.entity.QTBusinessInterface.qTBusinessInterface;
import static com.iflytek.integrated.platform.entity.QTHistory.qtHistory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@Slf4j
@Api(tags = "历史版本接口")
@RestController
@RequestMapping("/{version}/pt/history")
public class HistoryService extends BaseService<THistory, String, StringPath> {

    private static final Logger logger = LoggerFactory.getLogger(InterfaceService.class);

    public HistoryService() { super(qtHistory, qtHistory.pkId); }

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

    @ApiOperation(value = "获取列表", notes = "获取历史版本列表")
    @GetMapping("/getHisList")
    public ResultDto getHisList(
            @ApiParam(value = "接口转换/驱动/插件的id") @RequestParam(value = "recordId", required = true) String recordId,
            @ApiParam(value = "历史版本类型（1接口转换 2驱动 3插件）") @RequestParam(value = "hisType", required = true) Integer hisType
    ){
        try{
            // 校验是否获取到登录用户
            String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
            if (StringUtils.isBlank(loginUserName)) {
                return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!");
            }

            List list = sqlQueryFactory
                    .select(Projections.bean(THistory.class,qtHistory.pkId,qtHistory.hisType,qtHistory.hisContent,
                            qtHistory.createdBy,qtHistory.createdTime,qtHistory.originId,qtHistory.recordId))
                    .from(qtHistory)
                    .where(qtHistory.recordId.eq(recordId).and(qtHistory.hisType.eq(hisType)))
                    .fetch();
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取列表成功!", list);
        }catch (Exception e){
            e.printStackTrace();
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, e.getMessage());
        }
    }


    @ApiOperation(value = "获取详情",notes = "获取历史版本详情")
    @GetMapping("/getByKey")
    public ResultDto getByKey(
            @ApiParam(value = "主键") @RequestParam(value = "pkId", required = true) String pkId
    ){
        // 校验是否获取到登录用户
        String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
        if (StringUtils.isBlank(loginUserName)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!");
        }

        THistory tHistory = sqlQueryFactory
                .select(Projections.bean(THistory.class,qtHistory.pkId,qtHistory.hisType,qtHistory.hisContent,
                        qtHistory.createdBy,qtHistory.createdTime,qtHistory.originId,qtHistory.recordId))
                .from(qtHistory)
                .where(qtHistory.pkId.eq(pkId))
                .fetchFirst();

        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取详情成功!", tHistory);
    }


    @ApiOperation(value = "回滚历史版本",notes = "回滚历史版本")
    @PostMapping("/rollback")
    @Transactional(rollbackFor = Exception.class)
    public ResultDto rollback(
            @RequestBody HisRollbackDto hisRollbackDto
    ){
        try{
            // 校验是否获取到登录用户
            String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
            if (StringUtils.isBlank(loginUserName)) {
                return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!");
            }
            //获取历史版本并重新插入
            THistory tHistory = sqlQueryFactory
                    .select(Projections.bean(THistory.class,qtHistory.pkId,qtHistory.hisType,qtHistory.hisContent,qtHistory.recordId))
                    .from(qtHistory)
                    .where(qtHistory.pkId.eq(hisRollbackDto.getPkId().toString()))
                    .fetchFirst();
            if(tHistory == null){
                return new ResultDto(Constant.ResultCode.ERROR_CODE, "未查询到该历史版本！");
            }
            tHistory.setPkId(batchUidService.getUid(qTBusinessInterface.getTableName()) + "");
            tHistory.setRecordId(tHistory.getRecordId());
            tHistory.setOriginId(tHistory.getPkId().toString());
            tHistory.setCreatedBy(loginUserName);
            tHistory.setCreatedTime(new Date());
            this.post(tHistory);
            //获取当前记录并修改
            String hisType = hisRollbackDto.getHisType().toString();
            String recordId = tHistory.getRecordId();
            String hisContent = tHistory.getHisContent();
            JSONObject jsonObject = JSON.parseObject(hisContent);
            switch (hisType){
                case "2":   //驱动
                    TDrive tDrive = driveService.getOne(recordId);
                    if(tDrive == null){
                        throw new RuntimeException("未查询到该驱动！");
                    }
                    tDrive.setDriveName(jsonObject.getString("driveName"));
                    tDrive.setDriveCode(jsonObject.getString("driveCode"));
                    tDrive.setTypeId(jsonObject.getString("typeId"));
                    tDrive.setDriveInstruction(jsonObject.getString("driveInstruction"));
                    tDrive.setDriveContent(jsonObject.getString("driveContent"));
                    tDrive.setDriveCallType(jsonObject.getString("driveCallType"));
                    tDrive.setDependentPath(jsonObject.getString("dependentPath"));
                    tDrive.setUpdatedBy(loginUserName);
                    tDrive.setUpdatedTime(new Date());
                    driveService.put(tDrive.getId(),tDrive);
                    break;
                case "3":   //插件
                    TPlugin tPlugin = pluginService.getOne(recordId);
                    if(tPlugin == null){
                        throw new RuntimeException("未查询到该插件！");
                    }
                    tPlugin.setPluginName(jsonObject.get("pluginName").toString());
                    tPlugin.setPluginCode(jsonObject.get("pluginCode").toString());
                    tPlugin.setTypeId(jsonObject.get("typeId").toString());
                    tPlugin.setPluginInstruction(jsonObject.get("pluginInstruction").toString());
                    tPlugin.setPluginContent(jsonObject.get("pluginContent(").toString());
                    tPlugin.setDependentPath(jsonObject.get("dependentPath").toString());
                    pluginService.put(tPlugin.getId(),tPlugin);
                    break;
                default:
                    throw new RuntimeException("历史版本类型错误！");
            }
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "回滚历史版本成功!", null);
        }catch (Exception ex){
            ex.printStackTrace();
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, ex.getMessage());
        }
    }


    @ApiOperation(value = "回滚历史版本（接口转换）",notes = "回滚历史版本（接口转换）")
    @PostMapping("/rollback/businessInterface")
    @Transactional(rollbackFor = Exception.class)
    public ResultDto rollback(
            @RequestBody HisRollbackBIDto dto
    ){
        try{
            if(dto == null || StringUtils.isBlank(dto.getPkId())
                    || StringUtils.isBlank(dto.getInterfaceId()) || StringUtils.isBlank(dto.getRequestSysconfigId())){
                return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "入参为空!");
            }
            // 校验是否获取到登录用户
            String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
            if (StringUtils.isBlank(loginUserName)) {
                return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!");
            }
            //获取历史版本
            THistory tHistory = sqlQueryFactory
                    .select(Projections.bean(THistory.class,qtHistory.pkId,qtHistory.hisType,qtHistory.hisContent,qtHistory.recordId))
                    .from(qtHistory)
                    .where(qtHistory.pkId.eq(dto.getPkId().toString()))
                    .fetchFirst();
            if(tHistory == null){
                return new ResultDto(Constant.ResultCode.ERROR_CODE, "未查询到该历史版本！");
            }
            String hisContent = tHistory.getHisContent();
            JSONArray jsonArray = JSON.parseArray(hisContent);
            if(jsonArray == null){
                throw new RuntimeException("历史数据为空！");
            }
            //删除
            businessInterfaceService.delObjByCondition(dto.getInterfaceId(), dto.getRequestSysconfigId());
            for(Object obj : jsonArray){
                JSONObject jObj = JSON.parseObject(obj.toString());
                TBusinessInterface tbi = new TBusinessInterface();
                tbi.setId(batchUidService.getUid(qTBusinessInterface.getTableName()) + "");
                tbi.setRequestSysconfigId(jObj.getString("requestSysconfigId"));
                tbi.setRequestInterfaceId(jObj.getString("requestInterfaceId"));
                tbi.setRequestedSysconfigId(jObj.getString("requestedSysconfigId"));
                tbi.setBusinessInterfaceName(jObj.getString("businessInterfaceName"));
                tbi.setRequestType(jObj.getString("requestType"));
                tbi.setRequestConstant(jObj.getString("requestConstant"));
                tbi.setInterfaceType(jObj.getString("interfaceType"));
                tbi.setPluginId(jObj.getString("pluginId"));
                tbi.setInParamFormat(jObj.getString("inParamFormat"));
                tbi.setInParamSchema(jObj.getString("inParamSchema"));
                tbi.setInParamTemplateType(jObj.getString("inParamTemplateType"));
                tbi.setInParamTemplate(jObj.getString("inParamTemplate"));
                tbi.setInParamFormatType(jObj.getString("inParamFormatType"));
                tbi.setOutParamFormat(jObj.getString("outParamFormat"));
                tbi.setOutParamSchema(jObj.getString("outParamSchema"));
                tbi.setOutParamTemplateType(jObj.getString("outParamTemplateType"));
                tbi.setOutParamTemplate(jObj.getString("outParamTemplate"));
                tbi.setMockTemplate(jObj.getString("mockTemplate"));
                tbi.setMockStatus(jObj.getString("mockStatus"));
                tbi.setStatus(jObj.getString("status"));
                tbi.setExcErrStatus(jObj.getString("excErrStatus"));
                tbi.setExcErrOrder(jObj.getInteger("excErrOrder"));
                tbi.setMockIsUse(jObj.getInteger("mockIsUse"));
                tbi.setCreatedBy(loginUserName);
                tbi.setCreatedTime(new Date());
                tbi.setUpdatedBy(loginUserName);
                tbi.setUpdatedTime(new Date());
                tbi.setAsyncFlag(jObj.getInteger("asyncFlag"));
                tbi.setInterfaceSlowFlag(jObj.getInteger("interfaceSlowFlag"));
                tbi.setReplayFlag(jObj.getInteger("replayFlag"));
                // 获取schema
                niFiRequestUtil.generateSchemaToInterface(tbi);
                businessInterfaceService.post(tbi);
            }

            //插入history
            tHistory.setPkId(batchUidService.getUid(qTBusinessInterface.getTableName()) + "");
            tHistory.setRecordId(tHistory.getRecordId());
            tHistory.setOriginId(dto.getPkId().toString());
            tHistory.setCreatedBy(loginUserName);
            tHistory.setCreatedTime(new Date());
            this.post(tHistory);
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "回滚历史版本成功!", null);
        }catch (Exception ex){
            ex.printStackTrace();
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, ex.getMessage());
        }
    }


    //插入历史记录
    public void insertHis(Object obj, Integer hisType, String createdBy, String originId, String recordId){
        THistory tHistory = new THistory();
        tHistory.setPkId(batchUidService.getUid(qTBusinessInterface.getTableName()) + "");
        tHistory.setHisType(hisType);
        tHistory.setHisContent(JSON.toJSONString(obj));
        tHistory.setCreatedBy(createdBy);
        tHistory.setCreatedTime(new Date());
        tHistory.setOriginId(originId);
        tHistory.setRecordId(recordId);
        this.post(tHistory);
    }


    /**
     * 修改recordId
     * @param old 旧recordId
     * @param last 新recordId
     * @return
     */
    public long updateRecordId(String old, String last){
        return sqlQueryFactory.update(qtHistory).set(qtHistory.recordId,last).where(qtHistory.recordId.eq(old)).execute();
    }


}
