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
    
    private Integer sysDriverCount;
    
    private Integer sysInftCount;
    
    private Integer pluginCount;
    
    private Integer driverCount;
    
    private Integer intfTransCount;
    
    private Integer etlCount;

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

	public Integer getSysDriverCount() {
		return sysDriverCount;
	}

	public void setSysDriverCount(Integer sysDriverCount) {
		this.sysDriverCount = sysDriverCount;
	}

	public Integer getSysInftCount() {
		return sysInftCount;
	}

	public void setSysInftCount(Integer sysInftCount) {
		this.sysInftCount = sysInftCount;
	}

	public Integer getPluginCount() {
		return pluginCount;
	}

	public void setPluginCount(Integer pluginCount) {
		this.pluginCount = pluginCount;
	}

	public Integer getDriverCount() {
		return driverCount;
	}

	public void setDriverCount(Integer driverCount) {
		this.driverCount = driverCount;
	}

	public Integer getIntfTransCount() {
		return intfTransCount;
	}

	public void setIntfTransCount(Integer intfTransCount) {
		this.intfTransCount = intfTransCount;
	}

	public Integer getEtlCount() {
		return etlCount;
	}

	public void setEtlCount(Integer etlCount) {
		this.etlCount = etlCount;
	}

}

