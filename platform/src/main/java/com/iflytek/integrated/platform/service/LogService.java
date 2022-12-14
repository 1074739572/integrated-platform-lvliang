package com.iflytek.integrated.platform.service;

import com.alibaba.fastjson.JSONObject;
import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.common.dto.TableData;
import com.iflytek.integrated.common.utils.ExceptionUtil;
import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.platform.dto.DebugDto;
import com.iflytek.integrated.platform.entity.QTSys;
import com.iflytek.integrated.platform.entity.TBusinessInterface;
import com.iflytek.integrated.platform.entity.TLog;
import com.iflytek.integrated.platform.entity.TSysPublish;
import com.iflytek.integrated.platform.utils.NiFiRequestUtil;
import com.iflytek.integrated.platform.utils.PlatformUtil;
import com.querydsl.core.QueryFlag;
import com.querydsl.core.QueryFlag.Position;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.function.Function;
import java.util.stream.Collectors;

import static com.iflytek.integrated.platform.entity.QTBusinessInterface.qTBusinessInterface;
import static com.iflytek.integrated.platform.entity.QTInterface.qTInterface;
import static com.iflytek.integrated.platform.entity.QTLog.qTLog;
import static com.iflytek.integrated.platform.entity.QTSys.qTSys;
import static com.iflytek.integrated.platform.entity.QTSysPublish.qTSysPublish;
import static com.iflytek.integrated.platform.entity.QTSysRegistry.qTSysRegistry;

/**
 * @author czzhan
 * @version 1.0
 * @date 2020/12/20 16:46
 */
@Slf4j
@Api(tags = "????????????")
@RestController
@RequestMapping("/{version}/pt/log")
public class LogService extends BaseService<TLog, Long, NumberPath<Long>> {
    private static final Logger logger = LoggerFactory.getLogger(LogService.class);

    @Autowired
    private NiFiRequestUtil niFiRequestUtil;

    @Autowired
    SQLQueryFactory shardingSqlQueryFactory;

    @Autowired
    private BusinessInterfaceService businessInterfaceService;

    @Value("${server.db}")
    private String dbType;

    @Value("${config.request.nifiapi.readtimeout}")
    private int readTimeout;

    public LogService() {
        super(qTLog, qTLog.id);
    }


    @ApiOperation(value = "????????????????????????")
    @GetMapping("/logInfoList")
    public ResultDto<TableData<TLog>> logInfoList(String interfaceId, String status, String visitAddr, String interfaceName,
                                                  @ApiParam(value = "????????????") @RequestParam(value = "startTime", required = false) String startTime,
                                                  @ApiParam(value = "????????????") @RequestParam(value = "endTime", required = false) String endTime,
                                                  @ApiParam(value = "???????????????") @RequestParam(value = "businessReq", required = false) String businessReq,
                                                  @ApiParam(value = "???????????????") @RequestParam(value = "businessRep", required = false) String businessRep,
                                                  @ApiParam(value = "??????????????????") @RequestParam(value = "venderReq", required = false) String venderReq,
                                                  @ApiParam(value = "??????????????????") @RequestParam(value = "venderRep", required = false) String venderRep,
                                                  @ApiParam(value = "??????????????? 1 ????????? 0?????????") @RequestParam(defaultValue = "0") Integer isNext,
                                                  @ApiParam(value = "??????id") @RequestParam(defaultValue = "0") String id,
                                                  @RequestParam(defaultValue = "1") Integer pageSize) {

        // ????????????
        ArrayList<Predicate> list = new ArrayList<>();
        List<String> btIdList = new ArrayList<>();
        if (interfaceId != null && interfaceId != "") {
            //???????????????id??????????????????
            List<TBusinessInterface> tBusinessInterfaces = businessInterfaceService.getListByInterfaceId(interfaceId);
            if (CollectionUtils.isNotEmpty(tBusinessInterfaces)) {
                btIdList = tBusinessInterfaces.stream().map(TBusinessInterface::getId).collect(Collectors.toList());
            }
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (startTime != null && startTime != "") {
                list.add(qTLog.createdTime.goe(sdf.parse(startTime)));
            }
            if (endTime != null && endTime != "") {
                list.add(qTLog.createdTime.loe(sdf.parse(endTime)));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //???????????????????????????????????????
        if (CollectionUtils.isNotEmpty(btIdList)) {
            list.add(qTLog.businessInterfaceId.in(btIdList));
        }

        if (StringUtils.isNotBlank(interfaceName)) {
            list.add(qTInterface.interfaceName.like("%" + interfaceName + "%"));
        }
        if (StringUtils.isNotBlank(status)) {
            list.add(qTLog.status.eq(status));
        }
        // ????????????????????????
        if (StringUtils.isNotBlank(visitAddr)) {
            list.add(qTLog.visitAddr.like(PlatformUtil.createFuzzyText(visitAddr)));
        }

        String tplStr = "AES_DECRYPT(from_base64({0}),'w5xv7[Nmc0Z/3U^X')";
        String emptyEvaluteStr = "COALESCE({0},'')";
        if("postgresql".equals(dbType)) {
            tplStr = "CONVERT_FROM(decrypt(decode({0},'base64') ,'w5xv7[Nmc0Z/3U^X' ,'aes-ecb/pad:pkcs'),'UTF-8')";
        }
        if (StringUtils.isNotBlank(businessReq)) {
            list.add(Expressions.stringTemplate(tplStr, qTLog.businessReq)
                    .like(PlatformUtil.createFuzzyText(businessReq)));
            list.add(Expressions.stringTemplate(emptyEvaluteStr,qTLog.businessReq).ne(""));
        }
        if (StringUtils.isNotBlank(businessRep)) {
            list.add(Expressions.stringTemplate(tplStr, qTLog.businessRep)
                    .like(PlatformUtil.createFuzzyText(businessRep)));
        }
        if (StringUtils.isNotBlank(venderReq)) {
            list.add(Expressions.stringTemplate(tplStr, qTLog.venderReq)
                    .like(PlatformUtil.createFuzzyText(venderReq)));
            list.add(Expressions.stringTemplate(emptyEvaluteStr,qTLog.venderReq).ne(""));
        }

        if (StringUtils.isNotBlank(venderRep)) {
            list.add(Expressions.stringTemplate(tplStr, qTLog.venderRep)
                    .like(PlatformUtil.createFuzzyText(venderRep)));
        }

        TLog count = shardingSqlQueryFactory.select(Projections.bean(TLog.class, qTLog.id.count().as("count"))).from(qTLog)
                .where(list.toArray(new Predicate[list.size()])).fetchOne();

        //SHARDING????????????

        List<TLog> tLogList = pageLogs(isNext, id, pageSize, list);


        QueryResults<TLog> tLogQueryResults=new QueryResults<TLog>(tLogList,new Long(pageSize),null,count.getCount());
        if (tLogQueryResults == null && tLogQueryResults.isEmpty()) {
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "??????????????????????????????!", new TableData<>());
        }

        //???????????????????????????
        Set<String> btList = new HashSet<>();
        Set<String> pubList = new HashSet<>();

        for (TLog result : tLogQueryResults.getResults()) {
            if (!StringUtils.isEmpty(result.getBusinessInterfaceId())) {
                btList.add(result.getBusinessInterfaceId());
            }

            if (!StringUtils.isEmpty(result.getPublishId())) {
                pubList.add(result.getPublishId());
            }
        }

        //1.??????????????????
        List<TBusinessInterface> btResults = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(btList)) {
            ArrayList<Predicate> listBt = new ArrayList<>();
            listBt.add(qTBusinessInterface.id.in(btList));
            btResults = sqlQueryFactory.select(Projections.bean(TBusinessInterface.class,
                            qTBusinessInterface.id, qTBusinessInterface.excErrOrder, qTBusinessInterface.requestInterfaceId,
                            qTBusinessInterface.replayFlag, qTBusinessInterface.businessInterfaceName,
                            qTInterface.interfaceName, qTInterface.interfaceUrl,
                            qTSysRegistry.registryName,
                            qTBusinessInterface.sysRegistryId, qTSys.sysName))
                    .from(qTBusinessInterface)
                    .leftJoin(qTInterface).on(qTBusinessInterface.requestInterfaceId.eq(qTInterface.id))
                    .leftJoin(qTSysRegistry).on(qTBusinessInterface.sysRegistryId.eq(qTSysRegistry.id))
                    .leftJoin(qTSys).on(qTSysRegistry.sysId.eq(qTSys.id))
                    .where(listBt.toArray(new Predicate[listBt.size()]))
                    .fetch();

        }

        //3.??????????????????
        List<TSysPublish> pubResults = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(pubList)) {
            ArrayList<Predicate> listPub = new ArrayList<>();
            listPub.add(qTSysPublish.id.in(pubList));
            pubResults = sqlQueryFactory.select(Projections.bean(TSysPublish.class,
                            qTSysPublish.id,qTSysPublish.publishName, qTSys.sysName))
                    .from(qTSysPublish)
                    .leftJoin(qTSys).on(qTSysPublish.sysId.eq(qTSys.id))
                    .where(listPub.toArray(new Predicate[listPub.size()]))
                    .fetch();
        }

        //???????????????
        combineResult(tLogQueryResults, btResults, pubResults);

        if (tLogQueryResults != null && tLogQueryResults.getResults() != null && tLogQueryResults.getResults().size() > 0) {
            Set<String> set = new HashSet<>();
            tLogQueryResults.getResults().forEach(
                    record -> set.add(record.getBusinessInterfaceId())
            );
            List<TBusinessInterface> businessInterfaces = sqlQueryFactory.select(Projections.bean(TBusinessInterface.class,
                            qTBusinessInterface.requestInterfaceId, qTBusinessInterface.excErrOrder.max().as("maxOrder")))
                    .from(qTBusinessInterface)
                    .where(qTBusinessInterface.id.in(set))
                    .groupBy(qTBusinessInterface.requestInterfaceId)
                    .fetch();
            Map map = new HashMap();
            businessInterfaces.forEach(bi -> {
                map.put(bi.getRequestInterfaceId(), bi.getMaxOrder());
            });
            tLogQueryResults.getResults().forEach(record -> {
                if ("0".equals(record.getBusinessInterfaceId()) || record.getExcErrOrder() == null) {
                    record.setShowOrder("1/1");
                } else {
                    int excErrOrder = record.getExcErrOrder() + 1;
                    int maxOrder = record.getBusinessInterfaceId() == null ? 0 : (Integer) map.get(record.getRequestInterfaceId());
                    record.setShowOrder(excErrOrder + "/" + (maxOrder + 1));
                }
            });
        }

        // ??????
        TableData<TLog> tableData = new TableData<>(tLogQueryResults.getTotal(), tLogQueryResults.getResults());
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "??????????????????????????????!", tableData);
    }

    private List<TLog> pageLogs(Integer isNext, String id, Integer pageSize, ArrayList<Predicate> list) {
        List<OrderSpecifier> orderList=new ArrayList<>();
        //???????????????
        if(isNext ==1){
            list.add(qTLog.id.lt(Long.valueOf(id)));
            orderList.add(qTLog.createdTime.desc());
            orderList.add(qTLog.requestIdentifier.desc());
        }else{
            list.add(qTLog.id.gt(Long.valueOf(id)));
            if(StringUtils.equals("0",id)) {
                //?????????0 ????????????????????????  id>0  ?????????
                orderList.add(qTLog.createdTime.desc());
                orderList.add(qTLog.requestIdentifier.desc());
            }else{
                //?????????????????????????????????????????????id
                //???????????????????????????id??????????????? ??????N???
                //??????????????????
                orderList.add(qTLog.createdTime.asc());
                orderList.add(qTLog.requestIdentifier.asc());
            }
        }

        //????????????????????????
        List<TLog> tLogList = shardingSqlQueryFactory.select(Projections.bean(TLog.class, qTLog.id, qTLog.businessInterfaceId, qTLog.createdTime, qTLog.status, qTLog.venderRepTime,
                        qTLog.businessRepTime, qTLog.visitAddr, qTLog.debugreplayFlag,
                        qTLog.logType, qTLog.logNode, qTLog.logHeader,qTLog.publishId)).from(qTLog)
                .where(list.toArray(new Predicate[list.size()]))
                .orderBy(orderList.toArray(new OrderSpecifier[orderList.size()])).limit(pageSize).fetch();

        //?????????????????? ????????????????????????????????????
        if(CollectionUtils.isNotEmpty(tLogList) && isNext==0 && !StringUtils.equals("0",id)){
            tLogList=tLogList.stream().sorted(Comparator.comparing(TLog::getId).reversed())
                    .collect(Collectors.toList());
        }

        return tLogList;
    }

    private void combineResult(QueryResults<TLog> tLogQueryResults, List<TBusinessInterface> btResults, List<TSysPublish> pubResults) {
        List<TLog> results = tLogQueryResults.getResults();
        //????????????????????????id???key???map
        Map<String, TBusinessInterface> btMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(btResults)) {
            btMap = btResults.stream().collect(Collectors.toMap(TBusinessInterface::getId, Function.identity()));
        }

        //????????????????????????id???key???map
        Map<String, TSysPublish> pubMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(pubResults)) {
            pubMap = pubResults.stream().collect(Collectors.toMap(TSysPublish::getId, Function.identity()));
        }

        //????????????
        for (TLog result : results) {
            if (!btMap.isEmpty()) {
                TBusinessInterface tBusinessInterface = btMap.get(result.getBusinessInterfaceId());
                if(tBusinessInterface==null) continue;
                result.setInterfaceId(tBusinessInterface.getRequestInterfaceId());
                result.setInterfaceName(tBusinessInterface.getInterfaceName());
                result.setInterfaceUrl(tBusinessInterface.getInterfaceUrl());
                result.setExcErrOrder(tBusinessInterface.getExcErrOrder());
                result.setRequestInterfaceId(tBusinessInterface.getRequestInterfaceId());
                result.setReplayFlag(tBusinessInterface.getReplayFlag());
                result.setBusinessInterfaceName(tBusinessInterface.getBusinessInterfaceName());
                result.setRegId(tBusinessInterface.getSysRegistryId());
                result.setRegistryName(tBusinessInterface.getRegistryName());
                result.setRegSysName(tBusinessInterface.getSysName());
                result.setRegSysId(tBusinessInterface.getSysRegistryId());
            }

            if (!pubMap.isEmpty()) {
                TSysPublish tSysPublish = pubMap.get(result.getPublishId());
                if(tSysPublish==null) continue;
                result.setPublishId(tSysPublish.getId());
                result.setPublishName(tSysPublish.getPublishName());
                result.setPublishSysId(tSysPublish.getSysId());
                result.setPublishSysName(tSysPublish.getSysName());
            }
        }
    }

    @ApiOperation(value = "????????????????????????")
    @GetMapping("/logInfo")
    public ResultDto<TLog> logInfo(String id,@RequestParam(required = true) String createdTime) {
        if (StringUtils.isEmpty(id)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "?????????????????????id??????");
        }
        Date createTime=null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            createTime = sdf.parse(createdTime);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        // ????????????
        TLog tLog = shardingSqlQueryFactory.select(Projections.bean(TLog.class, qTLog.id, qTLog.createdTime, qTLog.status,
                        qTLog.venderRepTime, qTLog.businessRepTime, qTLog.visitAddr, qTLog.businessReq, qTLog.venderReq,
                        qTLog.businessRep, qTLog.venderRep, qTLog.debugreplayFlag,qTLog.businessInterfaceId,qTLog.publishId,
                        qTLog.logType, qTLog.logNode, qTLog.logHeader,qTLog.ipAddress,qTLog.groupName))
                .from(qTLog)
                .where(qTLog.id.eq(Long.valueOf(id)).and(qTLog.createdTime.eq(createTime)))
                .fetchFirst();

            //???????????????????????????
            TBusinessInterface tBusinessInterface = sqlQueryFactory.select(Projections.bean(TBusinessInterface.class, qTBusinessInterface.id,
                            qTBusinessInterface.requestInterfaceId,
                            qTInterface.interfaceName, qTInterface.interfaceUrl))
                    .from(qTBusinessInterface)
                    .leftJoin(qTInterface).on(qTBusinessInterface.requestInterfaceId.eq(qTInterface.id))
                    .where(qTBusinessInterface.id.eq(tLog.getBusinessInterfaceId())).fetchFirst();

        if(tBusinessInterface!=null){
            tLog.setInterfaceId(tBusinessInterface.getRequestInterfaceId());
            tLog.setInterfaceName(tBusinessInterface.getInterfaceName());
            tLog.setInterfaceUrl(tBusinessInterface.getInterfaceUrl());
            tLog.setRequestInterfaceId(tBusinessInterface.getRequestInterfaceId());
        }

        TSysPublish tSysPublish = sqlQueryFactory.select(Projections.bean(TSysPublish.class, qTSysPublish.id,
                        qTSysPublish.publishName,
                        qTSys.id.as("sysId"), qTSys.sysName))
                .from(qTSysPublish)
                .leftJoin(qTSys).on(qTSysPublish.sysId.eq(qTSys.id))
                .where(qTSysPublish.id.eq(tLog.getPublishId()))
                .fetchFirst();

        if(tSysPublish!=null){
            tLog.setPublishId(tSysPublish.getId());
            tLog.setPublishName(tSysPublish.getPublishName());
            tLog.setPublishSysId(tSysPublish.getSysId());
            tLog.setPublishSysName(tSysPublish.getSysName());
        }

        // ???????????????????????????
        String businessRep = decryptAndFilterSensitive(tLog.getBusinessRep());
        if (StringUtils.isNotBlank(businessRep)) {
            businessRep = businessRep.length() > 5000 ? businessRep.substring(0, 5000) + "......" : businessRep;
        }
        tLog.setBusinessRep(businessRep);
        String venderRep = decryptAndFilterSensitive(tLog.getVenderRep());
        if (StringUtils.isNotBlank(venderRep)) {
            venderRep = venderRep.length() > 5000 ? venderRep.substring(0, 5000) + "......" : venderRep;
        }
        tLog.setVenderRep(venderRep);

        tLog.setBusinessReq(decryptAndFilterSensitive(tLog.getBusinessReq()));
        tLog.setVenderReq(decryptAndFilterSensitive(tLog.getVenderReq()));
        if(StringUtils.isNotEmpty(tLog.getGroupName())) {
            tLog.setIpAddress(tLog.getIpAddress()+"/"+tLog.getGroupName());
        }
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "????????????????????????!", tLog);
    }


    @ApiOperation(value = "??????????????????")
    @GetMapping(path = "/downloadLogInfo")
    public void downloadLogInfo(String id, HttpServletRequest request, HttpServletResponse response,
                                @RequestParam(required = true) String createdTime) {
        Date createTime=null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            createTime = sdf.parse(createdTime);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (StringUtils.isEmpty(id)) {
            throw new RuntimeException("?????????????????????id??????!");
        }
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String fileName = "log_info_detail" + dateStr + ".txt";
        // ????????????
        TLog tLog = shardingSqlQueryFactory.select(Projections.bean(TLog.class, qTLog.id, qTLog.createdTime, qTLog.status,
                qTLog.venderRepTime, qTLog.businessRepTime, qTLog.visitAddr, qTLog.businessReq, qTLog.venderReq,
                qTLog.businessRep, qTLog.venderRep)).from(qTLog).where(qTLog.id.eq(Long.valueOf(id)).and(qTLog.createdTime.eq(createTime))).fetchFirst();
        // ???????????????????????????
        String businessRep = decryptAndFilterSensitive(tLog.getBusinessRep());
        tLog.setBusinessRep(businessRep);
        String venderRep = decryptAndFilterSensitive(tLog.getVenderRep());
        tLog.setVenderRep(venderRep);
        tLog.setBusinessReq(decryptAndFilterSensitive(tLog.getBusinessReq()));
        tLog.setVenderReq(decryptAndFilterSensitive(tLog.getVenderReq()));

        StringBuilder data = new StringBuilder();
        data.append("??????????????????:").append(tLog.getVisitAddr()).append("\r\n");
        data.append("???????????????:").append(tLog.getBusinessReq()).append("\r\n");
        data.append("???????????????:").append(tLog.getBusinessRep()).append("\r\n");
        data.append("??????????????????:").append(tLog.getVenderReq()).append("\r\n");
        data.append("??????????????????:").append(tLog.getVenderRep()).append("\r\n");
        try {
            OutputStream ouputStream = response.getOutputStream();
            response.setContentType("application/csv;charset=UTF-8");
            request.setCharacterEncoding("UTF-8");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName);
            ouputStream.write(data.toString().getBytes("UTF-8"));
            ouputStream.flush();
            ouputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @ApiOperation(value = "??????????????????")
    @PostMapping("/interfaceDebugRedo")
    public ResultDto<String> interfaceDebugRedo(@RequestBody DebugDto dto) {
        if (StringUtils.isEmpty(dto.getIds())) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "??????????????????ids??????");
        }

        if (CollectionUtils.isEmpty(dto.getCreatedTimeList())) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "??????????????????createdTimeList??????");
        }
        List<Date> createTimeList=new ArrayList<>();
        try {
            for (String date : dto.getCreatedTimeList()) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                createTimeList.add(sdf.parse(date));
            }


        } catch (ParseException e) {
            e.printStackTrace();
        }

        Map<String, String> headerMap = new HashMap<>();
        String loginUrlPrefix = niFiRequestUtil.getInterfaceDebugWithAuth();
        if ("1".equals(dto.getAuthFlag())) {
            headerMap.putAll(niFiRequestUtil.interfaceAuthLogin(loginUrlPrefix, false));
        }

		String[] idArrays = dto.getIds().split(",");
		if(idArrays != null && idArrays.length > 0) {
			try {
				Long[] realIds = new Long[idArrays.length];
				for(int i = 0 ; i < idArrays.length ; i++) {
					realIds[i] = Long.valueOf(idArrays[i]);
				}
				List<TLog> logs = shardingSqlQueryFactory.select(qTLog).from(qTLog).where(qTLog.id.in(realIds)
                        .and(qTLog.createdTime.in(createTimeList))).fetch();
				for(TLog tlog: logs) {
					if(tlog.getDebugreplayFlag() == 0 || tlog.getDebugreplayFlag() == 2) {
						headerMap.put("Debugreplay-Flag", "2");
					}else {
						headerMap.put("Debugreplay-Flag", "3");
					}

                    //lastMap????????????headerMap
                    Map lastMap = new HashMap();
                    lastMap.putAll(headerMap);
                    //log_header
                    if (StringUtils.isNotEmpty(tlog.getLogHeader())) {
                        JSONObject joHeader = JSONObject.parseObject(tlog.getLogHeader());
                        joHeader.forEach((key, val) -> lastMap.put(key, val == null ? null : val.toString()));
                    }
                    String format = decryptAndFilterSensitive(tlog.getBusinessReq());
                    if ("0".equals(tlog.getBusinessInterfaceId())) {
                        if (StringUtils.isBlank(tlog.getVisitAddr())) {
                            continue;
                        }
                        String wsdlUrl = tlog.getVisitAddr();
                        List<String> wsOperationNames = PlatformUtil.getWsdlOperationNames(wsdlUrl);
                        if (wsOperationNames == null || wsOperationNames.size() == 0) {
                            continue;
                        }
                        String methodName = wsOperationNames.get(0);

                        PlatformUtil.invokeWsServiceWithOrigin(wsdlUrl, methodName, format, lastMap, readTimeout);
                        continue;
                    }
                    niFiRequestUtil.interfaceDebug(format, lastMap, "1".equals(dto.getAuthFlag()));
                }
            } catch (Exception e) {
                logger.error("????????????????????????????????????! MSG:{}", ExceptionUtil.dealException(e));
                return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "??????????????????");
            }
        }
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "??????????????????");
    }

}
