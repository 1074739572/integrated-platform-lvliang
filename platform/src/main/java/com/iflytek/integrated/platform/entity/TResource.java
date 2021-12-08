package com.iflytek.integrated.platform.entity;

import java.io.Serializable;

/**
 * 资源标签
 */
public class TResource implements Serializable {

    private String id;

    private String type;

    private String typeName;

    private String resourceName;
    
    private Long sysDriverCount;
    
    private Long sysInftCount;
    
    private Long pluginCount;
    
    private Long driverCount;
    
    private Long intfTransCount;
    
    private Long etlCount;
    
    private Long hospitalCount;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	public Long getSysDriverCount() {
		return sysDriverCount;
	}

	public void setSysDriverCount(Long sysDriverCount) {
		this.sysDriverCount = sysDriverCount;
	}

	public Long getSysInftCount() {
		return sysInftCount;
	}

	public void setSysInftCount(Long sysInftCount) {
		this.sysInftCount = sysInftCount;
	}

	public Long getPluginCount() {
		return pluginCount;
	}

	public void setPluginCount(Long pluginCount) {
		this.pluginCount = pluginCount;
	}

	public Long getDriverCount() {
		return driverCount;
	}

	public void setDriverCount(Long driverCount) {
		this.driverCount = driverCount;
	}

	public Long getIntfTransCount() {
		return intfTransCount;
	}

	public void setIntfTransCount(Long intfTransCount) {
		this.intfTransCount = intfTransCount;
	}

	public Long getEtlCount() {
		return etlCount;
	}

	public void setEtlCount(Long etlCount) {
		this.etlCount = etlCount;
	}

	public Long getHospitalCount() {
		return hospitalCount;
	}

	public void setHospitalCount(Long hospitalCount) {
		this.hospitalCount = hospitalCount;
	}

}

