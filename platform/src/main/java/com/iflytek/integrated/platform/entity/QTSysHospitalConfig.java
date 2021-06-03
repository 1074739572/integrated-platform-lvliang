package com.iflytek.integrated.platform.entity;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

import java.sql.Types;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.PrimaryKey;

/**
 * QTSysHospitalConfig is a Querydsl query type for TSysDriveLink
 */
public class QTSysHospitalConfig extends com.querydsl.sql.RelationalPathBase<TSysHospitalConfig> {

	private static final long serialVersionUID = -890822584;

	public static final QTSysHospitalConfig qTSysHospitalConfig = new QTSysHospitalConfig("t_sys_hospital_config");

	public final StringPath id = createString("id");
	public final StringPath sysConfigId = createString("sysConfigId");
	public final StringPath hospitalId = createString("hospitalId");
	public final StringPath hospitalCode = createString("hospitalCode");

	public final StringPath createdBy = createString("createdBy");

	public final DateTimePath<java.util.Date> createdTime = createDateTime("createdTime", java.util.Date.class);

	public final StringPath updatedBy = createString("updatedBy");

	public final DateTimePath<java.util.Date> updatedTime = createDateTime("updatedTime", java.util.Date.class);

	public final PrimaryKey<TSysHospitalConfig> primary = createPrimaryKey(id);

	public QTSysHospitalConfig(String variable) {
		super(TSysHospitalConfig.class, forVariable(variable), "null", "t_sys_hospital_config");
		addMetadata();
	}

	public QTSysHospitalConfig(String variable, String schema, String table) {
		super(TSysHospitalConfig.class, forVariable(variable), schema, table);
		addMetadata();
	}

	public QTSysHospitalConfig(String variable, String schema) {
		super(TSysHospitalConfig.class, forVariable(variable), schema, "t_sys_hospital_config");
		addMetadata();
	}

	public QTSysHospitalConfig(Path<? extends TSysHospitalConfig> path) {
		super(path.getType(), path.getMetadata(), "null", "t_sys_hospital_config");
		addMetadata();
	}

	public QTSysHospitalConfig(PathMetadata metadata) {
		super(TSysHospitalConfig.class, metadata, "null", "t_sys_hospital_config");
		addMetadata();
	}

	public void addMetadata() {
		addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
		addMetadata(sysConfigId,
				ColumnMetadata.named("SYS_CONFIG_ID").withIndex(2).ofType(Types.VARCHAR).withSize(32).notNull());
		addMetadata(hospitalId,
				ColumnMetadata.named("HOSPITAL_ID").withIndex(3).ofType(Types.VARCHAR).withSize(32).notNull());
		addMetadata(hospitalCode,
				ColumnMetadata.named("HOSPITAL_CODE").withIndex(4).ofType(Types.VARCHAR).withSize(32).notNull());
		addMetadata(createdBy,
				ColumnMetadata.named("CREATED_BY").withIndex(5).ofType(Types.VARCHAR).withSize(32).notNull());
		addMetadata(createdTime,
				ColumnMetadata.named("CREATED_TIME").withIndex(6).ofType(Types.TIMESTAMP).withSize(19).notNull());
		addMetadata(updatedBy,
				ColumnMetadata.named("UPDATED_BY").withIndex(7).ofType(Types.VARCHAR).withSize(32).notNull());
		addMetadata(updatedTime,
				ColumnMetadata.named("UPDATED_TIME").withIndex(8).ofType(Types.TIMESTAMP).withSize(19).notNull());
	}

}
