package com.iflytek.integrated.platform.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Transient;

import java.io.Serializable;

/**
 * TLog is a Querydsl bean type
 */
public class TLog implements Serializable {

    private Long id;

    private Long count;

    private String businessInterfaceId;

    private String businessInterfaceName;

    private String interfaceId;

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

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private java.util.Date createdTime;

    private String updatedBy;

    private java.util.Date updatedTime;

    private String typeId;
    
    private Integer debugreplayFlag;

    private Integer replayFlag;

    private String interfaceName;

    private String interfaceUrl;

    private String regConnectionType;

    private String publishId;

    private String publishName;

    private String publishSysId;

    private String publishSysName;

    private String showOrder;

    private Integer excErrOrder;

    private String requestInterfaceId;

    private String regId;

    private String registryName;

    private String regSysId;

    private String regSysName;

    private String logType;

    private String logNode;

    private String logHeader;

    public String getLogType() {
        return logType;
    }

    public void setLogType(String logType) {
        this.logType = logType;
    }

    public String getLogNode() {
        return logNode;
    }

    public void setLogNode(String logNode) {
        this.logNode = logNode;
    }

    public String getLogHeader() {
        return logHeader;
    }

    public void setLogHeader(String logHeader) {
        this.logHeader = logHeader;
    }

    public String getBusinessInterfaceName() {
        return businessInterfaceName;
    }

    public void setBusinessInterfaceName(String businessInterfaceName) {
        this.businessInterfaceName = businessInterfaceName;
    }

    public String getRegId() {
        return regId;
    }

    public void setRegId(String regId) {
        this.regId = regId;
    }

    public String getRegistryName() {
        return registryName;
    }

    public void setRegistryName(String registryName) {
        this.registryName = registryName;
    }

    public String getRegSysId() {
        return regSysId;
    }

    public void setRegSysId(String regSysId) {
        this.regSysId = regSysId;
    }

    public String getRegSysName() {
        return regSysName;
    }

    public void setRegSysName(String regSysName) {
        this.regSysName = regSysName;
    }

    public Integer getReplayFlag() {
        return replayFlag;
    }

    public void setReplayFlag(Integer replayFlag) {
        this.replayFlag = replayFlag;
    }

    public String getRequestInterfaceId() {
        return requestInterfaceId;
    }

    public void setRequestInterfaceId(String requestInterfaceId) {
        this.requestInterfaceId = requestInterfaceId;
    }

    public Integer getExcErrOrder() {
        return excErrOrder;
    }

    public void setExcErrOrder(Integer excErrOrder) {
        this.excErrOrder = excErrOrder;
    }

    public String getShowOrder() {
        return showOrder;
    }

    public void setShowOrder(String showOrder) {
        this.showOrder = showOrder;
    }

    public String getBusinessInterfaceId() {
        return businessInterfaceId;
    }

    public void setBusinessInterfaceId(String businessInterfaceId) {
        this.businessInterfaceId = businessInterfaceId;
    }

    public String getPublishSysId() {
        return publishSysId;
    }

    public void setPublishSysId(String publishSysId) {
        this.publishSysId = publishSysId;
    }

    public String getPublishSysName() {
        return publishSysName;
    }

    public void setPublishSysName(String publishSysName) {
        this.publishSysName = publishSysName;
    }

    public String getPublishId() {
        return publishId;
    }

    public void setPublishId(String publishId) {
        this.publishId = publishId;
    }

    public String getPublishName() {
        return publishName;
    }

    public void setPublishName(String publishName) {
        this.publishName = publishName;
    }


    public String getRegConnectionType() {
        return regConnectionType;
    }

    public void setRegConnectionType(String regConnectionType) {
        this.regConnectionType = regConnectionType;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getInterfaceUrl() {
        return interfaceUrl;
    }

    public void setInterfaceUrl(String interfaceUrl) {
        this.interfaceUrl = interfaceUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

	public Integer getDebugreplayFlag() {
		return debugreplayFlag;
	}

	public void setDebugreplayFlag(Integer debugreplayFlag) {
		this.debugreplayFlag = debugreplayFlag;
	}

    public String getInterfaceId() {
        return interfaceId;
    }

    public void setInterfaceId(String interfaceId) {
        this.interfaceId = interfaceId;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}

