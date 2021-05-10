package com.iflytek.integrated.platform.entity;

import java.io.Serializable;

import org.springframework.data.annotation.Transient;

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
    
    @Transient
    private String sysName;
    
    @Transient
    private String sysId;
    
    @Transient
    private String hospitalId;
    
    @Transient
    private String hospitalName;

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

}

