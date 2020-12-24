package com.iflytek.integrated.platform.dto;

import com.alibaba.fastjson.JSONArray;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author czzhan
 * @version 1.0
 * @date 2020/12/24 14:03
 */
@Data
public class JoltDebuggerDto {

    @NotNull(message = "originJson不能为空")
    private Object originJson;

    @NotNull(message = "jolt不能为空")
    private JSONArray jolt;
}
