package com.iflytek.integrated.platform.entity;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;

import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

/**
 * QTVendorConfig is a Querydsl query type for TSysConfig
 */
public class QTSysPublish extends com.querydsl.sql.RelationalPathBase<TSysPublish> {

	private static final long serialVersionUID = -23654882;

	public static final QTSysPublish qTSysPublish = new QTSysPublish("t_sys_publish");

	public final StringPath id = createString("id");

	public final StringPath sysId = createString("sysId");

	public final StringPath addressUrl = createString("addressUrl");

	public final StringPath connectionType = createString("connectionType");

	public final StringPath createdBy = createString("createdBy");

	public final DateTimePath<java.util.Date> createdTime = createDateTime("createdTime", java.util.Date.class);

	public final StringPath publishName = createString("publishName");

	public final StringPath isAuthen = createString("isAuthen");

	public final StringPath limitIps = createString("limitIps");

	public final StringPath updatedBy = createString("updatedBy");

	public final StringPath isValid = createString("isValid");

	public final StringPath signKey = createString("signKey");

	public final StringPath asyncResponse = createString("asyncResponse");

	public final DateTimePath<java.util.Date> updatedTime = createDateTime("updatedTime", java.util.Date.class);

	public final com.querydsl.sql.PrimaryKey<TSysPublish> primary = createPrimaryKey(id);

	public QTSysPublish(String variable) {
		super(TSysPublish.class, forVariable(variable), "null", "t_sys_publish");
		addMetadata();
	}

	public QTSysPublish(String variable, String schema, String table) {
		super(TSysPublish.class, forVariable(variable), schema, table);
		addMetadata();
	}

	public QTSysPublish(String variable, String schema) {
		super(TSysPublish.class, forVariable(variable), schema, "t_sys_publish");
		addMetadata();
	}

	public QTSysPublish(Path<? extends TSysPublish> path) {
		super(path.getType(), path.getMetadata(), "null", "t_sys_publish");
		addMetadata();
	}

	public QTSysPublish(PathMetadata metadata) {
		super(TSysPublish.class, metadata, "null", "t_sys_publish");
		addMetadata();
	}

	public void addMetadata() {
		addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
		addMetadata(sysId, ColumnMetadata.named("SYS_ID").withIndex(2).ofType(Types.VARCHAR).withSize(32).notNull());
		addMetadata(connectionType,
				ColumnMetadata.named("CONNECTION_TYPE").withIndex(4).ofType(Types.VARCHAR).withSize(32).notNull());
		addMetadata(addressUrl,
				ColumnMetadata.named("ADDRESS_URL").withIndex(5).ofType(Types.VARCHAR).withSize(128).notNull());
		addMetadata(publishName,
				ColumnMetadata.named("PUBLISH_NAME").withIndex(6).ofType(Types.VARCHAR).withSize(255).notNull());
		addMetadata(isAuthen,
				ColumnMetadata.named("IS_AUTHEN").withIndex(7).ofType(Types.VARCHAR).withSize(1).notNull());
		addMetadata(limitIps,
				ColumnMetadata.named("LIMIT_IPS").withIndex(8).ofType(Types.LONGVARCHAR).withSize(65535).notNull());
		addMetadata(createdBy,
				ColumnMetadata.named("CREATED_BY").withIndex(9).ofType(Types.VARCHAR).withSize(32).notNull());
		addMetadata(createdTime,
				ColumnMetadata.named("CREATED_TIME").withIndex(10).ofType(Types.TIMESTAMP).withSize(19).notNull());
		addMetadata(updatedBy,
				ColumnMetadata.named("UPDATED_BY").withIndex(11).ofType(Types.VARCHAR).withSize(32).notNull());
		addMetadata(updatedTime,
				ColumnMetadata.named("UPDATED_TIME").withIndex(12).ofType(Types.TIMESTAMP).withSize(19).notNull());
		addMetadata(isValid,
				ColumnMetadata.named("IS_VALID").withIndex(13).ofType(Types.VARCHAR).withSize(1).notNull());
		addMetadata(signKey,
				ColumnMetadata.named("SIGN_KEY").withIndex(14).ofType(Types.VARCHAR).withSize(32).notNull());
		addMetadata(asyncResponse,
				ColumnMetadata.named("ASYNC_RESPONSE").withIndex(15).ofType(Types.VARCHAR).withSize(1000).notNull());
	}

}
