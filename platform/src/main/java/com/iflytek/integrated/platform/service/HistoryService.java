package com.iflytek.integrated.platform.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.common.dto.TableData;
import com.iflytek.integrated.common.intercept.UserLoginIntercept;
import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.platform.dto.HisRollbackBIDto;
import com.iflytek.integrated.platform.dto.HisRollbackDto;
import com.iflytek.integrated.platform.entity.TBusinessInterface;
import com.iflytek.integrated.platform.entity.TDrive;
import com.iflytek.integrated.platform.entity.THistory;
import com.iflytek.integrated.platform.entity.TInterface;
import com.iflytek.integrated.platform.entity.TPlugin;
import com.iflytek.integrated.platform.entity.TSys;
import com.iflytek.integrated.platform.entity.TSysConfig;
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
@Api(tags = "历史版本接口")
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

    @ApiOperation(value = "获取列表", notes = "获取历史版本列表")
    @GetMapping("/getHisList")
    public ResultDto getHisList(
            @ApiParam(value = "接口转换/驱动/插件的id") @RequestParam(value = "recordId", required = true) String recordId,
            @ApiParam(value = "历史版本类型（1接口转换 2驱动 3插件）") @RequestParam(value = "hisType", required = true) Integer hisType,
            @ApiParam(value = "页码") @RequestParam(defaultValue = "1") Integer pageNo,
            @ApiParam(value = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        try {
            // 校验是否获取到登录用户
            String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
            if (StringUtils.isBlank(loginUserName)) {
                return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!");
            }

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
            //分页
            TableData<TPlugin> tableData = new TableData(queryResults.getTotal(), queryResults.getResults());
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取列表成功!", tableData);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, e.getMessage());
        }
    }


    @ApiOperation(value = "获取详情", notes = "获取历史版本详情")
    @GetMapping("/getByKey")
    public ResultDto getByKey(
            @ApiParam(value = "主键") @RequestParam(value = "pkId", required = true) String pkId
    ) {
        // 校验是否获取到登录用户
        String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
        if (StringUtils.isBlank(loginUserName)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!");
        }

        THistory tHistory = sqlQueryFactory
                .select(Projections.bean(THistory.class, qtHistory.pkId, qtHistory.hisType, qtHistory.hisShow, qtHistory.hisContent,
                        qtHistory.createdBy, qtHistory.createdTime, qtHistory.originId, qtHistory.recordId))
                .from(qtHistory)
                .where(qtHistory.pkId.eq(pkId))
                .fetchFirst();

        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取详情成功!", tHistory);
    }


    @ApiOperation(value = "回滚历史版本", notes = "回滚历史版本")
    @PostMapping("/rollback")
    @Transactional(rollbackFor = Exception.class)
    public ResultDto rollback(
            @RequestBody HisRollbackDto hisRollbackDto
    ) {
        // 校验是否获取到登录用户
        String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
        if (StringUtils.isBlank(loginUserName)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!");
        }
        //获取历史版本
        THistory tHistory = sqlQueryFactory
                .select(Projections.bean(THistory.class, qtHistory.pkId, qtHistory.hisType, qtHistory.hisContent, qtHistory.recordId))
                .from(qtHistory)
                .where(qtHistory.pkId.eq(hisRollbackDto.getPkId().toString()))
                .fetchFirst();
        if (tHistory == null) {
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "未查询到该历史版本！");
        }
        String hisType = hisRollbackDto.getHisType().toString();
        String hisContent = tHistory.getHisContent();
        JSONObject jsonObject = JSON.parseObject(hisContent);
        switch (hisType) {
            case "2":   //当前驱动
                TDrive tDrive = driveService.getOne(tHistory.getRecordId());
                if (tDrive == null) {
                    throw new RuntimeException("未查询到该驱动！");
                }
                //插入历史
                TType tType = typeService.getOne(tDrive.getTypeId());
                tDrive.setDriveTypeName(tType.getTypeName());
                if ("1".equals(tDrive.getDriveCallType())) {
                    tDrive.setDriveCallTypeName("请求方");
                }
                if ("2".equals(tDrive.getDriveCallType())) {
                    tDrive.setDriveCallTypeName("被请求方");
                }
                insertHis(tDrive, Integer.valueOf(hisType), loginUserName, tHistory.getPkId(), tHistory.getRecordId(), null);
                //修改
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
                break;
            case "3":   //当前插件
                TPlugin tPlugin = pluginService.getOne(tHistory.getRecordId());
                if (tPlugin == null) {
                    throw new RuntimeException("未查询到该插件！");
                }
                //插入历史
                TType tt = typeService.getOne(tPlugin.getTypeId());
                tPlugin.setPluginTypeName(tt.getTypeName());
                insertHis(tPlugin, Integer.valueOf(hisType), loginUserName, tHistory.getPkId(), tHistory.getRecordId(), null);
                //修改
                tPlugin.setPluginName(jsonObject.getString("pluginName"));
                tPlugin.setPluginCode(jsonObject.getString("pluginCode"));
                tPlugin.setTypeId(jsonObject.getString("typeId"));
                tPlugin.setPluginInstruction(jsonObject.getString("pluginInstruction"));
                tPlugin.setPluginContent(jsonObject.getString("pluginContent"));
                tPlugin.setDependentPath(jsonObject.getString("dependentPath"));
                pluginService.put(tPlugin.getId(), tPlugin);
                break;
            default:
                throw new RuntimeException("历史版本类型错误！");
        }
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "回滚历史版本成功!", null);
    }


    @ApiOperation(value = "回滚历史版本（接口转换）", notes = "回滚历史版本（接口转换）")
    @PostMapping("/rollback/businessInterface")
    @Transactional(rollbackFor = Exception.class)
    public ResultDto rollback(
            @RequestBody HisRollbackBIDto dto
    ) {
        if (dto == null || StringUtils.isBlank(dto.getPkId())
                || StringUtils.isBlank(dto.getInterfaceId())) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "入参为空!");
        }
        // 校验是否获取到登录用户
        String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
        if (StringUtils.isBlank(loginUserName)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!");
        }
        //获取历史版本
        THistory tHistory = sqlQueryFactory
                .select(Projections.bean(THistory.class, qtHistory.pkId, qtHistory.hisType, qtHistory.hisShow, qtHistory.hisContent, qtHistory.recordId))
                .from(qtHistory)
                .where(qtHistory.pkId.eq(dto.getPkId()))
                .fetchFirst();
        if (tHistory == null) {
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "未查询到该历史版本！");
        }
        String hisContent = tHistory.getHisContent();
        JSONArray jsonArray = JSON.parseArray(hisContent);
        if (jsonArray == null) {
            throw new RuntimeException("历史数据为空！");
        }

        //校验
        String oldRecordId = tHistory.getRecordId();
        String lastRecordId = dto.getInterfaceId();
        String[] arr = oldRecordId.split(",");
        String hisReqSysId = arr[0];
        String hisReqInterfaceId = arr[1];
        boolean noModReq = hisReqInterfaceId.equals(dto.getInterfaceId());
        if (noModReq) {
            //未修改过请求方
        } else {
            //请求方不一致，需校验历史版本的请求方是否存在、是否重复
            TInterface tInterface = interfaceService.getOne(dto.getInterfaceId());
            if (tInterface == null) {
                return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "未查询到历史版本中的请求方!");
            }
            List tbiList = businessInterfaceService.getListByCondition(dto.getInterfaceId());
            if (tbiList != null && tbiList.size() > 0) {
                return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "历史版本中的请求方已存在!");
            }
            this.updateRecordId(oldRecordId, lastRecordId);
        }

        //插入history
        List<TBusinessInterface> list = businessInterfaceService.getListByCondition(hisReqInterfaceId);
        String businessInterfaceName = "";
        String versionId = "";
        Integer interfaceSlowFlag = null;
        Integer replayFlag = null;
        String QIId = null;
        Integer QIFlag = null;
        String requestInterfaceName = "";
        String typeId = "";
        String requestSysId = "";
        for (int i = 0; i < list.size(); i++) {
            TBusinessInterface tbi = list.get(i);
            TInterface tInterface = interfaceService.getOne(tbi.getRequestInterfaceId());
            if (StringUtils.isNotBlank(tbi.getSysRegistryId())) {
                TSysRegistry tSysRegistry = sysRegistryService.getOne(tbi.getSysRegistryId());
                TSys requestedSys = sysService.getOne(tSysRegistry.getSysId());
                tbi.setRequestedSysId(requestedSys.getId());
                businessInterfaceName += (requestedSys.getSysName() + "/" + tbi.getBusinessInterfaceName() + ",");
            }
            if (StringUtils.isBlank(typeId)) {
                typeId = tInterface.getTypeId();
            }
            //请求方服务名称
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
        //插入历史记录
        Map map = new HashMap();
        map.put("requestSysConfigId", hisReqSysId);
        map.put("requestInterfaceId", hisReqInterfaceId);
        map.put("businessInterfaceName", businessInterfaceName);
        map.put("requestInterfaceName", requestInterfaceName);
        map.put("requestSysId", requestSysId);
        map.put("requestInterfaceTypeId", typeId);
        map.put("interfaceSlowFlag", interfaceSlowFlag);
        map.put("replayFlag", replayFlag);
        map.put("QIId", QIId);
        map.put("QIFlag", QIFlag);
        String hisShow = JSON.toJSONString(map);
        this.insertHis(list, 1, loginUserName, lastRecordId, lastRecordId, hisShow);

        //删除
        businessInterfaceService.delObjByCondition(hisReqInterfaceId);
        for (Object obj : jsonArray) {
            JSONObject jObj = JSON.parseObject(obj.toString());
            TBusinessInterface tbi = new TBusinessInterface();
            tbi.setId(batchUidService.getUid(qTBusinessInterface.getTableName()) + "");
            tbi.setSysRegistryId(jObj.getString("sysRegistryId"));
            tbi.setRequestInterfaceId(jObj.getString("requestInterfaceId"));
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
            tbi.setOutParamFormatType(jObj.getString("outParamFormatType"));
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

        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "回滚历史版本成功!", null);
    }


    //插入历史记录
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
     * 修改recordId
     *
     * @param old  旧recordId
     * @param last 新recordId
     * @return
     */
    public long updateRecordId(String old, String last) {
        return sqlQueryFactory.update(qtHistory).set(qtHistory.recordId, last).where(qtHistory.recordId.eq(old)).execute();
    }


}
