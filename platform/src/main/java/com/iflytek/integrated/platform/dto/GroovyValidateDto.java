package com.iflytek.integrated.platform.dto;

import lombok.Data;

/**
 * @author czzhan
 * 校验groovy脚本格式是否正确
 */
@Data
public class GroovyValidateDto {

    /**
     * 成功时的返回
     */
    private String validResult;

    private String validStatus;

    /**
     * 失败时的返回
     */
    private String result;

    private String error;

}
