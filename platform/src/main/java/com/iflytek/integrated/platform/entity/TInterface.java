package com.iflytek.integrated.platform.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

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

    @JsonFormat(pattern="yyyy/MM/dd HH:mm:ss")
    private java.util.Date updatedTime;

    private String interfaceTypeName;

    private Long inParamCount;

    private Long outParamCount;

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

    public String getInterfaceTypeName() {
        return interfaceTypeName;
    }

    public void setInterfaceTypeName(String interfaceTypeName) {
        this.interfaceTypeName = interfaceTypeName;
    }

    public Long getInParamCount() {
        return inParamCount;
    }

    public void setInParamCount(Long inParamCount) {
        this.inParamCount = inParamCount;
    }

    public Long getOutParamCount() {
        return outParamCount;
    }

    public void setOutParamCount(Long outParamCount) {
        this.outParamCount = outParamCount;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TInterface) {
            if (this.id.equals(((TInterface) obj).id)){
                return true;
            }
        }
        return super.equals(obj);
    }
}

