package com.iflytek.integrated.platform.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @Author ganghuang6
 * @Date 2021/6/29
 */
@Data
@ApiModel("数据库连接测试")
public class DbUrlTestDto {

    //数据库类型
    private String databaseType;

    //驱动地址
    private String driverUrl;

    //数据库驱动
    private String databaseDriver;

    //数据库连接URL
    private String databaseUrl;

    private String userName;

    private String userPassword;
}
