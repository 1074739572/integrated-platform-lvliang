package com.iflytek.integrated.platform.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel("区域实体")
public class EtlLogInfoDto {

    private String projectName;

    private String platformName;

    private String hospitalName;

    private String sysName;

}
