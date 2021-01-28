package com.iflytek.integrated.platform.dto;

import com.iflytek.integrated.platform.entity.TDrive;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

/**
 * 新增/编辑驱动dto
 * @author weihe9
 * @date 2021/1/26 14:22
 */
@Data
@ApiModel("驱动")
public class DriveDto {

    private String id;

    private String name;

    private List<TDrive> children;

}
