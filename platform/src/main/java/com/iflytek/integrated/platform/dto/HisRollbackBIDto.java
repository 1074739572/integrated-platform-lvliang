package com.iflytek.integrated.platform.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("回滚历史版本（接口转换）入参")
public class HisRollbackBIDto {

    @ApiModelProperty(value = "历史版本id",required = true)
    private String pkId;

    @ApiModelProperty(value = "requestInterfaceId",notes = "从当前接口取（历史版本里取）",required = true)
    private String interfaceId;

    @ApiModelProperty(value = "requestSysconfigId",notes = "从当前接口取（历史版本里取）",required = true)
    private String requestSysconfigId;
}
