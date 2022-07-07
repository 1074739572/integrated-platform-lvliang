package com.iflytek.integrated.platform.dto;

import lombok.Data;

import java.util.List;

/**
 * 累计类统计DTO
 */
@Data
public class TodayStatisticsDTO {
    /**
     * 服务请求总量
     */
    private String serverRequestTotal;

    /**
     * 访问成功次数
     */
    private String serverRequestOkTotal;

    /**
     * 访问成功率
     */
    private String okRate;

    /**
     * 访问失败次数
     */
    private String serverRequestFailTotal;

    /**
     * 平均响应时长
     */
    private String avgResponseTime;

    /**
     * 请求方系统总量
     */
    private String requestSysTotal;

    /**
     * 异常服务总数
     */
    private String exceptionServerTotal;

}
