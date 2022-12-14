package com.iflytek.integrated.platform.entity;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;

import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

/**
 * QTVendorConfig is a Querydsl query type for TSysConfig
 */
public class QTSysRegistry extends com.querydsl.sql.RelationalPathBase<TSysRegistry> {

	private static final long serialVersionUID = -23654882;

	public static final QTSysRegistry qTSysRegistry = new QTSysRegistry("t_sys_registry");

	public final StringPath id = createString("id");

	public final StringPath sysId = createString("sysId");

	public final StringPath addressUrl = createString("addressUrl");

	public final StringPath connectionType = createString("connectionType");

	public final StringPath createdBy = createString("createdBy");

	public final DateTimePath<java.util.Date> createdTime = createDateTime("createdTime", java.util.Date.class);

	public final StringPath databaseDriver = createString("databaseDriver");

	public final StringPath databaseName = createString("databaseName");

	public final StringPath databaseType = createString("databaseType");

	public final StringPath databaseUrl = createString("databaseUrl");

	public final StringPath driverUrl = createString("driverUrl");

	public final StringPath endpointUrl = createString("endpointUrl");

	public final StringPath jsonParams = createString("jsonParams");

	public final StringPath namespaceUrl = createString("namespaceUrl");

	public final StringPath updatedBy = createString("updatedBy");

	public final DateTimePath<java.util.Date> updatedTime = createDateTime("updatedTime", java.util.Date.class);

	public final StringPath userName = createString("userName");

	public final StringPath userPassword = createString("userPassword");

	public final StringPath registryName = createString("registryName");

	public final StringPath useStatus = createString("useStatus");

	public final com.querydsl.sql.PrimaryKey<TSysRegistry> primary = createPrimaryKey(id);

	public QTSysRegistry(String variable) {
		super(TSysRegistry.class, forVariable(variable), "null", "t_sys_registry");
		addMetadata();
	}

	public QTSysRegistry(String variable, String schema, String table) {
		super(TSysRegistry.class, forVariable(variable), schema, table);
		addMetadata();
	}

	public QTSysRegistry(String variable, String schema) {
		super(TSysRegistry.class, forVariable(variable), schema, "t_sys_registry");
		addMetadata();
	}

	public QTSysRegistry(Path<? extends TSysRegistry> path) {
		super(path.getType(), path.getMetadata(), "null", "t_sys_registry");
		addMetadata();
	}

	public QTSysRegistry(PathMetadata metadata) {
		super(TSysRegistry.class, metadata, "null", "t_sys_registry");
		addMetadata();
	}

	public void addMetadata() {
		addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
		addMetadata(sysId, ColumnMetadata.named("SYS_ID").withIndex(4).ofType(Types.VARCHAR).withSize(32).notNull());
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
		addMetadata(databaseType,
				ColumnMetadata.named("DATABASE_TYPE").withIndex(11).ofType(Types.VARCHAR).withSize(50).notNull());
		addMetadata(databaseUrl,
				ColumnMetadata.named("DATABASE_URL").withIndex(12).ofType(Types.VARCHAR).withSize(128).notNull());
		addMetadata(databaseDriver,
				ColumnMetadata.named("DATABASE_DRIVER").withIndex(14).ofType(Types.VARCHAR).withSize(128).notNull());
		addMetadata(driverUrl, ColumnMetadata.named("DRIVER_URL").withIndex(15).ofType(Types.VARCHAR).withSize(128));
		addMetadata(jsonParams,
				ColumnMetadata.named("JSON_PARAMS").withIndex(16).ofType(Types.LONGVARCHAR).withSize(65535));
		addMetadata(userName,
				ColumnMetadata.named("USER_NAME").withIndex(17).ofType(Types.VARCHAR).withSize(32).notNull());
		addMetadata(userPassword,
				ColumnMetadata.named("USER_PASSWORD").withIndex(18).ofType(Types.VARCHAR).withSize(128).notNull());
		addMetadata(createdBy,
				ColumnMetadata.named("CREATED_BY").withIndex(19).ofType(Types.VARCHAR).withSize(32).notNull());
		addMetadata(createdTime,
				ColumnMetadata.named("CREATED_TIME").withIndex(20).ofType(Types.TIMESTAMP).withSize(19).notNull());
		addMetadata(updatedBy,
				ColumnMetadata.named("UPDATED_BY").withIndex(21).ofType(Types.VARCHAR).withSize(32).notNull());
		addMetadata(updatedTime,
				ColumnMetadata.named("UPDATED_TIME").withIndex(22).ofType(Types.TIMESTAMP).withSize(19).notNull());
		addMetadata(registryName,
				ColumnMetadata.named("REGISTRY_NAME").withIndex(23).ofType(Types.VARCHAR).withSize(255).notNull());
		addMetadata(useStatus,
				ColumnMetadata.named("USE_STATUS").withIndex(24).ofType(Types.INTEGER).withSize(2).notNull());
		addMetadata(databaseType,
				ColumnMetadata.named("DATABASE_TYPE").withIndex(13).ofType(Types.VARCHAR).withSize(32).notNull());
	}

}
