package com.iflytek.integrated.platform.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;

/**
 * TType is a Querydsl bean type
 */
@Data
public class TCadFile implements Serializable {

    private String id;

    private String docNo;

    private String docTheme;

    private String docStandardNo;

    private String docStandardDesc;

    private String docFileName;

    private String filePath;

    private String createdBy;

    private java.util.Date createdTime;

    private String updatedBy;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private java.util.Date updatedTime;
}

