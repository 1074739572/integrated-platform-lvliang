package com.iflytek.integrated.platform.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * TBusinessInterface is a Querydsl bean type
 */
@ApiModel("接口配置")
@Data
public class THistory implements Serializable {

    private String pkId;

    private Integer hisType;

    private String hisContent;

    private String createdBy;

    private Date createdTime;

    private String originId;

    private String recordId;



}
