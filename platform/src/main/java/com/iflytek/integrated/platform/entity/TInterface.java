package com.iflytek.integrated.platform.entity;

import java.io.Serializable;

/**
 * TInterface is a Querydsl bean type
 */
public class TInterface implements Serializable {

    private String id;

    private String interfaceName;

    private String interfaceTypeId;

    private String interfaceUrl;

    private String interfaceFormat;

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

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getInterfaceTypeId() {
        return interfaceTypeId;
    }

    public void setInterfaceTypeId(String interfaceTypeId) {
        this.interfaceTypeId = interfaceTypeId;
    }

    public String getInterfaceUrl() {
        return interfaceUrl;
    }

    public void setInterfaceUrl(String interfaceUrl) {
        this.interfaceUrl = interfaceUrl;
    }

    public String getInterfaceFormat() {
        return interfaceFormat;
    }

    public void setInterfaceFormat(String interfaceFormat) {
        this.interfaceFormat = interfaceFormat;
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

