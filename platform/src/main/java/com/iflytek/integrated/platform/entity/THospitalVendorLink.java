package com.iflytek.integrated.platform.entity;

import java.io.Serializable;

/**
 * THospitalVendorLink is a Querydsl bean type
 */
public class THospitalVendorLink implements Serializable {

    private String id;

    private String vendorConfigId;

    private String hospitalId;

    private String vendorHospitalId;

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

    public String getVendorConfigId() {
        return vendorConfigId;
    }

    public void setVendorConfigId(String vendorConfigId) {
        this.vendorConfigId = vendorConfigId;
    }

    public String getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(String hospitalId) {
        this.hospitalId = hospitalId;
    }

    public String getVendorHospitalId() {
        return vendorHospitalId;
    }

    public void setVendorHospitalId(String vendorHospitalId) {
        this.vendorHospitalId = vendorHospitalId;
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

}

