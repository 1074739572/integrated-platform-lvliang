package com.iflytek.integrated.platform.dto;

import com.iflytek.integrated.platform.entity.TFunction;
import io.swagger.annotations.ApiModel;
import lombok.Data;
/**
* 产品功能dto
* @author weihe9
* @date 2021/1/26 14:38
*/
@Data
@ApiModel("产品功能实体")
public class FunctionDto extends TFunction {

    private String functionId;

}
