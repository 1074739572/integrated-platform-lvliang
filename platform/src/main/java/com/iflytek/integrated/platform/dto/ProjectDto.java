package com.iflytek.integrated.platform.dto;

import com.iflytek.integrated.platform.entity.TProject;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

/**
 *
 * @author 新增/编辑项目dto
 * @date 2021/1/26 18:30
 */
@Data
@ApiModel("项目")
public class ProjectDto extends TProject {

	private List<SysDto> sysList;

}
