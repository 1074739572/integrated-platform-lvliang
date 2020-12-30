package com.iflytek.integrated.platform.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;

/**
 * TLog is a Querydsl bean type
 */
public class TLog implements Serializable {

    private Long id;

    private String projectId;

    private String platformId;

    private String productFunctionLinkId;

    private String businessInterfaceId;

    private String visitAddr;

    private String businessReq;

    private String venderReq;

    private String venderRep;

    private String businessRep;

    private Integer venderRepTime;

    private Integer businessRepTime;

    private String requestIdentifier;

    private String status;

    private String createdBy;

    @JsonFormat(pattern="yyyy/MM/dd HH:mm:ss",timezone="GMT+8")
    private java.util.Date createdTime;

    private String updatedBy;

    private java.util.Date updatedTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getPlatformId() {
        return platformId;
    }

    public void setPlatformId(String platformId) {
        this.platformId = platformId;
    }

    public String getProductFunctionLinkId() {
        return productFunctionLinkId;
    }

    public void setProductFunctionLinkId(String productFunctionLinkId) {
        this.productFunctionLinkId = productFunctionLinkId;
    }

    public String getBusinessInterfaceId() {
        return businessInterfaceId;
    }

    public void setBusinessInterfaceId(String businessInterfaceId) {
        this.businessInterfaceId = businessInterfaceId;
    }

    public String getVisitAddr() {
        return visitAddr;
    }

    public void setVisitAddr(String visitAddr) {
        this.visitAddr = visitAddr;
    }

    public String getBusinessReq() {
        return businessReq;
    }

    public void setBusinessReq(String businessReq) {
        this.businessReq = businessReq;
    }

    public String getVenderReq() {
        return venderReq;
    }

    public void setVenderReq(String venderReq) {
        this.venderReq = venderReq;
    }

    public String getVenderRep() {
        return venderRep;
    }

    public void setVenderRep(String venderRep) {
        this.venderRep = venderRep;
    }

    public String getBusinessRep() {
        return businessRep;
    }

    public void setBusinessRep(String businessRep) {
        this.businessRep = businessRep;
    }

    public Integer getVenderRepTime() {
        return venderRepTime;
    }

    public void setVenderRepTime(Integer venderRepTime) {
        this.venderRepTime = venderRepTime;
    }

    public Integer getBusinessRepTime() {
        return businessRepTime;
    }

    public void setBusinessRepTime(Integer businessRepTime) {
        this.businessRepTime = businessRepTime;
    }

    public String getRequestIdentifier() {
        return requestIdentifier;
    }

    public void setRequestIdentifier(String requestIdentifier) {
        this.requestIdentifier = requestIdentifier;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

