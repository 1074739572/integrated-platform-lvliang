package com.iflytek.integrated.platform.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author czzhan
 * @version 1.0
 * @date 2021/1/11 16:57
 */
@Data
public class MockTemplateDto {

    @NotBlank(message = "接口配置id不能为空")
    String id;

    @NotBlank(message = "接口配置Mock模板不能为空")
    String mockTemplate;

    @NotNull(message = "mock是否需要模拟不能为空")
    Integer mockIsUse;

    Integer excErrOrder;

    public MockTemplateDto(String id, String mockTemplate, Integer mockIsUse, Integer excErrOrder){
        this.id = id;
        this.mockTemplate = mockTemplate;
        this.mockIsUse = mockIsUse;
        this.excErrOrder = excErrOrder;
    }
}
