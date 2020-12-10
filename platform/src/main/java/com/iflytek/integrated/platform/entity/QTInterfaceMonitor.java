package com.iflytek.integrated.platform.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QTInterfaceMonitor is a Querydsl query type for TInterfaceMonitor
 */
public class QTInterfaceMonitor extends com.querydsl.sql.RelationalPathBase<TInterfaceMonitor> {

    private static final long serialVersionUID = -1825014955;

    public static final QTInterfaceMonitor qTInterfaceMonitor = new QTInterfaceMonitor("t_interface_monitor");

    public final StringPath createdBy = createString("createdBy");

    public final DateTimePath<java.util.Date> createdTime = createDateTime("createdTime", java.util.Date.class);

    public final StringPath functionId = createString("functionId");

    public final StringPath hospitalId = createString("hospitalId");

    public final StringPath id = createString("id");

    public final StringPath interfaceId = createString("interfaceId");

    public final StringPath productId = createString("productId");

    public final StringPath projectId = createString("projectId");

    public final StringPath status = createString("status");

    public final StringPath updatedBy = createString("updatedBy");

    public final DateTimePath<java.util.Date> updatedTime = createDateTime("updatedTime", java.util.Date.class);

    public QTInterfaceMonitor(String variable) {
        super(TInterfaceMonitor.class, forVariable(variable), "null", "t_interface_monitor");
        addMetadata();
    }

    public QTInterfaceMonitor(String variable, String schema, String table) {
        super(TInterfaceMonitor.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTInterfaceMonitor(String variable, String schema) {
        super(TInterfaceMonitor.class, forVariable(variable), schema, "t_interface_monitor");
        addMetadata();
    }

    public QTInterfaceMonitor(Path<? extends TInterfaceMonitor> path) {
        super(path.getType(), path.getMetadata(), "null", "t_interface_monitor");
        addMetadata();
    }

    public QTInterfaceMonitor(PathMetadata metadata) {
        super(TInterfaceMonitor.class, metadata, "null", "t_interface_monitor");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(createdBy, ColumnMetadata.named("CREATED_BY").withIndex(8).ofType(Types.VARCHAR).withSize(32));
        addMetadata(createdTime, ColumnMetadata.named("CREATED_TIME").withIndex(9).ofType(Types.TIMESTAMP).withSize(19));
        addMetadata(functionId, ColumnMetadata.named("FUNCTION_ID").withIndex(5).ofType(Types.VARCHAR).withSize(32));
        addMetadata(hospitalId, ColumnMetadata.named("HOSPITAL_ID").withIndex(6).ofType(Types.VARCHAR).withSize(32));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.VARCHAR).withSize(32));
        addMetadata(interfaceId, ColumnMetadata.named("INTERFACE_ID").withIndex(3).ofType(Types.VARCHAR).withSize(32));
        addMetadata(productId, ColumnMetadata.named("PRODUCT_ID").withIndex(4).ofType(Types.VARCHAR).withSize(32));
        addMetadata(projectId, ColumnMetadata.named("PROJECT_ID").withIndex(2).ofType(Types.VARCHAR).withSize(32));
        addMetadata(status, ColumnMetadata.named("STATUS").withIndex(7).ofType(Types.VARCHAR).withSize(1));
        addMetadata(updatedBy, ColumnMetadata.named("UPDATED_BY").withIndex(10).ofType(Types.VARCHAR).withSize(32));
        addMetadata(updatedTime, ColumnMetadata.named("UPDATED_TIME").withIndex(11).ofType(Types.TIMESTAMP).withSize(19));
    }

}

