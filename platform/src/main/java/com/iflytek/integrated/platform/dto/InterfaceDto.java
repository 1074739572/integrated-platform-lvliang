package com.iflytek.integrated.platform.dto;

import com.iflytek.integrated.platform.entity.TInterface;
import com.iflytek.integrated.platform.entity.TInterfaceParam;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

/**
* 标准接口信息
* @author weihe9
* @date 2020/12/14 17:43
*/
@Data
@ApiModel("标准接口信息")
public class InterfaceDto extends TInterface {

    private String productId;

    private List<String> productIds;

    /**
     * 接口入参
     */
    private List<TInterfaceParam> inParamList;
    /**
     * 接口出参
     */
    private List<TInterfaceParam> outParamList;


}
