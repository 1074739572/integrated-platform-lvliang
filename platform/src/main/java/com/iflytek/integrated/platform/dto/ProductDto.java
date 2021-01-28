package com.iflytek.integrated.platform.dto;

import com.iflytek.integrated.platform.entity.TProduct;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

/**
* 新增/编辑产品dto
* @author weihe9
* @date 2021/1/26 14:22
*/
@Data
@ApiModel("产品")
public class ProductDto extends TProduct {

    private String functionId;

    private String productId;

    private String oldProductId;

    private String oldFunctionId;

    private String oldProductName;
    /**
     * 新增编辑标识 1新增 2编辑
     */
    private String addOrUpdate;

    private List<FunctionDto> functionList;

}
