package com.iflytek.integrated.platform.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

@ApiModel("ETLFlow数组实体类")
@Data
public class BatchEtlFlowDto {

    private List<EtlFlowDto> list ;


}
