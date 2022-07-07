package com.iflytek.integrated.platform.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.annotation.Transient;

import java.io.Serializable;

/**
 * TType is a Querydsl bean type
 */
@Data
public class TServerStatisticsHour implements Serializable {

    private String id;

    private Long serverRequestTotal;

    private java.util.Date dt;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private java.util.Date update_time;

}

