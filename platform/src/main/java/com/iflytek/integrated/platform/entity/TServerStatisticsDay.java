package com.iflytek.integrated.platform.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.annotation.Transient;

import java.io.Serializable;

/**
 * TType is a Querydsl bean type
 */
@Data
public class TServerStatisticsDay implements Serializable {

    private String id;

    private String serverId;

    private String typeId;

    private String vendorId;

    private Long currRequestTotal;

    private Long currRequestOkTotal;

    private Long currResponseTimeTotal;

    private java.util.Date dt;

    @Transient
    private String interfaceName;

    @Transient
    private String typeName;

    @Transient
    private String vendorName;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private java.util.Date update_time;

}

