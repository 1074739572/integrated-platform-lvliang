package com.iflytek.integrated.platform.entity;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * TPlugin is a Querydsl bean type
 */
public class TPlugin implements Serializable {

    private String id;

    @NotBlank(message = "插件名称不能为空")
    @Length(max = 20, message = "插件名称不能超过20")
    private String pluginName;

    @NotBlank(message = "插件编码不能为空")
    @Length(max = 20, message = "插件编码不能超过20")
    private String pluginCode;

    @Length(max = 100, message = "插件说明不能超过100")
    private String pluginInstruction;

    @NotBlank(message = "插件内容不能为空")
    private String pluginContent;

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

    public String getPluginName() {
        return pluginName;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    public String getPluginCode() {
        return pluginCode;
    }

    public void setPluginCode(String pluginCode) {
        this.pluginCode = pluginCode;
    }

    public String getPluginInstruction() {
        return pluginInstruction;
    }

    public void setPluginInstruction(String pluginInstruction) {
        this.pluginInstruction = pluginInstruction;
    }

    public String getPluginContent() {
        return pluginContent;
    }

    public void setPluginContent(String pluginContent) {
        this.pluginContent = pluginContent;
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

