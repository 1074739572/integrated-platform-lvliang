package com.iflytek.integrated.platform.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

/**
 * @author czzhan
 * 产品功能
 */
@Data
public class ProductFunctionDto {

    /**
     * 关联关系id
     */
    private String id;

    /**
     * 产品名称
     */
    @NotBlank(message = "产品名称不能为空")
    @Length(max = 32, message = "产品名称不能超过32")
    private String productName;

    /**
     * 功能名称
     */
    @NotBlank(message = "功能名称不能为空")
    @Length(max = 32, message = "功能名称不能超过32")
    private String functionName;
}
