package com.iflytek.integrated.platform.entity;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.Transient;

import java.io.Serializable;

/**
 * TSysConfig is a Querydsl bean type
 */
@Data
public class TSysPublish implements Serializable {

    private String id;
    
    private String sysId;
    
    private String connectionType;

    private String publishName;

    @Length(max = 128, message = "发布地址URL长度不能超过128")
    private String addressUrl;

    private String isAuthen;

    private String limitIps;

    private String createdBy;

    private java.util.Date createdTime;

    private String updatedBy;

    private String isValid;

    private String signKey;

    private String mqReponseFormat;

    private java.util.Date updatedTime;

    @Transient
    private String sysName;

    @Transient
    private String sysCode;

    /**
     * 是否明文
     */
    @Transient
    private String isShow;
}

