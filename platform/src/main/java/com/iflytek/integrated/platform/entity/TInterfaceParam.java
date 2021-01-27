package com.iflytek.integrated.platform.entity;

import io.swagger.annotations.ApiModel;
import org.springframework.data.annotation.Transient;

import java.io.Serializable;

/**
 * TInterfaceParam is a Querydsl bean type
 */
@ApiModel("接口出入参")
public class TInterfaceParam implements Serializable {

    private String id;

    private String paramName;

    private String paramInstruction;

    private String interfaceId;

    private String paramType;

    private Integer paramLength;

    private String paramInOut;

    private String createdBy;

    private java.util.Date createdTime;

    private String updatedBy;

    private java.util.Date updatedTime;

    /**
     * 出参是否开启 1开启
     */
    @Transient
    private String isStart;
    /**
     * 状态字段（仅用于出参)
     */
    @Transient
    private String paramOutStatus;
    /**
     * 状态字段成功状态值 (仅用于出参)
     */
    @Transient
    private String paramOutStatusSuccess;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public String getParamInstruction() {
        return paramInstruction;
    }

    public void setParamInstruction(String paramInstruction) {
        this.paramInstruction = paramInstruction;
    }

    public String getInterfaceId() {
        return interfaceId;
    }

    public void setInterfaceId(String interfaceId) {
        this.interfaceId = interfaceId;
    }

    public String getParamType() {
        return paramType;
    }

    public void setParamType(String paramType) {
        this.paramType = paramType;
    }

    public Integer getParamLength() {
        return paramLength;
    }

    public void setParamLength(Integer paramLength) {
        this.paramLength = paramLength;
    }

    public String getParamInOut() {
        return paramInOut;
    }

    public void setParamInOut(String paramInOut) {
        this.paramInOut = paramInOut;
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

    public String getIsStart() {
        return isStart;
    }

    public void setIsStart(String isStart) {
        this.isStart = isStart;
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

}

