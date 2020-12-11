package com.iflytek.integrated.common;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author czzhan
 */
public class ResultDto {
    @ApiModelProperty(
            value = "tHttpStatus",
            name = "操作结果"
    )
    private int flag;
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

    public ResultDto(int flag, String message, Object data) {
        this.flag = flag;
        this.message = message;
        this.data = data;
    }

    public ResultDto(int flag, String message) {
        this.flag = flag;
        this.message = message;
    }

    public int isFlag() {
        return this.flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
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
