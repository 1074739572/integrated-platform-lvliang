package com.iflytek.integrated.platform.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel("ETL模板实体")
public class EtlTplDto{

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

}

