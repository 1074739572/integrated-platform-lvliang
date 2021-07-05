package com.iflytek.integrated.platform.dto;

import com.iflytek.integrated.platform.entity.TBusinessInterface;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

/**
 * @author
 */
@Data
@ApiModel("接口配置实体")
public class BusinessInterfaceDto {

    /**
     * businessInterfaceId
     */
    private String id;

    private String projectId;

    private String platformId;
    
    private String requestSysId;

    private String requestSysconfigId;

    private String requestInterfaceId;

    private String requestInterfaceTypeId;

    private Integer interfaceSlowFlag;

    /**
     * 多个厂商配置信息
     */
    private List<TBusinessInterface> businessInterfaceList;
    /**
     * 新增编辑接口配置标识  1新增 2编辑
     */
    private String addOrUpdate;

}
