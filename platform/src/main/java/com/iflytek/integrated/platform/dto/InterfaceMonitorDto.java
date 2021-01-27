package com.iflytek.integrated.platform.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @author czzhan
 * @version 1.0
 * @date 2020/12/20 16:54
 */
@Data
@ApiModel("接口监控")
public class InterfaceMonitorDto {

    /**
     * 接口监控id
     */
    private Long id;

    /**
     * 产品名
     */
    private String productName;

    /**
     * 功能名
     */
    private String functionName;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 平台名称
     */
    private String platformName;

    /**
     * 状态（1、正常 2、异常）
     */
    private String status;

    /**
     * 成功次数
     */
    private Integer successCount;

    /**
     * 失败次数
     */
    private Integer errorCount;
}
