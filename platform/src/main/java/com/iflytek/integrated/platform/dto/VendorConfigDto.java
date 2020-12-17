package com.iflytek.integrated.platform.dto;

import com.iflytek.integrated.platform.entity.TVendorConfig;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
* 厂商配置信息
* @author weihe9
* @date 2020/12/11 18:21
*/
@Data
public class VendorConfigDto extends TVendorConfig {

    private String vendorCode;

    private String vendorName;

    private List<Map<String, String>> hospitalConfig;

}
