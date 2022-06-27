package com.iflytek.integrated.platform.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * TArea is a Querydsl bean type
 */
@Data
public class TEngine implements Serializable {

    private String id;

    private String engineName;

    private String engineCode;

    private String isEtl;

    private String engineUrl;

    private String engineUser;

    private String enginePwd;

    private String createdBy;

    private java.util.Date createdTime;

    private String updatedBy;

    private java.util.Date updatedTime;
}

