package com.iflytek.integrated.platform.dto;

import lombok.Data;

@Data
public class CallStatisticsDTO {
    /**
     * 业务名
     */
    private String name;

    /**
     * 指标值
     */
    private Long indexCount;

}
