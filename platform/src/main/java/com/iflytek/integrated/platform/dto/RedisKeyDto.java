package com.iflytek.integrated.platform.dto;

import lombok.Data;
/**
* redis_key对象
* @author weihe9
* @date 2021/1/20 14:12
*/
@Data
public class RedisKeyDto {

    private String projectCode;

    private String orgId;

    private String productCode;

    private String funCode;

}
