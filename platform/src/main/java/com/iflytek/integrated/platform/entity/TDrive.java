package com.iflytek.integrated.platform.entity;

import java.io.Serializable;

/**
 * TDrive is a Querydsl bean type
 */
public class TDrive implements Serializable {

    private String id;

    private String driveName;

    private String driveCode;

    private String driveInstruction;

    private String driveContent;

    private String createdBy;

    private java.util.Date createdTime;

    private String updatedBy;

    private java.util.Date updatedTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDriveName() {
        return driveName;
    }

    public void setDriveName(String driveName) {
        this.driveName = driveName;
    }

    public String getDriveCode() {
        return driveCode;
    }

    public void setDriveCode(String driveCode) {
        this.driveCode = driveCode;
    }

    public String getDriveInstruction() {
        return driveInstruction;
    }

    public void setDriveInstruction(String driveInstruction) {
        this.driveInstruction = driveInstruction;
    }

    public String getDriveContent() {
        return driveContent;
    }

    public void setDriveContent(String driveContent) {
        this.driveContent = driveContent;
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

}

