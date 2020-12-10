package com.iflytek.integrated.platform.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QTLog is a Querydsl query type for TLog
 */
public class QTLog extends com.querydsl.sql.RelationalPathBase<TLog> {

    private static final long serialVersionUID = 1101898064;

    public static final QTLog qTLog = new QTLog("t_log");

    public final StringPath createdBy = createString("createdBy");

    public final DateTimePath<java.util.Date> createdTime = createDateTime("createdTime", java.util.Date.class);

    public final StringPath id = createString("id");

    public final StringPath interfaceMonitorId = createString("interfaceMonitorId");

    public final StringPath logInfo = createString("logInfo");

    public final StringPath updatedBy = createString("updatedBy");

    public final DateTimePath<java.util.Date> updatedTime = createDateTime("updatedTime", java.util.Date.class);

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
        addMetadata(createdBy, ColumnMetadata.named("CREATED_BY").withIndex(4).ofType(Types.VARCHAR).withSize(32));
        addMetadata(createdTime, ColumnMetadata.named("CREATED_TIME").withIndex(5).ofType(Types.TIMESTAMP).withSize(19));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.VARCHAR).withSize(32));
        addMetadata(interfaceMonitorId, ColumnMetadata.named("INTERFACE_MONITOR_ID").withIndex(2).ofType(Types.VARCHAR).withSize(32));
        addMetadata(logInfo, ColumnMetadata.named("LOG_INFO").withIndex(3).ofType(Types.LONGVARCHAR).withSize(65535));
        addMetadata(updatedBy, ColumnMetadata.named("UPDATED_BY").withIndex(6).ofType(Types.VARCHAR).withSize(32));
        addMetadata(updatedTime, ColumnMetadata.named("UPDATED_TIME").withIndex(7).ofType(Types.TIMESTAMP).withSize(19));
    }

}

