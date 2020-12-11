package com.iflytek.integrated.common;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author czzhan
 */
public class ResultDto {
    @ApiModelProperty(
            value = "HttpStatus",
            name = "操作结果"
    )
    private int code;
    @ApiModelProperty(
            value = "错误提示",
            name = "提示信息"
    )
    private String message;
    @ApiModelProperty(
            value = "业务数据",
            name = "业务数据"
    )
    private Object data;

    public ResultDto() {
    }

    public ResultDto(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public ResultDto(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return this.code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return this.data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
