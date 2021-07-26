package com.iflytek.integrated.platform.entity;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

import java.sql.Types;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;




/**
 * QTEtlDblink is a Querydsl query type for TEtlDblink
 */
public class QTEtlDblink extends com.querydsl.sql.RelationalPathBase<TEtlDblink> {

    private static final long serialVersionUID = 1572070651;

    public static final QTEtlDblink qTEtlDblink = new QTEtlDblink("t_etl_dblink");
    
    public final StringPath id = createString("id");

    public final StringPath etlGroupId = createString("etlGroupId");
    
	public final StringPath etlProcessorId = createString("etlProcessorId");
	
	public final StringPath dbConfigId = createString("dbConfigId");
	
    public QTEtlDblink(String variable) {
        super(TEtlDblink.class, forVariable(variable), "null", "t_etl_dblink");
        addMetadata();
    }

    public QTEtlDblink(String variable, String schema, String table) {
        super(TEtlDblink.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTEtlDblink(String variable, String schema) {
        super(TEtlDblink.class, forVariable(variable), schema, "t_etl_dblink");
        addMetadata();
    }

    public QTEtlDblink(Path<? extends TEtlDblink> path) {
        super(path.getType(), path.getMetadata(), "null", "t_etl_dblink");
        addMetadata();
    }

    public QTEtlDblink(PathMetadata metadata) {
        super(TEtlDblink.class, metadata, "null", "t_etl_dblink");
        addMetadata();
    }

    public void addMetadata() {
    	addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.BIGINT).withSize(20));
        addMetadata(etlGroupId, ColumnMetadata.named("ETL_GROUP_ID").withIndex(2).ofType(Types.VARCHAR).withSize(50));
        addMetadata(etlProcessorId, ColumnMetadata.named("ETL_PROCESSOR_ID").withIndex(3).ofType(Types.VARCHAR).withSize(50));
        addMetadata(dbConfigId, ColumnMetadata.named("DB_CONFIG_ID").withIndex(4).ofType(Types.VARCHAR).withSize(32));
    }

}

