package com.iflytek.integrated.platform.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("回滚历史版本入参")
public class HisRollbackDto {

    @ApiModelProperty(value = "历史版本id",required = true)
    private String pkId;

    @ApiModelProperty(value = "历史版本类型（1接口转换 2驱动 3插件）",required = true)
    private Integer hisType;

//    @ApiModelProperty(value = "当前驱动/插件id")
//    private String id;

}
