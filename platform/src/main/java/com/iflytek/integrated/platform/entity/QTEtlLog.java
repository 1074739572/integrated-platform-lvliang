package com.iflytek.integrated.platform.entity;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

import java.sql.Types;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;




/**
 * QTEtlLog is a Querydsl query type for TEtlLog
 */
public class QTEtlLog extends com.querydsl.sql.RelationalPathBase<TEtlLog> {

    private static final long serialVersionUID = 1572070651;

    public static final QTEtlLog qTEtlLog = new QTEtlLog("t_etl_log");

    public final DateTimePath<java.util.Date> createdTime = createDateTime("createdTime", java.util.Date.class);

    public final StringPath id = createString("id");
    
    public final StringPath etlGroupId = createString("etlGroupId");
    
    public final StringPath flowName = createString("flowName");
	
	public final DateTimePath<java.util.Date> jobTime  = createDateTime("jobTime", java.util.Date.class);
	
	public final StringPath status = createString("status");
	
	public final StringPath errorInfo = createString("errorInfo");

    public final com.querydsl.sql.PrimaryKey<TEtlLog> primary = createPrimaryKey(id);

    public QTEtlLog(String variable) {
        super(TEtlLog.class, forVariable(variable), "null", "t_etl_log");
        addMetadata();
    }

    public QTEtlLog(String variable, String schema, String table) {
        super(TEtlLog.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTEtlLog(String variable, String schema) {
        super(TEtlLog.class, forVariable(variable), schema, "t_etl_log");
        addMetadata();
    }

    public QTEtlLog(Path<? extends TEtlLog> path) {
        super(path.getType(), path.getMetadata(), "null", "t_etl_log");
        addMetadata();
    }

    public QTEtlLog(PathMetadata metadata) {
        super(TEtlLog.class, metadata, "null", "t_etl_log");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(etlGroupId, ColumnMetadata.named("ETL_GROUP_ID").withIndex(2).ofType(Types.VARCHAR).withSize(50));
        addMetadata(flowName, ColumnMetadata.named("FLOW_NAME").withIndex(3).ofType(Types.VARCHAR).withSize(255));
        addMetadata(jobTime, ColumnMetadata.named("JOB_TIME").withIndex(4).ofType(Types.TIMESTAMP).withSize(19));
        addMetadata(createdTime, ColumnMetadata.named("CREATED_TIME").withIndex(5).ofType(Types.TIMESTAMP).withSize(19));
        addMetadata(status, ColumnMetadata.named("status").withIndex(6).ofType(Types.TINYINT).withSize(1));
        addMetadata(errorInfo, ColumnMetadata.named("ERROR_INFO").withIndex(7).ofType(Types.LONGVARCHAR));
    }

}

