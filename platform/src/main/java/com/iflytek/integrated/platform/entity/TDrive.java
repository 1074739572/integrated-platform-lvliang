package com.iflytek.integrated.platform.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * TDrive is a Querydsl bean type
 */
@ApiModel("驱动")
public class TDrive implements Serializable {

    private String id;

    private String typeId;

    @NotBlank(message = "驱动名称不能为空")
    @Length(max = 20, message = "驱动名称不能超过20")
    private String driveName;

    @Length(max = 20, message = "驱动编码不能超过20")
    private String driveCode;

    @Length(max = 100, message = "驱动说明不能超过100")
    private String driveInstruction;

    @NotBlank(message = "驱动内容不能为空")
    private String driveContent;

    private String createdBy;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private java.util.Date createdTime;

    private String updatedBy;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private java.util.Date updatedTime;

    private String driveTypeName;

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

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
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

    public String getDriveTypeName() {
        return driveTypeName;
    }

    public void setDriveTypeName(String driveTypeName) {
        this.driveTypeName = driveTypeName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (obj instanceof TDrive) {
            if (this.id.equals(((TDrive) obj).id)) {
                return true;
            }
        }
        return super.equals(obj);
    }

}

