package com.iflytek.integrated.platform.dto;

import com.iflytek.integrated.platform.entity.TVendorConfig;

import java.util.List;
import java.util.Map;

/**
* 厂商配置信息
* @author weihe9
* @date 2020/12/11 18:21
*/
public class VendorConfigDto extends TVendorConfig {

    private String vendorCode;

    private String vendorName;

    private List<Map<String, String>> hospitalConfig;

    public String getVendorCode() {
        return vendorCode;
    }

    public void setVendorCode(String vendorCode) {
        this.vendorCode = vendorCode;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public List<Map<String, String>> getHospitalConfig() {
        return hospitalConfig;
    }

    public void setHospitalConfig(List<Map<String, String>> hospitalConfig) {
        this.hospitalConfig = hospitalConfig;
    }

}
