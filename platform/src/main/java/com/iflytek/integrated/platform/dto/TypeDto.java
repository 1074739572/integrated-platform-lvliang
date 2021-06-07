package com.iflytek.integrated.platform.dto;

import com.iflytek.integrated.platform.entity.TType;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

/**
 * @Author ganghuang6
 * @Date 2021/6/7
 */
@Data
@ApiModel("分类")
public class TypeDto{

    private List<TType> typeList;
}
