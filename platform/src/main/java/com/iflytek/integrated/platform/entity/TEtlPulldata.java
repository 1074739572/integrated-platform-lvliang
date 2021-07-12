package com.iflytek.integrated.platform.entity;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.annotation.Transient;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * TEtlPulldata is a Querydsl bean type
 */
public class TEtlPulldata implements Serializable {
	
	private String id;
	
	private String etlGroupId;
	
	private Integer pageNum;
	
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
	private Date debugTime;
	
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
	private Date createdTime;
	
	private String flowfileJson;
	
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

	public Integer getPageNum() {
		return pageNum;
	}

	public void setPageNum(Integer pageNum) {
		this.pageNum = pageNum;
	}

	public Date getDebugTime() {
		return debugTime;
	}

	public void setDebugTime(Date debugTime) {
		this.debugTime = debugTime;
	}

	public Date getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}

	public String getFlowfileJson() {
		return flowfileJson;
	}

	public void setFlowfileJson(String flowfileJson) {
		this.flowfileJson = flowfileJson;
	}
	
}

