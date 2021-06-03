package com.iflytek.integrated.platform.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * 医院配置信息
 * 
 * @author weihe9
 * @date 2020/12/14 17:43
 */
@Data
@ApiModel("系统医院配置信息")
public class SysHospitalDto {

	private String hospitalId;

	private String hospitalCode;
}
