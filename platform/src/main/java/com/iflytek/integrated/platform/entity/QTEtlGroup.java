package com.iflytek.integrated.platform.entity;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

import java.sql.Types;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;

/**
 * QTArea is a Querydsl query type for TArea
 */
public class QTEtlGroup extends com.querydsl.sql.RelationalPathBase<TEtlGroup> {

	private static final long serialVersionUID = -201223167;

	public static final QTEtlGroup qTEtlGroup = new QTEtlGroup("t_etl_group");

	public final StringPath projectId = createString("projectId");

	public final StringPath platformId = createString("platformId");

	public final StringPath sysId = createString("sysId");

	public final StringPath hospitalId = createString("hospitalId");

	public final StringPath etlGroupId = createString("etlGroupId");

	public final StringPath etlGroupName = createString("etlGroupName");

	public final StringPath createdBy = createString("createdBy");

	public final DateTimePath<java.util.Date> createdTime = createDateTime("createdTime", java.util.Date.class);

	public final StringPath id = createString("id");

	public final StringPath updatedBy = createString("updatedBy");

	public final DateTimePath<java.util.Date> updatedTime = createDateTime("updatedTime", java.util.Date.class);

	public final com.querydsl.sql.PrimaryKey<TEtlGroup> primary = createPrimaryKey(id);

	public QTEtlGroup(String variable) {
		super(TEtlGroup.class, forVariable(variable), "null", "t_etl_group");
		addMetadata();
	}

	public QTEtlGroup(String variable, String schema, String table) {
		super(TEtlGroup.class, forVariable(variable), schema, table);
		addMetadata();
	}

	public QTEtlGroup(String variable, String schema) {
		super(TEtlGroup.class, forVariable(variable), schema, "t_etl_group");
		addMetadata();
	}

	public QTEtlGroup(Path<? extends TEtlGroup> path) {
		super(path.getType(), path.getMetadata(), "null", "t_etl_group");
		addMetadata();
	}

	public QTEtlGroup(PathMetadata metadata) {
		super(TEtlGroup.class, metadata, "null", "t_etl_group");
		addMetadata();
	}

	public void addMetadata() {
		addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
		addMetadata(projectId, ColumnMetadata.named("PROJECT_ID").withIndex(2).ofType(Types.VARCHAR).withSize(32));
		addMetadata(platformId, ColumnMetadata.named("PLATFORM_ID").withIndex(3).ofType(Types.VARCHAR).withSize(32));
		addMetadata(sysId, ColumnMetadata.named("SYS_ID").withIndex(4).ofType(Types.VARCHAR).withSize(32));
		addMetadata(hospitalId, ColumnMetadata.named("HOSPITAL_ID").withIndex(5).ofType(Types.VARCHAR).withSize(32));
		addMetadata(etlGroupId, ColumnMetadata.named("ETL_GROUP_ID").withIndex(6).ofType(Types.VARCHAR).withSize(32));
		addMetadata(etlGroupName,
				ColumnMetadata.named("ETL_GROUP_NAME").withIndex(7).ofType(Types.VARCHAR).withSize(255));
		addMetadata(createdBy, ColumnMetadata.named("CREATED_BY").withIndex(8).ofType(Types.VARCHAR).withSize(32));
		addMetadata(createdTime,
				ColumnMetadata.named("CREATED_TIME").withIndex(9).ofType(Types.TIMESTAMP).withSize(19));
		addMetadata(updatedBy, ColumnMetadata.named("UPDATED_BY").withIndex(10).ofType(Types.VARCHAR).withSize(32));
		addMetadata(updatedTime,
				ColumnMetadata.named("UPDATED_TIME").withIndex(11).ofType(Types.TIMESTAMP).withSize(19));
	}

}
