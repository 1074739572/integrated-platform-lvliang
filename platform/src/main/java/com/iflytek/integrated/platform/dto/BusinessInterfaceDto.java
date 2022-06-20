package com.iflytek.integrated.platform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    private String requestInterfaceId;

    private String requestInterfaceTypeId;

    private Integer interfaceSlowFlag;

    private Integer replayFlag;

    private String QIId;

    private Integer QIFlag;

    private String mockStatus;

    private String status;

    /**
     * 多个厂商配置信息
     */
    private List<TBusinessInterface> businessInterfaceList;
    /**
     * 新增编辑接口配置标识  1新增 2编辑
     */
    private String addOrUpdate;


    public String getQIId() {
        return QIId;
    }

    @JsonProperty("QIId")
    public void setQIId(String QIId) {
        this.QIId = QIId;
    }

    public Integer getQIFlag() {
        return QIFlag;
    }

    @JsonProperty("QIFlag")
    public void setQIFlag(Integer QIFlag) {
        this.QIFlag = QIFlag;
    }

}
