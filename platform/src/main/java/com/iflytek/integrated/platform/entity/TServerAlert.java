package com.iflytek.integrated.platform.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;

/**
 * TType is a Querydsl bean type
 */
@Data
public class TServerAlert implements Serializable {

    private String id;

    private String url;

    private String theme;

    private Integer type;

    private String token;

    private Integer level;

    private String createdBy;

    private java.util.Date createdTime;

    private String updatedBy;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private java.util.Date updatedTime;
}

