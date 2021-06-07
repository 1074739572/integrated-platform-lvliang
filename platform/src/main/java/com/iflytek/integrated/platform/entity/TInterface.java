package com.iflytek.integrated.platform.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * TInterface is a Querydsl bean type
 */
@Data
public class TInterface implements Serializable {

    private String id;
    
    private String sysId;

    @NotBlank(message = "接口名称不能为空")
    @Length(max = 20, message = "接口名称长度不能超过20")
    private String interfaceName;

    private String typeId;

    @NotBlank(message = "接口方法不能为空")
    @Length(max = 100, message = "接口方法长度不能超过100")
    private String interfaceUrl;

    private String inParamFormat;

    private String outParamFormat;

    private String paramOutStatus;

    private String paramOutStatusSuccess;

    private String createdBy;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private java.util.Date createdTime;

    private String updatedBy;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private java.util.Date updatedTime;

    private String interfaceTypeName;

    private Long inParamCount;

    private Long outParamCount;
    
    private String inParamSchema;
    
    private String inParamFormatType;
    
    private String outParamSchema;
    
    private String outParamFormatType;

}

