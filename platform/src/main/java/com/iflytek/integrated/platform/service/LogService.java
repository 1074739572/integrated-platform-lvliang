package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.common.Constant;
import com.iflytek.integrated.common.ResultDto;
import com.iflytek.integrated.common.TableData;
import com.iflytek.integrated.common.utils.ExceptionUtil;
import com.iflytek.integrated.platform.dto.InterfaceMonitorDto;
import com.iflytek.integrated.platform.entity.QTInterfaceMonitor;
import com.iflytek.integrated.platform.entity.TLog;
import com.iflytek.integrated.platform.utils.Utils;
import com.iflytek.medicalboot.core.dto.PageRequest;
import com.iflytek.medicalboot.core.querydsl.QuerydslService;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.core.types.dsl.*;
import com.querydsl.sql.SQLExpressions;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

import static com.iflytek.integrated.platform.entity.QTBusinessInterface.qTBusinessInterface;
import static com.iflytek.integrated.platform.entity.QTFunction.qTFunction;
import static com.iflytek.integrated.platform.entity.QTLog.qTLog;
import static com.iflytek.integrated.platform.entity.QTInterfaceMonitor.qTInterfaceMonitor;
import static com.iflytek.integrated.platform.entity.QTPlatform.qTPlatform;
import static com.iflytek.integrated.platform.entity.QTProduct.qTProduct;
import static com.iflytek.integrated.platform.entity.QTProductFunctionLink.qTProductFunctionLink;
import static com.iflytek.integrated.platform.entity.QTProject.qTProject;
import static com.iflytek.integrated.platform.entity.QTVendorConfig.qTVendorConfig;

/**
 * @author czzhan
 * @version 1.0
 * @date 2020/12/20 16:46
 */
@Slf4j
@Api(tags = "服务监控")
@RestController
@RequestMapping("/{version}/pt/interfaceMonitor")
public class LogService extends QuerydslService<TLog, Long, TLog, NumberPath<Long>, PageRequest<TLog>> {
    private static final Logger logger = LoggerFactory.getLogger(LogService.class);

    public LogService(){
        super(qTLog, qTLog.id);
    }

    @ApiOperation(value = "查看服务监控列表")
    @GetMapping("/getListPage")
    public ResultDto getListPage(String projectId, String platFormId, String productId, String status,
                                  @RequestParam(defaultValue = "1")Integer pageNo,
                                  @RequestParam(defaultValue = "10")Integer pageSize) {
        try {
            //查询条件
            ArrayList<Predicate> list = new ArrayList<>();
            //判断条件是否为空
            if(StringUtils.isNotBlank(productId)){
                list.add(qTProduct.id.eq(productId));
            }
            //先合并t_interface_monitor，再根据三合一结果进行查询
            String q = "queryMonitor";
            StringPath queryLabel = Expressions.stringPath(q);
            QTInterfaceMonitor monitor = new QTInterfaceMonitor(q);
            SubQueryExpression query = SQLExpressions.select(
                    qTInterfaceMonitor.id,
                    qTInterfaceMonitor.status.max().as("status"),
                    qTInterfaceMonitor.successCount.sum().as("SUCCESS_COUNT"),
                    qTInterfaceMonitor.errorCount.sum().as("ERROR_COUNT"),
                    qTInterfaceMonitor.projectId,
                    qTInterfaceMonitor.platformId,
                    qTInterfaceMonitor.productFunctionLinkId,
                    qTInterfaceMonitor.createdTime
            ).from(qTInterfaceMonitor)
            .rightJoin(qTBusinessInterface).on(qTBusinessInterface.id.eq(qTInterfaceMonitor.businessInterfaceId)
                    .and(qTBusinessInterface.productFunctionLinkId.eq(qTInterfaceMonitor.productFunctionLinkId)))
            .rightJoin(qTVendorConfig).on(qTVendorConfig.id.eq(qTBusinessInterface.vendorConfigId)
                    .and(qTVendorConfig.platformId.eq(qTInterfaceMonitor.platformId)))
            .where(qTInterfaceMonitor.id.isNotNull())
            .groupBy(qTInterfaceMonitor.platformId,qTInterfaceMonitor.projectId,qTInterfaceMonitor.productFunctionLinkId)
            .orderBy(qTInterfaceMonitor.id.asc());

            //按条件筛选
            if(StringUtils.isNotBlank(projectId)){
                list.add(monitor.projectId.eq(projectId));
            }
            if(StringUtils.isNotBlank(platFormId)){
                list.add(monitor.platformId.eq(platFormId));
            }
            if(StringUtils.isNotBlank(status)){
                list.add(monitor.status.eq(status));
            }
            //根据结果查询
            QueryResults<InterfaceMonitorDto> queryResults = sqlQueryFactory.select(
                    Projections.bean(InterfaceMonitorDto.class,
                            monitor.id,
                            monitor.status,
                            monitor.successCount,
                            monitor.errorCount,
                            qTProject.projectName,
                            qTPlatform.platformName,
                            qTProduct.productName,
                            qTFunction.functionName
                    )).from(query,queryLabel).leftJoin(qTProject).on(qTProject.id.eq(monitor.projectId))
                    .leftJoin(qTPlatform).on(qTPlatform.id.eq(monitor.platformId))
                    .leftJoin(qTProductFunctionLink).on(qTProductFunctionLink.id.eq(monitor.productFunctionLinkId))
                    .leftJoin(qTProduct).on(qTProduct.id.eq(qTProductFunctionLink.productId))
                    .leftJoin(qTFunction).on(qTFunction.id.eq(qTProductFunctionLink.functionId))
                    .where(list.toArray(new Predicate[list.size()]))
                    .limit(pageSize)
                    .offset((pageNo - 1) * pageSize)
                    .orderBy(monitor.createdTime.desc())
                    .fetchResults();
            //分页
            TableData<InterfaceMonitorDto> tableData = new TableData<>(queryResults.getTotal(), queryResults.getResults());
            return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "", tableData);
        }catch (Exception e){
            logger.error("查看服务监控列表失败!", ExceptionUtil.dealException(e));
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "", ExceptionUtil.dealException(e));
        }
    }

    @ApiOperation(value = "查看日志详细列表")
    @GetMapping("/logInfo")
    public ResultDto logInfo(String interfaceMonitorId,String status,String visitAddr,
                             @RequestParam(defaultValue = "1")Integer pageNo,
                             @RequestParam(defaultValue = "10")Integer pageSize){
        //查询条件
        ArrayList<Predicate> list = new ArrayList<>();
        if(StringUtils.isEmpty(interfaceMonitorId)){
            return new ResultDto(Constant.ResultCode.ERROR_CODE,"","获取日志详细列表，id必传");
        }
        list.add(qTInterfaceMonitor.id.eq(Long.valueOf(interfaceMonitorId)));
        if(StringUtils.isNotBlank(status)){
            list.add(qTLog.status.eq(status));
        }
        //模糊查询接口地址
        if(StringUtils.isNotBlank(visitAddr)){
            list.add(qTLog.visitAddr.like(Utils.createFuzzyText(visitAddr)));
        }
        QueryResults<TLog> queryResults = sqlQueryFactory.select(
                Projections.bean(TLog.class,
                        qTLog.id,
                        qTLog.createdTime,
                        qTLog.status,
                        qTLog.venderRepTime,
                        qTLog.businessRepTime,
                        qTLog.visitAddr,
                        qTLog.businessReq,
                        qTLog.venderReq,
                        qTLog.businessRep,
                        qTLog.venderRep
                )
            ).from(qTLog)
            .leftJoin(qTInterfaceMonitor).on(qTInterfaceMonitor.projectId.eq(qTLog.projectId)
                .and(qTInterfaceMonitor.platformId.eq(qTLog.platformId)
                .and(qTInterfaceMonitor.productFunctionLinkId.eq(qTLog.productFunctionLinkId))))
            .where(list.toArray(new Predicate[list.size()]))
            .limit(pageSize)
            .offset((pageNo - 1) * pageSize)
            .orderBy(qTLog.createdTime.desc())
            .fetchResults();
        //分页
        TableData<TLog> tableData = new TableData<>(queryResults.getTotal(), queryResults.getResults());
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "",tableData);
    }
}
