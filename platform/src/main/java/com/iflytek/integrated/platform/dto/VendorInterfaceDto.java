package com.iflytek.integrated.platform.dto;

import lombok.Data;

/**
* 新增厂商接口信息
* @author weihe9
* @date 2020/12/24 17:19
*/
@Data
public class VendorInterfaceDto {

    /**
     * 厂商接口名称
     */
    private String businessInterfaceName;
    /**
     * 请求方式（1、POST多参数 2、POST单参数  3、GET多参数  4、GET单参数）
     */
    private String requestType;
    /**
     * 请求常量
     */
    private String REQUEST_CONSTANT;
    private String PLUGIN_ID;
    private String EXC_ERR_STATUS;
    private String EXC_ERR_ORDER;
    private String IN_PARAM_FORMAT;
    private String IN_PARAM_SCHEMA;
    private String IN_PARAM_TEMPLATE;
    private String IN_PARAM_FORMAT_TYPE;
    private String OUT_PARAM_FORMAT;
    private String OUT_PARAM_SCHEMA;
    private String OUT_PARAM_TEMPLATE;
    private String OUT_PARAM_FORMAT_TYPE;


}
