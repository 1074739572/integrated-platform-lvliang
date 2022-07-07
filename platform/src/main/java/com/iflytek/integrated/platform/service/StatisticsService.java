package com.iflytek.integrated.platform.service;

import com.alibaba.fastjson.JSON;
import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.common.dto.TableData;
import com.iflytek.integrated.common.utils.ExceptionUtil;
import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.platform.dto.CallStatisticsDTO;
import com.iflytek.integrated.platform.dto.TotalStatisticsDTO;
import com.iflytek.integrated.platform.entity.TBusinessInterface;
import com.iflytek.integrated.platform.entity.TServerStatisticsDay;
import com.iflytek.integrated.platform.entity.TSysRegistry;
import com.iflytek.integrated.platform.utils.PlatformUtil;
import com.iflytek.medicalboot.core.id.BatchUidService;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.StringPath;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.iflytek.integrated.platform.entity.QTType.qTType;
import static com.iflytek.integrated.platform.entity.QTInterface.qTInterface;
import static com.iflytek.integrated.platform.entity.QTServerStatisticsDay.qTServerStatisticsDay;
import static com.iflytek.integrated.platform.entity.QTVendor.qtVendor;

@Slf4j
@Api(tags = "服务概览")
@RestController
@RequestMapping("/{version}/pt/statistics")
public class StatisticsService extends BaseService<TServerStatisticsDay, String, StringPath> {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsService.class);

    public StatisticsService() {
        super(qTServerStatisticsDay, qTServerStatisticsDay.id);
    }

    @ApiOperation(value = "查询今日和累计类的数据")
    @GetMapping("/getStatistics")
    public ResultDto<TotalStatisticsDTO> getStatistics() {
        try {
            //查询累计数据
            List<TServerStatisticsDay> queryResults = sqlQueryFactory
                    .select(Projections
                            .bean(TServerStatisticsDay.class, qTServerStatisticsDay.serverId,
                                    qTServerStatisticsDay.typeId, qTServerStatisticsDay.vendorId,
                                    qTServerStatisticsDay.currRequestTotal.sum().as(qTServerStatisticsDay.currRequestTotal),
                                    qTServerStatisticsDay.currRequestOkTotal.sum().as(qTServerStatisticsDay.currRequestOkTotal),
                                    qTServerStatisticsDay.currResponseTimeTotal.sum().as(qTServerStatisticsDay.currResponseTimeTotal),
                                    qTInterface.interfaceName.max().as(qTInterface.interfaceName),
                                    qTType.typeName.max().as(qTType.typeName),
                                    qtVendor.vendorName.max().as(qtVendor.vendorName)))
                    .from(qTServerStatisticsDay)
                    .leftJoin(qTInterface).on(qTInterface.id.eq(qTServerStatisticsDay.serverId))
                    .leftJoin(qTType).on(qTType.id.eq(qTServerStatisticsDay.typeId))
                    .leftJoin(qtVendor).on(qtVendor.id.eq(qTServerStatisticsDay.vendorId))
                    .groupBy(qTServerStatisticsDay.serverId, qTServerStatisticsDay.typeId, qTServerStatisticsDay.vendorId)
                    .fetch();
            if (queryResults.isEmpty()) {
                return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取统计数据成功!", null);
            }

            TotalStatisticsDTO statisticsDto = new TotalStatisticsDTO();

            //获得累计数据
            getTotal(queryResults, statisticsDto);

            //服务调用次数
            List<CallStatisticsDTO> serverCallList = serverCall(queryResults);

            statisticsDto.setServerCall(serverCallList);
            //取TOP10 按照访问量降序排序 取前10条
            List<CallStatisticsDTO> topTenServerCall = serverCallList.stream()
                    .sorted(Comparator.comparing(CallStatisticsDTO::getIndexCount).reversed())
                    .limit(10)
                    .collect(Collectors.toList());

            statisticsDto.setServerCallTopTen(topTenServerCall);
            //服务调用厂商TOP10
            statisticsDto.setVendorTopTen(toptenVendorCall(queryResults));

            //服务类型访问次数统计
            statisticsDto.setTypeTopTen(typeCall(queryResults));

            //服务调用速度TOP10
            statisticsDto.setSpeedTopTen(toptenSpeedCall(queryResults));

            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取统计数据成功!", statisticsDto);
        } catch (BeansException e) {
            logger.error("获取服务注册列表失败! MSG:{}", ExceptionUtil.dealException(e));
            e.printStackTrace();
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "获取统计数据失败!");
        }
    }

    private List<CallStatisticsDTO> toptenSpeedCall(List<TServerStatisticsDay> queryResults) {
        List<CallStatisticsDTO> list = new ArrayList<>();
        //按照服务分组
        Map<String, List<TServerStatisticsDay>> map = queryResults.stream().collect(Collectors.groupingBy(TServerStatisticsDay::getInterfaceName));
        for (Map.Entry<String, List<TServerStatisticsDay>> entry : map.entrySet()) {
            Long serverTimesTotal = 0L;
            CallStatisticsDTO dto = new CallStatisticsDTO();
            for (TServerStatisticsDay tServerStatisticsDay : entry.getValue()) {
                serverTimesTotal += tServerStatisticsDay.getCurrResponseTimeTotal();
            }
            dto.setName(entry.getKey());
            dto.setIndexCount(serverTimesTotal);
            list.add(dto);
        }

        return list.stream()
                .sorted(Comparator.comparing(CallStatisticsDTO::getIndexCount).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    private List<CallStatisticsDTO> typeCall(List<TServerStatisticsDay> queryResults) {
        List<CallStatisticsDTO> list = new ArrayList<>();
        //按照服务分组
        Map<String, List<TServerStatisticsDay>> map = queryResults.stream().collect(Collectors.groupingBy(TServerStatisticsDay::getTypeName));
        for (Map.Entry<String, List<TServerStatisticsDay>> entry : map.entrySet()) {
            Long serverTotal = 0L;
            CallStatisticsDTO dto = new CallStatisticsDTO();
            for (TServerStatisticsDay tServerStatisticsDay : entry.getValue()) {
                serverTotal += tServerStatisticsDay.getCurrRequestTotal();
            }
            dto.setName(entry.getKey());
            dto.setIndexCount(serverTotal);
            list.add(dto);
        }

        return list;
    }

    private List<CallStatisticsDTO> toptenVendorCall(List<TServerStatisticsDay> queryResults) {
        List<CallStatisticsDTO> list = new ArrayList<>();
        //按照服务分组
        Map<String, List<TServerStatisticsDay>> map = queryResults.stream().collect(Collectors.groupingBy(TServerStatisticsDay::getVendorName));
        for (Map.Entry<String, List<TServerStatisticsDay>> entry : map.entrySet()) {
            Long serverTotal = 0L;
            CallStatisticsDTO dto = new CallStatisticsDTO();
            for (TServerStatisticsDay tServerStatisticsDay : entry.getValue()) {
                serverTotal += tServerStatisticsDay.getCurrRequestTotal();
            }
            dto.setName(entry.getKey());
            dto.setIndexCount(serverTotal);
            list.add(dto);
        }

        return list.stream()
                .sorted(Comparator.comparing(CallStatisticsDTO::getIndexCount).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    private List<CallStatisticsDTO> serverCall(List<TServerStatisticsDay> queryResults) {
        List<CallStatisticsDTO> list = new ArrayList<>();
        //按照服务分组
        Map<String, List<TServerStatisticsDay>> map = queryResults.stream().collect(Collectors.groupingBy(TServerStatisticsDay::getInterfaceName));
        for (Map.Entry<String, List<TServerStatisticsDay>> entry : map.entrySet()) {
            Long serverTotal = 0L;
            CallStatisticsDTO dto = new CallStatisticsDTO();
            for (TServerStatisticsDay tServerStatisticsDay : entry.getValue()) {
                serverTotal += tServerStatisticsDay.getCurrRequestTotal();
            }
            dto.setName(entry.getKey());
            dto.setIndexCount(serverTotal);
            list.add(dto);
        }
        return list;
    }

    private void getTotal(List<TServerStatisticsDay> queryResults, TotalStatisticsDTO dto) {
        Long requestTotal = 0L;
        Long requestOkTotal = 0L;
        Long requestTimesTotal = 0L;
        for (TServerStatisticsDay queryResult : queryResults) {
            //服务请求总量
            requestTotal += queryResult.getCurrRequestTotal();
            //服务请求成功总量
            requestOkTotal += queryResult.getCurrRequestOkTotal();
            //服务响应时间
            requestTimesTotal += queryResult.getCurrResponseTimeTotal();
        }

        //请求成功率
        BigDecimal okRate = new BigDecimal(requestOkTotal.longValue()).divide(new BigDecimal(requestTotal.longValue()), 2, RoundingMode.HALF_UP);

        //访问失败
        Long failTotal = requestTotal - requestOkTotal;

        //平均响应时长(ms)
        BigDecimal avgTime = new BigDecimal(requestTimesTotal.longValue()).divide(new BigDecimal(requestTotal.longValue()), 2, RoundingMode.HALF_UP);

        //查询服务总数


        //查询请求方系统总量
        dto.setServerRequestTotal(requestTotal.toString());
        dto.setServerRequestOkTotal(requestOkTotal.toString());
        dto.setOkRate(okRate.toPlainString());
        dto.setServerRequestFailTotal(failTotal.toString());
        dto.setAvgResponseTime(avgTime.toPlainString());
    }

    @ApiOperation(value = "查询近7天")
    @GetMapping("/getLastSevenDay")
    public ResultDto<List<CallStatisticsDTO>> getLastSevenDayStatistics() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            //获取当前日期和7天前日期
            Calendar c = Calendar.getInstance();
            Date now = c.getTime();
            String nowStr = sdf.format(now);
            c.add(Calendar.DATE, -6);
            String sevenDay = sdf.format(c.getTime());

            //获得近7天的数据
            List<String> sevenDayList = new ArrayList<>();
            Calendar c1 = Calendar.getInstance();
            sevenDayList.add(sdf.format(c1.getTime()).substring(5));
            for (int i = 0; i < 6; i++) {
                c1.add(Calendar.DATE, -1);
                sevenDayList.add(sdf.format(c1.getTime()).substring(5));
            }

            //查询累计数据
            List<TServerStatisticsDay> queryResults = sqlQueryFactory
                    .select(Projections
                            .bean(TServerStatisticsDay.class, qTServerStatisticsDay.dt,
                                    qTServerStatisticsDay.currRequestTotal.sum().as(qTServerStatisticsDay.currRequestTotal)))
                    .from(qTServerStatisticsDay)
                    .leftJoin(qTInterface).on(qTInterface.id.eq(qTServerStatisticsDay.serverId))
                    .leftJoin(qTType).on(qTType.id.eq(qTServerStatisticsDay.typeId))
                    .leftJoin(qtVendor).on(qtVendor.id.eq(qTServerStatisticsDay.vendorId))
                    .where(qTServerStatisticsDay.dt.between(sdf.parse(sevenDay), sdf.parse(nowStr)))
                    .groupBy(qTServerStatisticsDay.dt)
                    .fetch();
            if (queryResults.isEmpty()) {
                return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取统计数据成功!", null);
            }

            List<CallStatisticsDTO> list = new ArrayList<>();
            for (TServerStatisticsDay queryResult : queryResults) {
                CallStatisticsDTO dto = new CallStatisticsDTO();
                String dateStr = sdf.format(queryResult.getDt()).substring(5);
                dto.setName(dateStr);
                sevenDayList.remove(dateStr);
                dto.setIndexCount(queryResult.getCurrRequestTotal());
                list.add(dto);
            }

            //没有匹配上的日期指标计数0
            if(CollectionUtils.isNotEmpty(sevenDayList)){
                for (String s : sevenDayList) {
                    CallStatisticsDTO dto = new CallStatisticsDTO();
                    dto.setName(s);
                    dto.setIndexCount(0L);
                    list.add(dto);
                }
            }

            //最后按照日期升序排序
            list=list.stream().sorted(Comparator.comparing(CallStatisticsDTO::getName)).collect(Collectors.toList());

            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取统计数据成功!", list);
        } catch (Exception e) {
            logger.error("获取服务注册列表失败! MSG:{}", ExceptionUtil.dealException(e));
            e.printStackTrace();
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "获取统计数据失败!");
        }
    }
}
