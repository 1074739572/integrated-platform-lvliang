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

    public final StringPath businessInterfaceId = createString("businessInterfaceId");

    public final StringPath createdBy = createString("createdBy");

    public final DateTimePath<java.util.Date> createdTime = createDateTime("createdTime", java.util.Date.class);

    public final NumberPath<Integer> errorCount = createNumber("errorCount", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath platformId = createString("platformId");

    public final StringPath productFunctionLinkId = createString("productFunctionLinkId");

    public final StringPath projectId = createString("projectId");

    public final StringPath status = createString("status");

    public final NumberPath<Integer> successCount = createNumber("successCount", Integer.class);

    public final StringPath updatedBy = createString("updatedBy");

    public final DateTimePath<java.util.Date> updatedTime = createDateTime("updatedTime", java.util.Date.class);

    public final com.querydsl.sql.PrimaryKey<TInterfaceMonitor> primary = createPrimaryKey(id);

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
        addMetadata(businessInterfaceId, ColumnMetadata.named("BUSINESS_INTERFACE_ID").withIndex(5).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(createdBy, ColumnMetadata.named("CREATED_BY").withIndex(9).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(createdTime, ColumnMetadata.named("CREATED_TIME").withIndex(10).ofType(Types.TIMESTAMP).withSize(19).notNull());
        addMetadata(errorCount, ColumnMetadata.named("ERROR_COUNT").withIndex(7).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(platformId, ColumnMetadata.named("PLATFORM_ID").withIndex(3).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(productFunctionLinkId, ColumnMetadata.named("PRODUCT_FUNCTION_LINK_ID").withIndex(4).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(projectId, ColumnMetadata.named("PROJECT_ID").withIndex(2).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(status, ColumnMetadata.named("STATUS").withIndex(8).ofType(Types.VARCHAR).withSize(1).notNull());
        addMetadata(successCount, ColumnMetadata.named("SUCCESS_COUNT").withIndex(6).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(updatedBy, ColumnMetadata.named("UPDATED_BY").withIndex(11).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(updatedTime, ColumnMetadata.named("UPDATED_TIME").withIndex(12).ofType(Types.TIMESTAMP).withSize(19).notNull());
    }

}

