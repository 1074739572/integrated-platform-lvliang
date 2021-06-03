package com.iflytek.integrated.platform.entity;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

import java.sql.Types;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;

/**
 * QTVendorConfig is a Querydsl query type for TSysConfig
 */
public class QTSysConfig extends com.querydsl.sql.RelationalPathBase<TSysConfig> {

	private static final long serialVersionUID = -23654882;

	public static final QTSysConfig qTSysConfig = new QTSysConfig("t_sys_config");

	public final StringPath id = createString("id");

	public final StringPath projectId = createString("projectId");

	public final StringPath platformId = createString("platformId");

	public final StringPath sysId = createString("sysId");

	public final NumberPath<Integer> sysConfigType = createNumber("sysConfigType", Integer.class);

//	public final StringPath hospitalConfigs = createString("hospitalConfigs");

	public final StringPath addressUrl = createString("addressUrl");

	public final StringPath connectionType = createString("connectionType");

	public final StringPath createdBy = createString("createdBy");

	public final DateTimePath<java.util.Date> createdTime = createDateTime("createdTime", java.util.Date.class);

	public final StringPath databaseDriver = createString("databaseDriver");

	public final StringPath databaseName = createString("databaseName");

	public final StringPath databaseUrl = createString("databaseUrl");

	public final StringPath driverUrl = createString("driverUrl");

	public final StringPath endpointUrl = createString("endpointUrl");

	public final StringPath jsonParams = createString("jsonParams");

	public final StringPath namespaceUrl = createString("namespaceUrl");

	public final StringPath updatedBy = createString("updatedBy");

	public final DateTimePath<java.util.Date> updatedTime = createDateTime("updatedTime", java.util.Date.class);

	public final StringPath userName = createString("userName");

	public final StringPath userPassword = createString("userPassword");

	public final StringPath versionId = createString("versionId");

	public final StringPath innerIdx = createString("innerIdx");

	public final com.querydsl.sql.PrimaryKey<TSysConfig> primary = createPrimaryKey(id);

	public QTSysConfig(String variable) {
		super(TSysConfig.class, forVariable(variable), "null", "t_sys_config");
		addMetadata();
	}

	public QTSysConfig(String variable, String schema, String table) {
		super(TSysConfig.class, forVariable(variable), schema, table);
		addMetadata();
	}

	public QTSysConfig(String variable, String schema) {
		super(TSysConfig.class, forVariable(variable), schema, "t_sys_config");
		addMetadata();
	}

	public QTSysConfig(Path<? extends TSysConfig> path) {
		super(path.getType(), path.getMetadata(), "null", "t_sys_config");
		addMetadata();
	}

	public QTSysConfig(PathMetadata metadata) {
		super(TSysConfig.class, metadata, "null", "t_sys_config");
		addMetadata();
	}

	public void addMetadata() {
		addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
		addMetadata(projectId,
				ColumnMetadata.named("PROJECT_ID").withIndex(2).ofType(Types.VARCHAR).withSize(32).notNull());
		addMetadata(platformId,
				ColumnMetadata.named("PLATFORM_ID").withIndex(3).ofType(Types.VARCHAR).withSize(32).notNull());
		addMetadata(sysId, ColumnMetadata.named("SYS_ID").withIndex(4).ofType(Types.VARCHAR).withSize(32).notNull());
		addMetadata(sysConfigType,
				ColumnMetadata.named("SYS_CONFIG_TYPE").withIndex(5).ofType(Types.TINYINT).withSize(1).notNull());
//		addMetadata(hospitalConfigs, ColumnMetadata.named("HOSPITAL_CONFIGS").withIndex(6).ofType(Types.LONGVARCHAR)
//				.withSize(500).notNull());
		addMetadata(versionId,
				ColumnMetadata.named("VERSION_ID").withIndex(6).ofType(Types.VARCHAR).withSize(32).notNull());
		addMetadata(connectionType,
				ColumnMetadata.named("CONNECTION_TYPE").withIndex(7).ofType(Types.VARCHAR).withSize(32).notNull());
		addMetadata(addressUrl,
				ColumnMetadata.named("ADDRESS_URL").withIndex(8).ofType(Types.VARCHAR).withSize(128).notNull());
		addMetadata(endpointUrl,
				ColumnMetadata.named("ENDPOINT_URL").withIndex(9).ofType(Types.VARCHAR).withSize(128).notNull());
		addMetadata(namespaceUrl,
				ColumnMetadata.named("NAMESPACE_URL").withIndex(10).ofType(Types.VARCHAR).withSize(128).notNull());
		addMetadata(databaseName,
				ColumnMetadata.named("DATABASE_NAME").withIndex(11).ofType(Types.VARCHAR).withSize(128).notNull());
		addMetadata(databaseUrl,
				ColumnMetadata.named("DATABASE_URL").withIndex(12).ofType(Types.VARCHAR).withSize(128).notNull());
		addMetadata(databaseDriver,
				ColumnMetadata.named("DATABASE_DRIVER").withIndex(13).ofType(Types.VARCHAR).withSize(128).notNull());
		addMetadata(driverUrl, ColumnMetadata.named("DRIVER_URL").withIndex(14).ofType(Types.VARCHAR).withSize(128));
		addMetadata(jsonParams,
				ColumnMetadata.named("JSON_PARAMS").withIndex(15).ofType(Types.LONGVARCHAR).withSize(65535));
		addMetadata(userName,
				ColumnMetadata.named("USER_NAME").withIndex(16).ofType(Types.VARCHAR).withSize(32).notNull());
		addMetadata(userPassword,
				ColumnMetadata.named("USER_PASSWORD").withIndex(17).ofType(Types.VARCHAR).withSize(128).notNull());
		addMetadata(createdBy,
				ColumnMetadata.named("CREATED_BY").withIndex(18).ofType(Types.VARCHAR).withSize(32).notNull());
		addMetadata(createdTime,
				ColumnMetadata.named("CREATED_TIME").withIndex(19).ofType(Types.TIMESTAMP).withSize(19).notNull());
		addMetadata(updatedBy,
				ColumnMetadata.named("UPDATED_BY").withIndex(20).ofType(Types.VARCHAR).withSize(32).notNull());
		addMetadata(updatedTime,
				ColumnMetadata.named("UPDATED_TIME").withIndex(21).ofType(Types.TIMESTAMP).withSize(19).notNull());
		addMetadata(innerIdx,
				ColumnMetadata.named("INNER_IDX").withIndex(22).ofType(Types.VARCHAR).withSize(32).notNull());
	}

}
