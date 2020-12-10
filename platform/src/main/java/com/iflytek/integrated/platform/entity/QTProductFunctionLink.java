package com.iflytek.integrated.platform.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QTProductFunctionLink is a Querydsl query type for TProductFunctionLink
 */
public class QTProductFunctionLink extends com.querydsl.sql.RelationalPathBase<TProductFunctionLink> {

    private static final long serialVersionUID = -223968115;

    public static final QTProductFunctionLink qTProductFunctionLink = new QTProductFunctionLink("t_product_function_link");

    public final StringPath createdBy = createString("createdBy");

    public final DateTimePath<java.util.Date> createdTime = createDateTime("createdTime", java.util.Date.class);

    public final StringPath functionId = createString("functionId");

    public final StringPath id = createString("id");

    public final StringPath productId = createString("productId");

    public final StringPath updatedBy = createString("updatedBy");

    public final DateTimePath<java.util.Date> updatedTime = createDateTime("updatedTime", java.util.Date.class);

    public QTProductFunctionLink(String variable) {
        super(TProductFunctionLink.class, forVariable(variable), "null", "t_product_function_link");
        addMetadata();
    }

    public QTProductFunctionLink(String variable, String schema, String table) {
        super(TProductFunctionLink.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTProductFunctionLink(String variable, String schema) {
        super(TProductFunctionLink.class, forVariable(variable), schema, "t_product_function_link");
        addMetadata();
    }

    public QTProductFunctionLink(Path<? extends TProductFunctionLink> path) {
        super(path.getType(), path.getMetadata(), "null", "t_product_function_link");
        addMetadata();
    }

    public QTProductFunctionLink(PathMetadata metadata) {
        super(TProductFunctionLink.class, metadata, "null", "t_product_function_link");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(createdBy, ColumnMetadata.named("CREATED_BY").withIndex(4).ofType(Types.VARCHAR).withSize(32));
        addMetadata(createdTime, ColumnMetadata.named("CREATED_TIME").withIndex(5).ofType(Types.TIMESTAMP).withSize(19));
        addMetadata(functionId, ColumnMetadata.named("FUNCTION_ID").withIndex(3).ofType(Types.VARCHAR).withSize(32));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.VARCHAR).withSize(32));
        addMetadata(productId, ColumnMetadata.named("PRODUCT_ID").withIndex(2).ofType(Types.VARCHAR).withSize(32));
        addMetadata(updatedBy, ColumnMetadata.named("UPDATED_BY").withIndex(6).ofType(Types.VARCHAR).withSize(32));
        addMetadata(updatedTime, ColumnMetadata.named("UPDATED_TIME").withIndex(7).ofType(Types.TIMESTAMP).withSize(19));
    }

}

