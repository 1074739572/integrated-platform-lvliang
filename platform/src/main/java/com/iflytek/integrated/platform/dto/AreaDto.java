package com.iflytek.integrated.platform.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

/**
 * @author czzhan
 * @version 1.0
 * @date 2021/1/15 17:26
 */
@Data
@ApiModel("区域实体")
public class AreaDto {

    private String areaCode;

    private String areaName;

    private List<AreaDto> children;
}
