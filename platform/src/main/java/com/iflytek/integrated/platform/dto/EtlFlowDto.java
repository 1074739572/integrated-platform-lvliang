package com.iflytek.integrated.platform.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;

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

}
