package com.iflytek.integrated.platform.dto;

import com.iflytek.integrated.platform.entity.TPlatform;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;
/**
* 新增/编辑平台dto
* @author weihe9
* @date 2021/1/26 14:11
*/
@Data
@ApiModel("平台")
public class PlatformDto extends TPlatform {


    private List<VendorConfigDto> vendorInfo;

}
