package com.iflytek.integrated.platform.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * 接口调试dto
 * 
 * @author lsn
 * @date 2021/1/26 14:22
 */
@Data
@ApiModel("接口调试")
public class InterfaceDebugDto {

	private String sysIntfParamFormatType;

	private String format;

	private String wsdlUrl;

	private String wsOperationName;
	
	private String funcode;

}
