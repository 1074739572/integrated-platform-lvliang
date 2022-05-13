package com.iflytek.integrated.platform.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Transient;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * TEtlFlow is a Querydsl bean type
 */
public class TEtlLog implements Serializable {
	
	private Long id;
	
	private String etlGroupId;
	
	private String flowName;
	
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
	private Date jobTime;
	
	private String exeJobId;
	
	private Integer exeBatchNo;
	
	private Integer batchReadCount;
	
	private Integer batchWriteErrorcount;

	private Integer effectWriteCount;

	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
	private Date createdTime;
	
	private String status;
	
	@Transient
	private Integer statusCode;
	
	private String errorInfo;

	private String QIResult;

	@Transient
	private String execTime;
	
	@Transient
	private String projectName;
	
	@Transient
	private String platformName;
	
	@Transient
	private String sysName;
	
	@Transient
	private String hospitalName;
	
	@Transient
	private Integer allReadCount;
	
	@Transient
	private Integer allWriteErrorcount;

	@Transient
	private Integer allEffectWriteCount;
	
	private List<TEtlLog> batchErrorLogs;
	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEtlGroupId() {
		return etlGroupId;
	}

	public void setEtlGroupId(String etlGroupId) {
		this.etlGroupId = etlGroupId;
	}

	public String getFlowName() {
		return flowName;
	}

	public void setFlowName(String flowName) {
		this.flowName = flowName;
	}

	public Date getJobTime() {
		return jobTime;
	}

	public void setJobTime(Date jobTime) {
		this.jobTime = jobTime;
	}

	public Date getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getErrorInfo() {
		return errorInfo;
	}

	public void setErrorInfo(String errorInfo) {
		this.errorInfo = errorInfo;
	}

	@JsonProperty("QIResult")
	public String getQIResult() {
		return QIResult;
	}

	public void setQIResult(String QIResult) {
		this.QIResult = QIResult;
	}

	public String getExecTime() {
		return execTime;
	}

	public void setExecTime(String execTime) {
		this.execTime = execTime;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getPlatformName() {
		return platformName;
	}

	public void setPlatformName(String platformName) {
		this.platformName = platformName;
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

	public String getExeJobId() {
		return exeJobId;
	}

	public void setExeJobId(String exeJobId) {
		this.exeJobId = exeJobId;
	}

	public Integer getExeBatchNo() {
		return exeBatchNo;
	}

	public void setExeBatchNo(Integer exeBatchNo) {
		this.exeBatchNo = exeBatchNo;
	}

	public Integer getBatchReadCount() {
		return batchReadCount;
	}

	public void setBatchReadCount(Integer batchReadCount) {
		this.batchReadCount = batchReadCount;
	}

	public Integer getBatchWriteErrorcount() {
		return batchWriteErrorcount;
	}

	public void setBatchWriteErrorcount(Integer batchWriteErrorcount) {
		this.batchWriteErrorcount = batchWriteErrorcount;
	}

	public Integer getAllReadCount() {
		return allReadCount;
	}

	public void setAllReadCount(Integer allReadCount) {
		this.allReadCount = allReadCount;
	}

	public Integer getAllWriteErrorcount() {
		return allWriteErrorcount;
	}

	public void setAllWriteErrorcount(Integer allWriteErrorcount) {
		this.allWriteErrorcount = allWriteErrorcount;
	}

	public Integer getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(Integer statusCode) {
		this.statusCode = statusCode;
	}

	public List<TEtlLog> getBatchErrorLogs() {
		return batchErrorLogs;
	}

	public void setBatchErrorLogs(List<TEtlLog> batchErrorLogs) {
		this.batchErrorLogs = batchErrorLogs;
	}

	public Integer getAllEffectWriteCount() {
		return allEffectWriteCount;
	}

	public void setAllEffectWriteCount(Integer allEffectWriteCount) {
		this.allEffectWriteCount = allEffectWriteCount;
	}

	public Integer getEffectWriteCount() {
		return effectWriteCount;
	}

	public void setEffectWriteCount(Integer effectWriteCount) {
		this.effectWriteCount = effectWriteCount;
	}
}

