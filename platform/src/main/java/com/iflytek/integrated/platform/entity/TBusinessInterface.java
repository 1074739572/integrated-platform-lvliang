package com.iflytek.integrated.platform.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import org.springframework.data.annotation.Transient;

import java.io.Serializable;

/**
 * TBusinessInterface is a Querydsl bean type
 */
@ApiModel("接口配置")
@Data
public class TBusinessInterface implements Serializable {

    private String id;

    private String requestSysconfigId;

    private String requestInterfaceId;
    
    private String requestedSysconfigId;

    private String businessInterfaceName;

    private String requestType;

    private String requestConstant;

    private String interfaceType;

    private String pluginId;

    private String inParamFormat;

    private String inParamSchema;

    private String inParamTemplate;

    private String inParamFormatType;

    private String outParamFormat;

    private String outParamSchema;

    private String outParamTemplate;

    private String outParamFormatType;

    private String mockTemplate;

    private String mockStatus;

    private String status;

    private String excErrStatus;

    private Integer excErrOrder;

    private String createdBy;

    private java.util.Date createdTime;

    private String updatedBy;

    private java.util.Date updatedTime;

    private Integer mockIsUse;

    @Transient
    private String requestInterfaceName;
    @Transient
    private String platformId;
    @Transient
    private String requestSysId;

    private String requestedSysId;

    /**
     * 列表接口配置查询出参
     */
    @Transient
    private String productName;
    @Transient
    private String versionId;
    @Transient
    private String projectCode;
    @Transient
    private String productCode;
    @Transient
    private String interfaceUrl;

}

