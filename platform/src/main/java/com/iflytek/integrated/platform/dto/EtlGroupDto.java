package com.iflytek.integrated.platform.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;

/**
 * TEtlGroup is a Querydsl bean type
 */
@ApiModel("ETL流程实例组实体")
public class EtlGroupDto implements Serializable {

	private String id;

	private String projectId;

	private String platformId;

	private String sysId;

	private String hospitalId;

	private String etlGroupId;

	private String etlGroupName;

	public String getId() {
		return id;
	}

	public void setId(String id) {
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

	public String getEtlGroupId() {
		return etlGroupId;
	}

	public void setEtlGroupId(String etlGroupId) {
		this.etlGroupId = etlGroupId;
	}

	public String getEtlGroupName() {
		return etlGroupName;
	}

	public void setEtlGroupName(String etlGroupName) {
		this.etlGroupName = etlGroupName;
	}

}
