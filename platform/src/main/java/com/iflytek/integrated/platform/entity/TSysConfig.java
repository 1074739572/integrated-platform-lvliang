package com.iflytek.integrated.platform.entity;

import org.hibernate.validator.constraints.Length;

import com.iflytek.integrated.platform.dto.SysHospitalDto;

import lombok.Data;
import org.springframework.data.annotation.Transient;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

/**
 * TSysConfig is a Querydsl bean type
 */
@Data
public class TSysConfig implements Serializable {

    private String id;
    
    private String projectId;

    private String platformId;

    private String sysId;
    
    private String sysCode;

	private String sysName;

	private List<SysHospitalDto> hospitalConfigs;
	
	private String hospitalConfigStr;

//  系统配置类型（1、请求方 2、被请求方）
    private Integer sysConfigType;
    
    @NotBlank(message = "接口版本不能为空")
    @Length(max = 32, message = "接口版本长度不能超过32")
    private String versionId;

    private String connectionType;

    @Length(max = 128, message = "调用地址长度不能超过128")
    private String addressUrl;

    @Length(max = 128, message = "端点地址长度不能超过128")
    private String endpointUrl;

    @Length(max = 128, message = "命名空间长度不能超过128")
    private String namespaceUrl;

    private String databaseName;

    @Length(max = 128, message = "数据库连接URL长度不能超过128")
    private String databaseUrl;

    @Length(max = 128, message = "数据库驱动长度不能超过128")
    private String databaseDriver;

    private String driverUrl;

    private String jsonParams;

    @Length(max = 32, message = "用户名长度不能超过32")
    private String userName;

    private String userPassword;

    private String createdBy;

    private java.util.Date createdTime;

    private String updatedBy;

    private java.util.Date updatedTime;
    
    private String innerIdx;
    
    private String databaseType;

    @Transient
    private String sysInterface;

}

