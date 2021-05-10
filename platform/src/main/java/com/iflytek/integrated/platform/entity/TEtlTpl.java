package com.iflytek.integrated.platform.entity;

import java.io.Serializable;
import java.util.List;

/**
 * TEtlTpl is a Querydsl bean type
 */
public class TEtlTpl implements Serializable {

    private String id;

    private String tplName;

    private Integer tplType;

    private Integer tplFunType;

    private String tplContent;
    
    private String tplDesp;

    private String createdBy;

    private java.util.Date createdTime;

    private String updatedBy;

    private java.util.Date updatedTime;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTplName() {
		return tplName;
	}

	public void setTplName(String tplName) {
		this.tplName = tplName;
	}

	public Integer getTplType() {
		return tplType;
	}

	public void setTplType(Integer tplType) {
		this.tplType = tplType;
	}

	public Integer getTplFunType() {
		return tplFunType;
	}

	public void setTplFunType(Integer tplFunType) {
		this.tplFunType = tplFunType;
	}

	public String getTplContent() {
		return tplContent;
	}

	public void setTplContent(String tplContent) {
		this.tplContent = tplContent;
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

	public String getTplDesp() {
		return tplDesp;
	}

	public void setTplDesp(String tplDesp) {
		this.tplDesp = tplDesp;
	}

}

