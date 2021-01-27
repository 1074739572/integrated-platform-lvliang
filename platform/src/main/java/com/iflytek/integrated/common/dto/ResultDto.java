package com.iflytek.integrated.common.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author czzhan
 */
@ApiModel
@Data
public class ResultDto<T> {
    @ApiModelProperty(
            value = "HttpStatus",
            name = "操作结果"
    )
    private Integer code;

    @ApiModelProperty(
            value = "错误提示",
            name = "提示信息",
            example ="xxx"
    )
    private String message;

    @ApiModelProperty(
            value = "业务数据",
            name = "业务数据"
    )
    private T data;


    public ResultDto() {
    }

    public ResultDto(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public ResultDto(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
