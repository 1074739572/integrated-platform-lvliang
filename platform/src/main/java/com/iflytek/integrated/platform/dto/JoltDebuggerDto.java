package com.iflytek.integrated.platform.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author czzhan
 * @version 1.0
 * @date 2020/12/24 14:03
 */
@Data
public class JoltDebuggerDto {

    @NotBlank(message = "originObj不能为空")
    private String originObj;

    @NotNull(message = "jolt不能为空")
    private Object jolt;
}
