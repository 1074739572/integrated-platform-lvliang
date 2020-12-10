package com.iflytek.integrated.platform.entity;

import java.io.Serializable;

/**
 * TInterfaceType is a Querydsl bean type
 */
public class TInterfaceType implements Serializable {

    private String id;

    private String interfaceTypeName;

    private String interfaceTypeCode;

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

    public String getInterfaceTypeName() {
        return interfaceTypeName;
    }

    public void setInterfaceTypeName(String interfaceTypeName) {
        this.interfaceTypeName = interfaceTypeName;
    }

    public String getInterfaceTypeCode() {
        return interfaceTypeCode;
    }

    public void setInterfaceTypeCode(String interfaceTypeCode) {
        this.interfaceTypeCode = interfaceTypeCode;
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

