package com.iflytek.integrated.platform.entity;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;

import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

/**
 * QTArea is a Querydsl query type for TArea
 */
public class QTEtlFlow extends com.querydsl.sql.RelationalPathBase<TEtlFlow> {

	private static final long serialVersionUID = -201223167;

	public static final QTEtlFlow qTEtlFlow = new QTEtlFlow("t_etl_flow");
	
	public final StringPath tplId = createString("tplId");
	
	public final StringPath groupId = createString("groupId");

	public final StringPath flowName = createString("flowName");

	public final StringPath etlGroupId = createString("etlGroupId");
	
	public final StringPath etlEntryGroupId = createString("etlEntryGroupId");
	
	public final StringPath flowConfig = createString("flowConfig");
	
	public final StringPath flowDesp = createString("flowDesp");
	
	public final StringPath flowTplName = createString("flowTplName");
	
	public final StringPath funTplNames = createString("funTplNames");

	public final StringPath createdBy = createString("createdBy");

	public final DateTimePath<java.util.Date> createdTime = createDateTime("createdTime", java.util.Date.class);

	public final StringPath id = createString("id");

	public final StringPath updatedBy = createString("updatedBy");

	public final DateTimePath<java.util.Date> updatedTime = createDateTime("updatedTime", java.util.Date.class);

	public final StringPath status = createString("status");
	
	public final StringPath parentGroupId = createString("parentGroupId");

	public final com.querydsl.sql.PrimaryKey<TEtlFlow> primary = createPrimaryKey(id);

	public QTEtlFlow(String variable) {
		super(TEtlFlow.class, forVariable(variable), "null", "t_etl_flow");
		addMetadata();
	}

	public QTEtlFlow(String variable, String schema, String table) {
		super(TEtlFlow.class, forVariable(variable), schema, table);
		addMetadata();
	}

	public QTEtlFlow(String variable, String schema) {
		super(TEtlFlow.class, forVariable(variable), schema, "t_etl_flow");
		addMetadata();
	}

	public QTEtlFlow(Path<? extends TEtlFlow> path) {
		super(path.getType(), path.getMetadata(), "null", "t_etl_flow");
		addMetadata();
	}

	public QTEtlFlow(PathMetadata metadata) {
		super(TEtlFlow.class, metadata, "null", "t_etl_flow");
		addMetadata();
	}

	public void addMetadata() {
		addMetadata(groupId, ColumnMetadata.named("GROUP_ID").withIndex(2).ofType(Types.VARCHAR).withSize(32));
		addMetadata(flowName, ColumnMetadata.named("FLOW_NAME").withIndex(3).ofType(Types.VARCHAR).withSize(255));
		addMetadata(etlGroupId, ColumnMetadata.named("ETL_GROUP_ID").withIndex(4).ofType(Types.VARCHAR).withSize(32));
		addMetadata(flowConfig, ColumnMetadata.named("FLOW_CONFIG").withIndex(5).ofType(Types.LONGVARCHAR));
		addMetadata(flowDesp, ColumnMetadata.named("FLOW_DESP").withIndex(6).ofType(Types.VARCHAR));
		addMetadata(flowTplName, ColumnMetadata.named("FLOW_TPL_NAME").withIndex(7).ofType(Types.VARCHAR));
		addMetadata(funTplNames, ColumnMetadata.named("FUN_TPL_NAMES").withIndex(8).ofType(Types.VARCHAR));
		addMetadata(status, ColumnMetadata.named("STATUS").withIndex(9).ofType(Types.VARCHAR).withSize(1).notNull());
		addMetadata(createdBy, ColumnMetadata.named("CREATED_BY").withIndex(10).ofType(Types.VARCHAR).withSize(32));
		addMetadata(createdTime,ColumnMetadata.named("CREATED_TIME").withIndex(11).ofType(Types.TIMESTAMP).withSize(19));
		addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
		addMetadata(updatedBy, ColumnMetadata.named("UPDATED_BY").withIndex(12).ofType(Types.VARCHAR).withSize(32));
		addMetadata(updatedTime,
				ColumnMetadata.named("UPDATED_TIME").withIndex(13).ofType(Types.TIMESTAMP).withSize(19));
		addMetadata(tplId, ColumnMetadata.named("TPL_ID").withIndex(14).ofType(Types.VARCHAR).withSize(32));
		addMetadata(etlEntryGroupId, ColumnMetadata.named("ETL_ENTRY_GROUP_ID").withIndex(15).ofType(Types.VARCHAR).withSize(50));
		addMetadata(parentGroupId, ColumnMetadata.named("PARENT_GROUP_ID").withIndex(15).ofType(Types.VARCHAR).withSize(50));
		
	}

}
