package com.iflytek.integrated.platform.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @author czzhan
 * @version 1.0
 * @date 2020/12/23 18:42
 */
@Data
@ApiModel("标准接口出入参")
public class ParamsDto {

    private String paramKey;

    private String paramType;

    private Object paramValue;
}
