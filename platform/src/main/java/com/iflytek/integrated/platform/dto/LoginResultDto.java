package com.iflytek.integrated.platform.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author ganghuang6
 * @Date 2021/7/12
 */
@Data
@ApiModel("登录结果")
public class LoginResultDto {
    @ApiModelProperty("token")
    private String token;

    @ApiModelProperty("envFlag")
    private String envFlag;

}
