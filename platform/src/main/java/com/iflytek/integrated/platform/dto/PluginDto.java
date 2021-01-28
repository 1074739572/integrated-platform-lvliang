package com.iflytek.integrated.platform.dto;

import com.iflytek.integrated.platform.entity.TPlugin;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

/**
 * 新增/编辑插件dto
 * @author weihe9
 * @date 2021/1/26 14:22
 */
@Data
@ApiModel("插件")
public class PluginDto {

    private String typeId;

    private String name;

    private List<TPlugin> children;

}
