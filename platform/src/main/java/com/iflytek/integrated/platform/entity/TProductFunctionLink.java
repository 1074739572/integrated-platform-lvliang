package com.iflytek.integrated.platform.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;

/**
 * TProductFunctionLink is a Querydsl bean type
 */
public class TProductFunctionLink implements Serializable {

    private String id;

    private String productId;

    private String functionId;

    private String createdBy;

    @JsonFormat(pattern="yyyy/MM/dd HH:mm:ss")
    private java.util.Date createdTime;

    private String updatedBy;

    @JsonFormat(pattern="yyyy/MM/dd HH:mm:ss")
    private java.util.Date updatedTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getFunctionId() {
        return functionId;
    }

    public void setFunctionId(String functionId) {
        this.functionId = functionId;
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

