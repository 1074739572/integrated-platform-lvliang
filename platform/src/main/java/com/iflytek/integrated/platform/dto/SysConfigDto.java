package com.iflytek.integrated.platform.dto;

import java.util.List;

import com.iflytek.integrated.platform.entity.TSysConfig;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * 系统配置信息
 * 
 * @author weihe9
 * @date 2020/12/11 18:21
 */
@Data
@ApiModel("系统配置")
public class SysConfigDto extends TSysConfig {

	private String sysCode;

	private String sysName;

	private List<HospitalDto> hospitalConfig;

	private String platformId;
	
	private List<SysConfigDto> sysConfigs;

}
