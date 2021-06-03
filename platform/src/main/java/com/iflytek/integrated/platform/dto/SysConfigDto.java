package com.iflytek.integrated.platform.dto;

import java.io.Serializable;
import java.util.List;

import com.iflytek.integrated.platform.entity.TSysConfig;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiParam;
import lombok.Data;

/**
 * 系统配置信息
 * 
 * @author weihe9
 * @date 2020/12/11 18:21
 */
@Data
@ApiModel("系统配置")
public class SysConfigDto implements Serializable {

	@ApiParam("请求方系统配置")
	private TSysConfig requestSysConfig;

	@ApiParam("被请求方系统配置")
	private List<TSysConfig> requestedSysConfigs;

}
