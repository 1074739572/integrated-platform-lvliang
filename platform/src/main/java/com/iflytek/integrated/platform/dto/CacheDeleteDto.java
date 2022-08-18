package com.iflytek.integrated.platform.dto;

import lombok.Data;

import java.util.List;

/**
 * @author fangkun
 * @date 2022/8/20 14:12
 */
@Data
public class CacheDeleteDto {
    /**
     * 优先使用系统编码
     */
    private List<String> sysCodes;

    /**
     * 系统编码没有传值的时候使用id列表去查询编码
     * 如果id和编码都没传表示查询全部
     */
    private List<String> sysIds;

    private List<String> interfaceIds;

    private List<String> interfaceCodes;

    /**
     * 1.IntegratedPlatform:Configs:WS:drivers:_productcode
     * 2.IntegratedPlatform:Configs:authentication:_productcode
     * 3.IntegratedPlatform:Configs:WS:schema:_funcode_productcode
     * 4.IntegratedPlatform:Configs:_productcode_funcode
     */
    private List<Integer> cacheTypeList;

}
