package com.iflytek.integrated.platform.dto;

import lombok.Data;

import java.util.List;

/**
 * 累计类统计DTO
 */
@Data
public class TotalStatisticsDTO {
    /**
     * 服务总数
     */
    private String serverTotal;

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
     * 服务调用次数TOP10
     */
    List<CallStatisticsDTO> serverCallTopTen;

    /**
     * 实时服务调用统计
     */
    List<CallStatisticsDTO> serverCall;

    /**
     * 服务调用厂商TOP10
     */
    List<CallStatisticsDTO> vendorTopTen;

    /**
     * 服务类型访问次数统计
     */
    List<CallStatisticsDTO> typeTopTen;

    /**
     * 服务调用速度TOP10
     */
    List<CallStatisticsDTO> speedTopTen;
}
