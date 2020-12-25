package com.iflytek.integrated.platform.entity;

import org.springframework.data.annotation.Transient;

import java.io.Serializable;

/**
 * TBusinessInterface is a Querydsl bean type
 */
public class TBusinessInterface implements Serializable {

    private String id;

    private String productFunctionLinkId;

    private String interfaceId;

    private String vendorConfigId;

    private String businessInterfaceName;

    private String requestType;

    private String requestConstant;

    private String interfaceType;

    private String pluginId;

    private String inParamFormat;

    private String inParamSchema;

    private String inParamTemplate;

    private String inParamFormatType;

    private String outParamFormat;

    private String outParamSchema;

    private String outParamTemplate;

    private String outParamFormatType;

    private String mockTemplate;

    private String mockStatus;

    private String status;

    private String excErrStatus;

    private Integer excErrOrder;

    private String createdBy;

    private java.util.Date createdTime;

    private String updatedBy;

    private java.util.Date updatedTime;

    @Transient
    private String interfaceName;
    @Transient
    private String platformId;
    @Transient
    private String vendorId;
    @Transient
    private String productId;
    @Transient
    private String functionId;

    /**
     * 列表接口配置查询出参
     */
    @Transient
    private String productName;
    @Transient
    private String functionName;
    @Transient
    private String versionId;
    @Transient
    private String projectCode;
    @Transient
    private String productCode;
    @Transient
    private String interfaceUrl;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProductFunctionLinkId() {
        return productFunctionLinkId;
    }

    public void setProductFunctionLinkId(String productFunctionLinkId) {
        this.productFunctionLinkId = productFunctionLinkId;
    }

    public String getInterfaceId() {
        return interfaceId;
    }

    public void setInterfaceId(String interfaceId) {
        this.interfaceId = interfaceId;
    }

    public String getVendorConfigId() {
        return vendorConfigId;
    }

    public void setVendorConfigId(String vendorConfigId) {
        this.vendorConfigId = vendorConfigId;
    }

    public String getBusinessInterfaceName() {
        return businessInterfaceName;
    }

    public void setBusinessInterfaceName(String businessInterfaceName) {
        this.businessInterfaceName = businessInterfaceName;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getRequestConstant() {
        return requestConstant;
    }

    public void setRequestConstant(String requestConstant) {
        this.requestConstant = requestConstant;
    }

    public String getInterfaceType() {
        return interfaceType;
    }

    public void setInterfaceType(String interfaceType) {
        this.interfaceType = interfaceType;
    }

    public String getPluginId() {
        return pluginId;
    }

    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    public String getInParamFormat() {
        return inParamFormat;
    }

    public void setInParamFormat(String inParamFormat) {
        this.inParamFormat = inParamFormat;
    }

    public String getInParamSchema() {
        return inParamSchema;
    }

    public void setInParamSchema(String inParamSchema) {
        this.inParamSchema = inParamSchema;
    }

    public String getInParamTemplate() {
        return inParamTemplate;
    }

    public void setInParamTemplate(String inParamTemplate) {
        this.inParamTemplate = inParamTemplate;
    }

    public String getInParamFormatType() {
        return inParamFormatType;
    }

    public void setInParamFormatType(String inParamFormatType) {
        this.inParamFormatType = inParamFormatType;
    }

    public String getOutParamFormat() {
        return outParamFormat;
    }

    public void setOutParamFormat(String outParamFormat) {
        this.outParamFormat = outParamFormat;
    }

    public String getOutParamSchema() {
        return outParamSchema;
    }

    public void setOutParamSchema(String outParamSchema) {
        this.outParamSchema = outParamSchema;
    }

    public String getOutParamTemplate() {
        return outParamTemplate;
    }

    public void setOutParamTemplate(String outParamTemplate) {
        this.outParamTemplate = outParamTemplate;
    }

    public String getOutParamFormatType() {
        return outParamFormatType;
    }

    public void setOutParamFormatType(String outParamFormatType) {
        this.outParamFormatType = outParamFormatType;
    }

    public String getMockTemplate() {
        return mockTemplate;
    }

    public void setMockTemplate(String mockTemplate) {
        this.mockTemplate = mockTemplate;
    }

    public String getMockStatus() {
        return mockStatus;
    }

    public void setMockStatus(String mockStatus) {
        this.mockStatus = mockStatus;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getExcErrStatus() {
        return excErrStatus;
    }

    public void setExcErrStatus(String excErrStatus) {
        this.excErrStatus = excErrStatus;
    }

    public Integer getExcErrOrder() {
        return excErrOrder;
    }

    public void setExcErrOrder(Integer excErrOrder) {
        this.excErrOrder = excErrOrder;
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

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getPlatformId() {
        return platformId;
    }

    public void setPlatformId(String platformId) {
        this.platformId = platformId;
    }

    public String getVendorId() {
        return vendorId;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getFunctionId() {
        return functionId;
    }

    public void setFunctionId(String functionId) {
        this.functionId = functionId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getInterfaceUrl() {
        return interfaceUrl;
    }

    public void setInterfaceUrl(String interfaceUrl) {
        this.interfaceUrl = interfaceUrl;
    }

}

