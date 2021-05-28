package com.iflytek.integrated.platform.dto;

import com.iflytek.integrated.platform.entity.TSys;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * 新增/编辑产品dto
 * 
 * @author weihe9
 * @date 2021/1/26 14:22
 */
@Data
@ApiModel("系统")
public class SysDto extends TSys {

	/**
	 * 新增编辑标识 1新增 2编辑
	 */
	private String addOrUpdate;

	private String driveIds;

}
