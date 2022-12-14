package com.iflytek.integrated.platform.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * TPlatform is a Querydsl bean type
 */
public class TPlatform implements Serializable {

    private String id;

    private String projectId;

    @NotBlank(message = "平台名称不能为空")
    @Length(max = 32, message = "平台名称长度不能超过32")
    private String platformName;

    private String platformCode;

    private String platformStatus;

    private String platformType;
    
    private String etlServerUrl;
    
    private String etlUser;
    
    private String etlPwd;

    private String createdBy;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private java.util.Date createdTime;

    private String updatedBy;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private java.util.Date updatedTime;

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

    public String getPlatformName() {
        return platformName;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }

    public String getPlatformCode() {
        return platformCode;
    }

    public void setPlatformCode(String platformCode) {
        this.platformCode = platformCode;
    }

    public String getPlatformStatus() {
        return platformStatus;
    }

    public void setPlatformStatus(String platformStatus) {
        this.platformStatus = platformStatus;
    }

    public String getPlatformType() {
        return platformType;
    }

    public void setPlatformType(String platformType) {
        this.platformType = platformType;
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

	public String getEtlServerUrl() {
		return etlServerUrl;
	}

	public void setEtlServerUrl(String etlServerUrl) {
		this.etlServerUrl = etlServerUrl;
	}

	public String getEtlUser() {
		return etlUser;
	}

	public void setEtlUser(String etlUser) {
		this.etlUser = etlUser;
	}

	public String getEtlPwd() {
		return etlPwd;
	}

	public void setEtlPwd(String etlPwd) {
		this.etlPwd = etlPwd;
	}

}

