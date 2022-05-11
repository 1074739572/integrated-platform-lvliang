package com.iflytek.integrated.platform.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Objects;

/**
 * TPlugin is a Querydsl bean type
 */
@ApiModel("插件")
public class TPlugin implements Serializable {

    private String id;

    private String typeId;

    @NotBlank(message = "插件名称不能为空")
    @Length(max = 20, message = "插件名称不能超过20")
    private String pluginName;

    @Length(max = 20, message = "插件编码不能超过20")
    private String pluginCode;

    @Length(max = 100, message = "插件说明不能超过100")
    private String pluginInstruction;

    @NotBlank(message = "插件内容不能为空")
    private String pluginContent;

    private String createdBy;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private java.util.Date createdTime;

    private String updatedBy;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private java.util.Date updatedTime;

    private String pluginTypeName;

    private String pluginId;

    private String name;

    private String dependentPath;

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

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
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

    public String getPluginTypeName() {
        return pluginTypeName;
    }

    public void setPluginTypeName(String pluginTypeName) {
        this.pluginTypeName = pluginTypeName;
    }

    public String getPluginId() {
        return pluginId;
    }

    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (obj instanceof TPlugin) {
            if (this.id.equals(((TPlugin) obj).id)) {
                return true;
            }
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, typeId, pluginName, pluginCode, pluginInstruction, pluginContent, createdBy, createdTime, updatedBy, updatedTime, pluginTypeName, pluginId, name, dependentPath);
    }

    public String getDependentPath() {
        return dependentPath;
    }

    public void setDependentPath(String dependentPath) {
        this.dependentPath = dependentPath;
    }
}

