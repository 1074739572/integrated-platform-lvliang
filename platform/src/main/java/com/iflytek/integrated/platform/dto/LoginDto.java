package com.iflytek.integrated.platform.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author ganghuang6
 * @Date 2021/7/12
 */
@Data
@ApiModel("登录")
public class LoginDto {
    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("密码")
    private String password;

    // 图片验证码
    private String encryptedMeta;
    private String verifyCode;
}
