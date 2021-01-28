package com.iflytek.integrated.platform.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
* 产品功能dto
* @author weihe9
* @date 2021/1/26 14:38
*/
@Data
@ApiModel("产品功能实体")
public class FunctionDto {

    private String functionId;


    private String id;

    private String functionName;

    private String functionCode;

    private String createdBy;

    private java.util.Date createdTime;

    private String updatedBy;

    private java.util.Date updatedTime;

}
