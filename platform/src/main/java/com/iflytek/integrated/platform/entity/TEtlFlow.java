package com.iflytek.integrated.platform.entity;

import org.springframework.data.annotation.Transient;

import java.io.Serializable;

/**
 * TEtlFlow is a Querydsl bean type
 */
public class TEtlFlow implements Serializable {

    private String id;
    
    private String tplId;

    private String groupId;

    private String flowName;

    private String etlGroupId;

    private String flowConfig;
    
    private String flowDesp;
    
    private String flowTplName;
    
    private String funTplNames;
    
    private String createdBy;

    private java.util.Date createdTime;

    private String updatedBy;

    private java.util.Date updatedTime;

    private String status;
    
    private String etlEntryGroupId;
    
    private String etlControlId;
    
    @Transient
    private String sysName;
    
    @Transient
    private String sysId;
    
    @Transient
    private String hospitalId;
    
    @Transient
    private String hospitalName;
    
    @Transient
    private String platformId;
    
    private String parentGroupId;
    
    private Integer maxDuration;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getFlowName() {
		return flowName;
	}

	public void setFlowName(String flowName) {
		this.flowName = flowName;
	}

	public String getEtlGroupId() {
		return etlGroupId;
	}

	public void setEtlGroupId(String etlGroupId) {
		this.etlGroupId = etlGroupId;
	}

	public String getFlowConfig() {
		return flowConfig;
	}

	public void setFlowConfig(String flowConfig) {
		this.flowConfig = flowConfig;
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

	public String getFlowDesp() {
		return flowDesp;
	}

	public void setFlowDesp(String flowDesp) {
		this.flowDesp = flowDesp;
	}

	public String getFlowTplName() {
		return flowTplName;
	}

	public void setFlowTplName(String flowTplName) {
		this.flowTplName = flowTplName;
	}

	public String getFunTplNames() {
		return funTplNames;
	}

	public void setFunTplNames(String funTplNames) {
		this.funTplNames = funTplNames;
	}

	public String getSysName() {
		return sysName;
	}

	public void setSysName(String sysName) {
		this.sysName = sysName;
	}

	public String getHospitalName() {
		return hospitalName;
	}

	public void setHospitalName(String hospitalName) {
		this.hospitalName = hospitalName;
	}

	public String getTplId() {
		return tplId;
	}

	public void setTplId(String tplId) {
		this.tplId = tplId;
	}

	public String getSysId() {
		return sysId;
	}

	public void setSysId(String sysId) {
		this.sysId = sysId;
	}

	public String getHospitalId() {
		return hospitalId;
	}

	public void setHospitalId(String hospitalId) {
		this.hospitalId = hospitalId;
	}

	public String getPlatformId() {
		return platformId;
	}

	public void setPlatformId(String platformId) {
		this.platformId = platformId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getEtlEntryGroupId() {
		return etlEntryGroupId;
	}

	public void setEtlEntryGroupId(String etlEntryGroupId) {
		this.etlEntryGroupId = etlEntryGroupId;
	}

	public String getParentGroupId() {
		return parentGroupId;
	}

	public void setParentGroupId(String parentGroupId) {
		this.parentGroupId = parentGroupId;
	}

	public String getEtlControlId() {
		return etlControlId;
	}

	public void setEtlControlId(String etlControlId) {
		this.etlControlId = etlControlId;
	}

	public Integer getMaxDuration() {
		return maxDuration;
	}

	public void setMaxDuration(Integer maxDuration) {
		this.maxDuration = maxDuration;
	}
	
}

