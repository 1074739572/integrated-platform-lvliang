package com.iflytek.integrated.common.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author czzhan
 * 描述: 表格分页
 */
@Data
@ApiModel("表格分页")
public class TableData<T> {

    @ApiModelProperty(
            name = "total",
            value = "页数"
    )
    private int total;

    @ApiModelProperty(
            name = "rows",
            value = "数据列表"
    )
    private List<T> rows;

    public TableData(long total, List<T> rows) {
        this.total = new Long(total).intValue();
        this.rows = rows;
    }

    public TableData(){

    }
}