package com.iflytek.integrated.platform.entity;

import lombok.Data;

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
}

