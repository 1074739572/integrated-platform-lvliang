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
 * QTLog is a Querydsl query type for TLog
 */
public class QTLog extends com.querydsl.sql.RelationalPathBase<TLog> {

    private static final long serialVersionUID = 1101898064;

    public static final QTLog qTLog = new QTLog("t_log");

    public final StringPath businessInterfaceId = createString("businessInterfaceId");

    public final StringPath businessRep = createString("businessRep");

    public final NumberPath<Integer> businessRepTime = createNumber("businessRepTime", Integer.class);

    public final StringPath businessReq = createString("businessReq");

    public final StringPath createdBy = createString("createdBy");

    public final DateTimePath<java.util.Date> createdTime = createDateTime("createdTime", java.util.Date.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath platformId = createString("platformId");

    public final StringPath sysId = createString("sysId");

    public final StringPath typeId = createString("typeId");

    public final StringPath projectId = createString("projectId");

    public final StringPath requestIdentifier = createString("requestIdentifier");

    public final StringPath status = createString("status");

    public final StringPath updatedBy = createString("updatedBy");

    public final DateTimePath<java.util.Date> updatedTime = createDateTime("updatedTime", java.util.Date.class);

    public final StringPath venderRep = createString("venderRep");

    public final NumberPath<Integer> venderRepTime = createNumber("venderRepTime", Integer.class);

    public final StringPath venderReq = createString("venderReq");

    public final StringPath visitAddr = createString("visitAddr");

    public final com.querydsl.sql.PrimaryKey<TLog> primary = createPrimaryKey(id);

    public QTLog(String variable) {
        super(TLog.class, forVariable(variable), "null", "t_log");
        addMetadata();
    }

    public QTLog(String variable, String schema, String table) {
        super(TLog.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTLog(String variable, String schema) {
        super(TLog.class, forVariable(variable), schema, "t_log");
        addMetadata();
    }

    public QTLog(Path<? extends TLog> path) {
        super(path.getType(), path.getMetadata(), "null", "t_log");
        addMetadata();
    }

    public QTLog(PathMetadata metadata) {
        super(TLog.class, metadata, "null", "t_log");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(businessInterfaceId, ColumnMetadata.named("BUSINESS_INTERFACE_ID").withIndex(5).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(businessRep, ColumnMetadata.named("BUSINESS_REP").withIndex(10).ofType(Types.LONGVARCHAR).withSize(65535));
        addMetadata(businessRepTime, ColumnMetadata.named("BUSINESS_REP_TIME").withIndex(12).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(businessReq, ColumnMetadata.named("BUSINESS_REQ").withIndex(7).ofType(Types.VARCHAR).withSize(1024).notNull());
        addMetadata(createdBy, ColumnMetadata.named("CREATED_BY").withIndex(15).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(createdTime, ColumnMetadata.named("CREATED_TIME").withIndex(16).ofType(Types.TIMESTAMP).withSize(19).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(platformId, ColumnMetadata.named("PLATFORM_ID").withIndex(3).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(sysId, ColumnMetadata.named("SYS_ID").withIndex(4).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(projectId, ColumnMetadata.named("PROJECT_ID").withIndex(2).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(requestIdentifier, ColumnMetadata.named("REQUEST_IDENTIFIER").withIndex(13).ofType(Types.VARCHAR).withSize(255));
        addMetadata(status, ColumnMetadata.named("STATUS").withIndex(14).ofType(Types.VARCHAR).withSize(1).notNull());
        addMetadata(updatedBy, ColumnMetadata.named("UPDATED_BY").withIndex(17).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(updatedTime, ColumnMetadata.named("UPDATED_TIME").withIndex(18).ofType(Types.TIMESTAMP).withSize(19).notNull());
        addMetadata(venderRep, ColumnMetadata.named("VENDER_REP").withIndex(9).ofType(Types.LONGVARCHAR).withSize(65535));
        addMetadata(venderRepTime, ColumnMetadata.named("VENDER_REP_TIME").withIndex(11).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(venderReq, ColumnMetadata.named("VENDER_REQ").withIndex(8).ofType(Types.VARCHAR).withSize(1024).notNull());
        addMetadata(visitAddr, ColumnMetadata.named("VISIT_ADDR").withIndex(6).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(typeId, ColumnMetadata.named("TYPE_ID").withIndex(19).ofType(Types.VARCHAR).withSize(32).notNull());
    }

}

