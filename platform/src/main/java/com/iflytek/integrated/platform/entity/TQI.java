package com.iflytek.integrated.platform.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@ApiModel("质检")
@Data
public class TQI implements Serializable {

    private String QIId;

    private String QIName;

    private String QIScript;

    private Date createdTime;

    private String createdBy;


}
