package com.iflytek.integrated.platform.entity;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * TSys is a Querydsl bean type
 */
@Data
public class TSys implements Serializable {

    private String id;

    @NotBlank(message = "系统名称不能为空")
    @Length(max = 32, message = "系统名称长度不能超过32")
    private String sysName;

    private String sysCode;

    private String isValid;

    private String createdBy;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private java.util.Date createdTime;

    private String updatedBy;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private java.util.Date updatedTime;
    
}

