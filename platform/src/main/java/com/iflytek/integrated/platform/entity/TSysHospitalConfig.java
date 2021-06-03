package com.iflytek.integrated.platform.entity;

import java.io.Serializable;

import lombok.Data;

/**
 * TSysHospitalConfig is a Querydsl bean type
 */
@Data
public class TSysHospitalConfig implements Serializable {

	private String id;

	private String sysConfigId;

	private String hospitalId;

	private String hospitalCode;
}
