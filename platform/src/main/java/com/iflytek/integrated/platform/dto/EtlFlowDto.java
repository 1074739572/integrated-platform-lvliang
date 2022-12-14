package com.iflytek.integrated.platform.dto;

import io.swagger.annotations.ApiModel;

import java.io.Serializable;

/**
 * TEtlFlow is a Querydsl bean type
 */
@ApiModel("ETL流程实例实体")
public class EtlFlowDto implements Serializable {

	private String id;

	private String groupId;

	private String flowName;

	private String etlGroupId;

	private String flowConfig;
	
	private String flowDesp;
	
	private String flowTplName;
	
	private String funTplNames;

	private EtlGroupDto etlGroupDto;

	private String status;
	
	private String etlEntryGroupId;
	
	private String parentGroupId;
	
	private String etlControlId;
	
	private String etlJobcontrolId;
	
	private Integer maxDuration;
	
	private String parentEtlGroupId;
	
	private Integer alertDuration;
	
	private java.util.Date lastDebugTime;

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

	public EtlGroupDto getEtlGroupDto() {
		return etlGroupDto;
	}

	public void setEtlGroupDto(EtlGroupDto etlGroupDto) {
		this.etlGroupDto = etlGroupDto;
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

	public String getParentEtlGroupId() {
		return parentEtlGroupId;
	}

	public void setParentEtlGroupId(String parentEtlGroupId) {
		this.parentEtlGroupId = parentEtlGroupId;
	}

	public Integer getAlertDuration() {
		return alertDuration;
	}

	public void setAlertDuration(Integer alertDuration) {
		this.alertDuration = alertDuration;
	}

	public java.util.Date getLastDebugTime() {
		return lastDebugTime;
	}

	public void setLastDebugTime(java.util.Date lastDebugTime) {
		this.lastDebugTime = lastDebugTime;
	}

	public String getEtlJobcontrolId() {
		return etlJobcontrolId;
	}

	public void setEtlJobcontrolId(String etlJobcontrolId) {
		this.etlJobcontrolId = etlJobcontrolId;
	}
	
}
