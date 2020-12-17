package com.iflytek.integrated.platform.dto;

import com.iflytek.integrated.platform.entity.TInterface;
import com.iflytek.integrated.platform.entity.TInterfaceParam;
import lombok.Data;

import java.util.List;

/**
* 标准接口信息
* @author weihe9
* @date 2020/12/14 17:43
*/
@Data
public class InterfaceDto extends TInterface {

    private String productId;

    private List<String> productIds;

    /**
     * 出参格式
     */
    private String outParamFormat;
    /**
     * 接口入参
     */
    private List<TInterfaceParam> inParamList;
    /**
     * 接口出参
     */
    private List<TInterfaceParam> outParamList;


}
