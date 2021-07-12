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
 * QTEtlPulldata is a Querydsl query type for TEtlPulldata
 */
public class QTEtlPulldata extends com.querydsl.sql.RelationalPathBase<TEtlPulldata> {

    private static final long serialVersionUID = 1572070651;

    public static final QTEtlPulldata qTEtlPulldata = new QTEtlPulldata("t_etl_pulldata");
    
    public final StringPath id = createString("id");

    public final StringPath etlGroupId = createString("etlGroupId");
    
	public final DateTimePath<java.util.Date> debugTime  = createDateTime("debugTime", java.util.Date.class);
	
	public final DateTimePath<java.util.Date> createdTime = createDateTime("createdTime", java.util.Date.class);
	
	public final StringPath flowfileJson = createString("flowfileJson");
	
	public final NumberPath<Integer> pageNum = createNumber("errorInfo" , Integer.class);

    public QTEtlPulldata(String variable) {
        super(TEtlPulldata.class, forVariable(variable), "null", "t_etl_pulldata");
        addMetadata();
    }

    public QTEtlPulldata(String variable, String schema, String table) {
        super(TEtlPulldata.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTEtlPulldata(String variable, String schema) {
        super(TEtlPulldata.class, forVariable(variable), schema, "t_etl_pulldata");
        addMetadata();
    }

    public QTEtlPulldata(Path<? extends TEtlPulldata> path) {
        super(path.getType(), path.getMetadata(), "null", "t_etl_pulldata");
        addMetadata();
    }

    public QTEtlPulldata(PathMetadata metadata) {
        super(TEtlPulldata.class, metadata, "null", "t_etl_pulldata");
        addMetadata();
    }

    public void addMetadata() {
    	addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.BIGINT).withSize(20));
        addMetadata(etlGroupId, ColumnMetadata.named("ETL_GROUP_ID").withIndex(2).ofType(Types.VARCHAR).withSize(50));
        addMetadata(pageNum, ColumnMetadata.named("PAGE_NUM").withIndex(3).ofType(Types.INTEGER).withSize(10));
        addMetadata(debugTime, ColumnMetadata.named("DEBUG_TIME").withIndex(4).ofType(Types.TIMESTAMP).withSize(19));
        addMetadata(createdTime, ColumnMetadata.named("CREATED_TIME").withIndex(5).ofType(Types.TIMESTAMP).withSize(19));
        addMetadata(flowfileJson, ColumnMetadata.named("FLOWFILE_JSON").withIndex(6).ofType(Types.LONGVARCHAR));
    }

}

