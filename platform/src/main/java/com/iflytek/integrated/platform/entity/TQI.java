package com.iflytek.integrated.platform.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;

import java.io.Serializable;
import java.util.Date;

@ApiModel("质检")
public class TQI implements Serializable {

    private String QIId;

    private String QIName;

    private String QIScript;

    private Date createdTime;

    private String createdBy;

    private Date updatedTime;

    private String updatedBy;

    @JsonProperty("QIId")
    public String getQIId() {
        return QIId;
    }

    public void setQIId(String QIId) {
        this.QIId = QIId;
    }

    @JsonProperty("QIName")
    public String getQIName() {
        return QIName;
    }

    public void setQIName(String QIName) {
        this.QIName = QIName;
    }

    @JsonProperty("QIScript")
    public String getQIScript() {
        return QIScript;
    }

    public void setQIScript(String QIScript) {
        this.QIScript = QIScript;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
