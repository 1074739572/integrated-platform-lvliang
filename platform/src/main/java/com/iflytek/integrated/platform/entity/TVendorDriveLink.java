package com.iflytek.integrated.platform.entity;

import java.io.Serializable;

/**
 * TVendorDriveLink is a Querydsl bean type
 */
public class TVendorDriveLink implements Serializable {

    private String id;

    private String vendorId;

    private String driveId;

    private Integer driveOrder;

    private String createdBy;

    private java.util.Date createdTime;

    private String updatedBy;

    private java.util.Date updatedTime;

    private String driveName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVendorId() {
        return vendorId;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public String getDriveId() {
        return driveId;
    }

    public void setDriveId(String driveId) {
        this.driveId = driveId;
    }

    public Integer getDriveOrder() {
        return driveOrder;
    }

    public void setDriveOrder(Integer driveOrder) {
        this.driveOrder = driveOrder;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public java.util.Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(java.util.Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public java.util.Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(java.util.Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    public String getDriveName() {
        return driveName;
    }

    public void setDriveName(String driveName) {
        this.driveName = driveName;
    }

}

