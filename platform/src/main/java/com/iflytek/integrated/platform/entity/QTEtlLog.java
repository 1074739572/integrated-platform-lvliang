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
 * QTEtlLog is a Querydsl query type for TEtlLog
 */
public class QTEtlLog extends com.querydsl.sql.RelationalPathBase<TEtlLog> {

    private static final long serialVersionUID = 1572070651;

    public static final QTEtlLog qTEtlLog = new QTEtlLog("t_etl_log");

    public final DateTimePath<java.util.Date> createdTime = createDateTime("createdTime", java.util.Date.class);

    public final NumberPath<Long> id = createNumber("id" , Long.class);
    
    public final StringPath etlGroupId = createString("etlGroupId");
    
    public final StringPath flowName = createString("flowName");
	
	public final DateTimePath<java.util.Date> jobTime  = createDateTime("jobTime", java.util.Date.class);
	
	public final StringPath exeJobId = createString("exeJobId");
	
	public final NumberPath<Integer> exeBatchNo = createNumber("exeBatchNo", Integer.class);
	
	public final NumberPath<Integer> status = createNumber("status" , Integer.class);
	
	public final StringPath errorInfo = createString("errorInfo");

	public final NumberPath<Integer> batchReadCount = createNumber("batchReadCount", Integer.class);
	
	public final NumberPath<Integer> batchWriteErrorcount = createNumber("batchWriteErrorcount", Integer.class);

    public final StringPath QIResult = createString("QIResult");

    public final NumberPath<Integer> effectWriteCount = createNumber("effectWriteCount", Integer.class);

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
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.BIGINT).withSize(32).notNull());
        addMetadata(etlGroupId, ColumnMetadata.named("ETL_GROUP_ID").withIndex(2).ofType(Types.VARCHAR).withSize(50));
        addMetadata(flowName, ColumnMetadata.named("FLOW_NAME").withIndex(3).ofType(Types.VARCHAR).withSize(255));
        addMetadata(jobTime, ColumnMetadata.named("JOB_TIME").withIndex(4).ofType(Types.TIMESTAMP).withSize(19));
        addMetadata(exeJobId, ColumnMetadata.named("exe_job_id").withIndex(5).ofType(Types.VARCHAR).withSize(50));
        addMetadata(exeBatchNo, ColumnMetadata.named("exe_batch_no").withIndex(6).ofType(Types.INTEGER).withSize(11));
        addMetadata(createdTime, ColumnMetadata.named("CREATED_TIME").withIndex(7).ofType(Types.TIMESTAMP).withSize(19));
        addMetadata(status, ColumnMetadata.named("status").withIndex(8).ofType(Types.TINYINT).withSize(1));
        addMetadata(errorInfo, ColumnMetadata.named("ERROR_INFO").withIndex(9).ofType(Types.LONGVARCHAR));
        addMetadata(batchReadCount, ColumnMetadata.named("batch_read_count").withIndex(10).ofType(Types.INTEGER).withSize(11));
        addMetadata(batchWriteErrorcount, ColumnMetadata.named("batch_write_errorcount").withIndex(11).ofType(Types.INTEGER).withSize(11));
        addMetadata(QIResult, ColumnMetadata.named("QI_RESULT").withIndex(12).ofType(Types.LONGVARCHAR));
        addMetadata(effectWriteCount, ColumnMetadata.named("effect_write_count").withIndex(13).ofType(Types.INTEGER).withSize(11));
    }

}

