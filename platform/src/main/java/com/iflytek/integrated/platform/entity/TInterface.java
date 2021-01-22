package com.iflytek.integrated.platform.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * TInterface is a Querydsl bean type
 */
public class TInterface implements Serializable {

    private String id;

    @NotBlank(message = "接口名称不能为空")
    @Length(max = 20, message = "接口名称长度不能超过20")
    private String interfaceName;

    private String typeId;

    private String interfaceUrl;

    private String inParamFormat;

    private String outParamFormat;

    private String paramOutStatus;

    private String paramOutStatusSuccess;

    private String createdBy;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private java.util.Date createdTime;

    private String updatedBy;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
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

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public String getInterfaceUrl() {
        return interfaceUrl;
    }

    public void setInterfaceUrl(String interfaceUrl) {
        this.interfaceUrl = interfaceUrl;
    }

    public String getInParamFormat() {
        return inParamFormat;
    }

    public void setInParamFormat(String inParamFormat) {
        this.inParamFormat = inParamFormat;
    }

    public String getOutParamFormat() {
        return outParamFormat;
    }

    public void setOutParamFormat(String outParamFormat) {
        this.outParamFormat = outParamFormat;
    }

    public String getParamOutStatus() {
        return paramOutStatus;
    }

    public void setParamOutStatus(String paramOutStatus) {
        this.paramOutStatus = paramOutStatus;
    }

    public String getParamOutStatusSuccess() {
        return paramOutStatusSuccess;
    }

    public void setParamOutStatusSuccess(String paramOutStatusSuccess) {
        this.paramOutStatusSuccess = paramOutStatusSuccess;
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
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (obj instanceof TInterface) {
            if (this.id.equals(((TInterface) obj).id)){
                return true;
            }
        }
        return super.equals(obj);
    }

}

