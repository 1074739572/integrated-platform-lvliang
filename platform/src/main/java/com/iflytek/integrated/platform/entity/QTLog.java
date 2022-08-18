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

    public final StringPath businessRep = createString("businessRep");

    public final NumberPath<Integer> businessRepTime = createNumber("businessRepTime", Integer.class);

    public final StringPath businessReq = createString("businessReq");

    public final StringPath createdBy = createString("createdBy");

    public final DateTimePath<java.util.Date> createdTime = createDateTime("createdTime", java.util.Date.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath businessInterfaceId = createString("businessInterfaceId");

    public final StringPath typeId = createString("typeId");

    public final StringPath requestIdentifier = createString("requestIdentifier");

    public final StringPath status = createString("status");

    public final StringPath updatedBy = createString("updatedBy");

    public final DateTimePath<java.util.Date> updatedTime = createDateTime("updatedTime", java.util.Date.class);

    public final StringPath venderRep = createString("venderRep");

    public final NumberPath<Integer> venderRepTime = createNumber("venderRepTime", Integer.class);

    public final StringPath venderReq = createString("venderReq");

    public final StringPath visitAddr = createString("visitAddr");

    public final NumberPath<Integer> debugreplayFlag = createNumber("debugreplayFlag", Integer.class);

    public final StringPath regConnectionType = createString("regConnectionType");

    public final StringPath publishId = createString("publishId");

    public final StringPath logType = createString("logType");

    public final StringPath logNode = createString("logNode");

    public final StringPath logHeader = createString("logHeader");

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
        addMetadata(businessInterfaceId, ColumnMetadata.named("business_interface_id").withIndex(12).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(visitAddr, ColumnMetadata.named("VISIT_ADDR").withIndex(6).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(businessReq, ColumnMetadata.named("business_req").withIndex(7).ofType(Types.LONGVARCHAR).withSize(1024).notNull());
        addMetadata(venderReq, ColumnMetadata.named("vender_req").withIndex(8).ofType(Types.LONGVARCHAR).withSize(1024).notNull());
        addMetadata(venderRep, ColumnMetadata.named("vender_rep").withIndex(9).ofType(Types.LONGVARCHAR).withSize(65535));
        addMetadata(businessRep, ColumnMetadata.named("business_rep").withIndex(9).ofType(Types.LONGVARCHAR).withSize(65535));
        addMetadata(venderRepTime, ColumnMetadata.named("vender_rep_time").withIndex(11).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(businessRepTime, ColumnMetadata.named("business_rep_time").withIndex(11).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(requestIdentifier, ColumnMetadata.named("request_identifier").withIndex(13).ofType(Types.VARCHAR).withSize(255));
        addMetadata(status, ColumnMetadata.named("status").withIndex(14).ofType(Types.VARCHAR).withSize(1).notNull());
        addMetadata(createdBy, ColumnMetadata.named("created_by").withIndex(15).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(createdTime, ColumnMetadata.named("created_time").withIndex(16).ofType(Types.TIMESTAMP).withSize(19).notNull());
        addMetadata(updatedBy, ColumnMetadata.named("updated_by").withIndex(17).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(updatedTime, ColumnMetadata.named("updated_time").withIndex(18).ofType(Types.TIMESTAMP).withSize(19).notNull());
        addMetadata(typeId, ColumnMetadata.named("type_id").withIndex(19).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(debugreplayFlag, ColumnMetadata.named("debugreplay_flag").withIndex(20).ofType(Types.INTEGER).withSize(1).notNull());
        addMetadata(regConnectionType, ColumnMetadata.named("reg_connection_type").withIndex(20).ofType(Types.INTEGER).withSize(1).notNull());
        addMetadata(publishId, ColumnMetadata.named("publish_id").withIndex(20).ofType(Types.INTEGER).withSize(32).notNull());
        addMetadata(logType, ColumnMetadata.named("log_type").withIndex(21).ofType(Types.VARCHAR).withSize(16));
        addMetadata(logNode, ColumnMetadata.named("log_node").withIndex(22).ofType(Types.VARCHAR).withSize(100));
        addMetadata(logHeader, ColumnMetadata.named("log_header").withIndex(23).ofType(Types.VARCHAR).withSize(255));

        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

