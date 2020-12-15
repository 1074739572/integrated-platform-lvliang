package com.iflytek.integrated.platform.dto;

import com.iflytek.integrated.platform.entity.TInterface;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
* 标准接口信息
* @author weihe9
* @date 2020/12/14 17:43
*/
@Data
public class InterfaceDto extends TInterface {

    private String productId;

    /**
     * 出参格式
     */
    private String outParamFormat;
    /**
     * 接口入参
     */
    private List<Map<String, String>> inParamList;
    /**
     * 接口出参
     */
    private List<Map<String, String>> outParamList;


}
