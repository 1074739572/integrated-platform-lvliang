package com.iflytek.integrated.platform.dto;

import java.util.List;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * 接口调试
 * 
 * @author czzhan
 * @version 1.0
 * @date 2020/12/18 15:24
 */
@Data
@ApiModel("接口调试")
public class InDebugResDto {

	/**
	 * 标准接口url
	 */
	private String funcode;

	/**
	 * 产品code
	 */
	private String productcode;

	/**
	 * 医院code列表
	 */
	private List<String> orgids;

	/**
	 * 入参列表
	 */
	private List<String> inParams;

	private String wsInParams;

	private String wsdlUrl;

	private List<String> wsOperationNames;
	
	private String sysIntfParamFormatType;
}
