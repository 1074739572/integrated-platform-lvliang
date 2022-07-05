package com.iflytek.integrated.platform.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.annotation.Transient;

import java.io.Serializable;

/**
 * TArea is a Querydsl bean type
 */
@Data
public class TFunctionAuth implements Serializable {

    private String id;

    private String interfaceId;

    private String publishId;

    private String createdBy;

    private java.util.Date createdTime;

    private String updatedBy;

    private java.util.Date updatedTime;

    @Transient
    @ApiModelProperty("服务名称")
    private String interfaceName;

    @Transient
    @ApiModelProperty("业务类型名称")
    private String typeName;

    @Transient
    @ApiModelProperty("服务提供方/服务名称")
    private String businessInterfaceName;
}

