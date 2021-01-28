package com.iflytek.integrated.platform.dto;

import com.iflytek.integrated.platform.entity.TVendorConfig;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

/**
* 厂商配置信息
* @author weihe9
* @date 2020/12/11 18:21
*/
@Data
@ApiModel("厂商配置")
public class VendorConfigDto extends TVendorConfig {

    private String vendorCode;

    private String vendorName;

    private List<HospitalDto> hospitalConfig;

    private String platformId;

    private List<VendorConfigDto> vendorInfo;

}
