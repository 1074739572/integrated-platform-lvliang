package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.common.utils.ExceptionUtil;
import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.platform.dto.CallStatisticsDTO;
import com.iflytek.integrated.platform.dto.TodayStatisticsDTO;
import com.iflytek.integrated.platform.dto.TotalStatisticsDTO;
import com.iflytek.integrated.platform.entity.TServerStatisticsDay;
import com.iflytek.integrated.platform.entity.TServerStatisticsHour;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.StringPath;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.iflytek.integrated.platform.entity.QTInterface.qTInterface;
import static com.iflytek.integrated.platform.entity.QTServerStatisticsDay.qTServerStatisticsDay;
import static com.iflytek.integrated.platform.entity.QTServerStatisticsHour.qtServerStatisticsHour;
import static com.iflytek.integrated.platform.entity.QTSysRegistry.qTSysRegistry;
import static com.iflytek.integrated.platform.entity.QTType.qTType;
import static com.iflytek.integrated.platform.entity.QTVendor.qtVendor;

@Slf4j
@Api(tags = "服务概览")
@RestController
@RequestMapping("/{version}/pt/statistics")
public class StatisticsService extends BaseService<TServerStatisticsDay, String, StringPath> {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsService.class);

    public StatisticsService() {
        super(qTServerStatisticsDay, null);
    }

    @ApiOperation(value = "查询今日统计的数据")
    @GetMapping("/getTodayStatistics")
    public ResultDto<TodayStatisticsDTO> getTodayStatistics() {
        try {
            //统计结果对象
            TodayStatisticsDTO dto=new TodayStatisticsDTO();
            Long requestTotal = 0L;
            Long requestOkTotal = 0L;
            Long requestTimesTotal = 0L;
            Long exceptionServerTotal = 0L;
            BigDecimal okRate=BigDecimal.ZERO;
            BigDecimal failRate=BigDecimal.ZERO;
            Long failTotal=0L;
            BigDecimal avgTime=BigDecimal.ZERO;

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            //获取当前日期
            Calendar c = Calendar.getInstance();
            Date now = c.getTime();
            String nowStr = sdf.format(now);
            //查询当日数据
            List<TServerStatisticsDay> queryResults = sqlQueryFactory
                    .select(Projections
                            .bean(TServerStatisticsDay.class, qTServerStatisticsDay.serverId,
                                    qTServerStatisticsDay.currRequestTotal.sum().as(qTServerStatisticsDay.currRequestTotal),
                                    qTServerStatisticsDay.currRequestOkTotal.sum().as(qTServerStatisticsDay.currRequestOkTotal),
                                    qTServerStatisticsDay.currResponseTimeTotal.sum().as(qTServerStatisticsDay.currResponseTimeTotal))
                    )
                    .from(qTServerStatisticsDay)
                    .leftJoin(qTInterface).on(qTInterface.id.eq(qTServerStatisticsDay.serverId))
                    .where(qTServerStatisticsDay.dt.eq(sdf.parse(nowStr)))
                    .groupBy(qTServerStatisticsDay.serverId)
                    .fetch();
            if (!queryResults.isEmpty()) {
                for (TServerStatisticsDay queryResult : queryResults) {
                    //服务请求总量
                    requestTotal += queryResult.getCurrRequestTotal();
                    //服务请求成功总量
                    requestOkTotal += queryResult.getCurrRequestOkTotal();
                    //服务响应时间
                    requestTimesTotal += queryResult.getCurrResponseTimeTotal();
                    if (queryResult.getCurrRequestTotal() - queryResult.getCurrRequestOkTotal() > 0) {
                        //如果该服务存在失败的 则该服务异常
                        exceptionServerTotal += 1;
                    }
                }

                //请求成功率
                okRate = new BigDecimal(requestOkTotal.longValue()).divide(new BigDecimal(requestTotal.longValue()), 4, RoundingMode.HALF_UP);

                //请求失败率
                failRate=BigDecimal.ONE.subtract(okRate);

                //访问失败
                failTotal = requestTotal - requestOkTotal;

                //平均响应时长(ms)
                avgTime = new BigDecimal(requestTimesTotal.longValue()).divide(new BigDecimal(requestTotal.longValue()), 2, RoundingMode.HALF_UP);
            }

            //查询请求方系统总量
            dto.setServerRequestTotal(requestTotal.toString());
            dto.setServerRequestOkTotal(requestOkTotal.toString());
            dto.setOkRate(okRate.multiply(new BigDecimal(100)).setScale(2,RoundingMode.HALF_UP).toPlainString()+"%");
            dto.setFailRate(failRate.multiply(new BigDecimal(100)).setScale(2,RoundingMode.HALF_UP).toPlainString()+"%");
            dto.setServerRequestFailTotal(failTotal.toString());
            dto.setAvgResponseTime(avgTime.toPlainString());
            dto.setExceptionServerTotal(exceptionServerTotal.toString());
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取统计数据成功!", dto);
        } catch (Exception e) {
            logger.error("获取服务注册列表失败! MSG:{}", ExceptionUtil.dealException(e));
            e.printStackTrace();
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "获取统计数据失败!");
        }
    }

    @ApiOperation(value = "查询累计类的数据")
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
            statisticsDto.setTypeCall(typeCall(queryResults));

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
        Map<String, List<TServerStatisticsDay>> map = queryResults.stream().filter(e->!StringUtils.isEmpty(e.getInterfaceName())).collect(Collectors.groupingBy(TServerStatisticsDay::getInterfaceName));
        for (Map.Entry<String, List<TServerStatisticsDay>> entry : map.entrySet()) {
            Long serverTimesTotal = 0L;
            Long serverRequestTotal = 0L;
            CallStatisticsDTO dto = new CallStatisticsDTO();
            for (TServerStatisticsDay tServerStatisticsDay : entry.getValue()) {
                serverTimesTotal += tServerStatisticsDay.getCurrResponseTimeTotal();
                serverRequestTotal+=tServerStatisticsDay.getCurrRequestTotal();
            }
            dto.setName(entry.getKey());
            dto.setIndexCount(serverTimesTotal/serverRequestTotal);
            list.add(dto);
        }

        return list.stream()
                .sorted(Comparator.comparing(CallStatisticsDTO::getIndexCount))
                .limit(10)
                .collect(Collectors.toList());
    }

    private List<CallStatisticsDTO> typeCall(List<TServerStatisticsDay> queryResults) {
        List<CallStatisticsDTO> list = new ArrayList<>();
        //按照服务分组
        Map<String, List<TServerStatisticsDay>> map = queryResults.stream().filter(e->!StringUtils.isEmpty(e.getTypeName()))
                .collect(Collectors.groupingBy(TServerStatisticsDay::getTypeName));
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
        Map<String, List<TServerStatisticsDay>> map = queryResults.stream().filter(e->!StringUtils.isEmpty(e.getVendorName()))
                .collect(Collectors.groupingBy(TServerStatisticsDay::getVendorName));
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
        Map<String, List<TServerStatisticsDay>> map = queryResults.stream().filter(e->!StringUtils.isEmpty(e.getInterfaceName()))
                .collect(Collectors.groupingBy(TServerStatisticsDay::getInterfaceName));
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
        BigDecimal okRate = new BigDecimal(requestOkTotal.longValue()).divide(new BigDecimal(requestTotal.longValue()), 4, RoundingMode.HALF_UP);

        //请求失败率
        BigDecimal failRate=BigDecimal.ONE.subtract(okRate);

        //访问失败
        Long failTotal = requestTotal - requestOkTotal;

        //平均响应时长(ms)
        BigDecimal avgTime = new BigDecimal(requestTimesTotal.longValue()).divide(new BigDecimal(requestTotal.longValue()), 2, RoundingMode.HALF_UP);

        //查询服务总数
        Long serverCount = this.sqlQueryFactory.selectFrom(qTInterface).fetchCount();

        //查询请求方系统总量
        Long sysCount = sqlQueryFactory.selectFrom(qTSysRegistry).groupBy(qTSysRegistry.sysId).fetchCount();

        dto.setServerRequestTotal(requestTotal.toString());
        dto.setServerRequestOkTotal(requestOkTotal.toString());
        dto.setOkRate(okRate.multiply(new BigDecimal(100)).setScale(2,RoundingMode.HALF_UP).toPlainString()+"%");
        dto.setFailRate(failRate.multiply(new BigDecimal(100)).setScale(2,RoundingMode.HALF_UP).toPlainString()+"%");
        dto.setServerRequestFailTotal(failTotal.toString());
        dto.setAvgResponseTime(avgTime.toPlainString());
        dto.setServerTotal(serverCount.toString());
        dto.setRequestSysTotal(sysCount.toString());
    }

    @ApiOperation(value = "查询近7天")
    @GetMapping("/getLastSevenDay")
    public ResultDto<List<CallStatisticsDTO>> getLastSevenDayStatistics() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat sdfMh = new SimpleDateFormat("MM-dd");
            //获取当前日期和7天前日期
            Calendar c = Calendar.getInstance();
            Date now = c.getTime();
            String nowStr = sdf.format(now);
            c.add(Calendar.DATE, -6);
            String sevenDay = sdf.format(c.getTime());

            //获得近7天的数据
            List<CallStatisticsDTO> list = new ArrayList<>();
            CallStatisticsDTO dto = new CallStatisticsDTO();
            Calendar c1 = Calendar.getInstance();
            dto.setName(sdfMh.format(c1.getTime()));
            dto.setIndexCount(0L);
            list.add(dto);
            for (int i = 0; i < 6; i++) {
                dto = new CallStatisticsDTO();
                c1.add(Calendar.DATE, -1);
                dto.setName(sdfMh.format(c1.getTime()));
                list.add(dto);
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
            if (!queryResults.isEmpty()) {
                Map<String, TServerStatisticsDay> map = queryResults.stream().collect(Collectors.toMap(e->sdfMh.format(e.getDt()), Function.identity()));
                for (CallStatisticsDTO sto : list) {
                    TServerStatisticsDay tServerStatisticsDay = map.get(sto.getName());
                    sto.setIndexCount(tServerStatisticsDay==null?0L:tServerStatisticsDay.getCurrRequestTotal());
                }
            }
            //最后按照日期升序排序
            list = list.stream().sorted(Comparator.comparing(CallStatisticsDTO::getName)).collect(Collectors.toList());

            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取统计数据成功!", list);
        } catch (Exception e) {
            logger.error("获取服务注册列表失败! MSG:{}", ExceptionUtil.dealException(e));
            e.printStackTrace();
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "获取统计数据失败!");
        }
    }

    @ApiOperation(value = "查询近24小时")
    @GetMapping("/getEveryHour")
    public ResultDto<List<CallStatisticsDTO>> getEveryHour() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:00");
            SimpleDateFormat sdfMh = new SimpleDateFormat("MM-dd HH:00");
            //获取当前日期和7天前日期
            Calendar c = Calendar.getInstance();
            Date now = c.getTime();
            String nowStr = sdf.format(now);
            c.add(Calendar.HOUR, -23);
            String hourDay = sdf.format(c.getTime());

            //获得近24小时的数据
            List<CallStatisticsDTO> list = new ArrayList<>();
            CallStatisticsDTO dto = new CallStatisticsDTO();
            Calendar c1 = Calendar.getInstance();
            dto.setName(sdfMh.format(c1.getTime()));
            dto.setIndexCount(0L);
            list.add(dto);
            for (int i = 0; i < 23; i++) {
                dto = new CallStatisticsDTO();
                c1.add(Calendar.HOUR, -1);
                dto.setName(sdfMh.format(c1.getTime()));
                dto.setIndexCount(0L);
                list.add(dto);
            }

            //查询累计数据
            List<TServerStatisticsHour> queryResults = sqlQueryFactory
                    .select(Projections
                            .bean(TServerStatisticsHour.class, qtServerStatisticsHour.dt,
                                    qtServerStatisticsHour.serverRequestTotal))
                    .from(qtServerStatisticsHour)
                    .where(qtServerStatisticsHour.dt.between(sdf.parse(hourDay), sdf.parse(nowStr)))
                    .fetch();
            if (!queryResults.isEmpty()) {
                Map<String, TServerStatisticsHour> map = queryResults.stream().collect(Collectors.toMap(e->sdfMh.format(e.getDt()), Function.identity()));
                for (CallStatisticsDTO sto : list) {
                    TServerStatisticsHour tServerStatisticsHour = map.get(sto.getName());
                    sto.setIndexCount(tServerStatisticsHour==null?0L:tServerStatisticsHour.getServerRequestTotal());
                }
            }

            //最后按照日期升序排序
            list = list.stream().sorted(Comparator.comparing(CallStatisticsDTO::getName)).collect(Collectors.toList());

            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取统计数据成功!", list);
        } catch (Exception e) {
            logger.error("获取服务注册列表失败! MSG:{}", ExceptionUtil.dealException(e));
            e.printStackTrace();
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "获取统计数据失败!");
        }
    }
}
