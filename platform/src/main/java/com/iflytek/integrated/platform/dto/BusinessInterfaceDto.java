package com.iflytek.integrated.platform.dto;

import com.iflytek.integrated.platform.entity.TBusinessInterface;
import lombok.Data;

import java.util.List;

@Data
public class BusinessInterfaceDto {

    /**
     * businessInterfaceId
     */
    private String id;

    private String platformId;

    private String productId;

    private String functionId;

    private String productFunctionLinkId;

    private String interfaceId;

    private String vendorId;

    private String vendorConfigId;
    /**
     * 多个厂商配置信息
     */
    private List<TBusinessInterface> businessInterfaceList;

}
