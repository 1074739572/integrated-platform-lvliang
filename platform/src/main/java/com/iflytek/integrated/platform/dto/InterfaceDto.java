package com.iflytek.integrated.platform.dto;

import com.iflytek.integrated.platform.entity.TInterfaceParam;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import java.util.List;

/**
* 标准接口信息
* @author weihe9
* @date 2020/12/14 17:43
*/
@Data
@ApiModel("标准接口信息")
public class InterfaceDto {

    private String sysId;

    /**
     * 接口入参
     */
    private List<TInterfaceParam> inParamList;
    /**
     * 接口出参
     */
    private List<TInterfaceParam> outParamList;


    private String id;

    private String interfaceName;

    private String typeId;

    private String interfaceUrl;

    private String inParamFormat;

    private String outParamFormat;

    private String paramOutStatus;

    private String paramOutStatusSuccess;

    private String createdBy;

    private java.util.Date createdTime;

    private String updatedBy;

    private java.util.Date updatedTime;

    private String interfaceTypeName;

    private Long inParamCount;

    private Long outParamCount;
    
    private String inParamSchema;
    
    private String inParamFormatType;
    
    private String outParamSchema;
    
    private String outParamFormatType;

}
