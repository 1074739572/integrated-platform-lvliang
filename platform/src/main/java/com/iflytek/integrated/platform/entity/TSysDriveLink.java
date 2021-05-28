package com.iflytek.integrated.platform.entity;

import java.io.Serializable;

import lombok.Data;

/**
 * TSysDriveLink is a Querydsl bean type
 */
@Data
public class TSysDriveLink implements Serializable {

    private String id;

    private String sysId;

    private String driveId;

    private Integer driveOrder;

    private String createdBy;

    private java.util.Date createdTime;

    private String updatedBy;

    private java.util.Date updatedTime;

    private String driveName;

}

