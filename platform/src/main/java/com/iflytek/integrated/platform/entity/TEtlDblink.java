package com.iflytek.integrated.platform.entity;

import java.io.Serializable;

/**
 * TEtlDblink is a Querydsl bean type
 */
public class TEtlDblink implements Serializable {
	
	private String id;
	
	private String etlGroupId;
	
	private String etlProcessorId;
	
	private String dbConfigId;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getEtlGroupId() {
		return etlGroupId;
	}

	public void setEtlGroupId(String etlGroupId) {
		this.etlGroupId = etlGroupId;
	}

	public String getEtlProcessorId() {
		return etlProcessorId;
	}

	public void setEtlProcessorId(String etlProcessorId) {
		this.etlProcessorId = etlProcessorId;
	}

	public String getDbConfigId() {
		return dbConfigId;
	}

	public void setDbConfigId(String dbConfigId) {
		this.dbConfigId = dbConfigId;
	}

}

