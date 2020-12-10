package com.iflytek.integrated.platform.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QTProjectProductLink is a Querydsl query type for TProjectProductLink
 */
public class QTProjectProductLink extends com.querydsl.sql.RelationalPathBase<TProjectProductLink> {

    private static final long serialVersionUID = -290968284;

    public static final QTProjectProductLink qTProjectProductLink = new QTProjectProductLink("t_project_product_link");

    public final StringPath createdBy = createString("createdBy");

    public final DateTimePath<java.util.Date> createdTime = createDateTime("createdTime", java.util.Date.class);

    public final StringPath id = createString("id");

    public final StringPath productFunctionLinkId = createString("productFunctionLinkId");

    public final StringPath projectId = createString("projectId");

    public final StringPath updatedBy = createString("updatedBy");

    public final DateTimePath<java.util.Date> updatedTime = createDateTime("updatedTime", java.util.Date.class);

    public QTProjectProductLink(String variable) {
        super(TProjectProductLink.class, forVariable(variable), "null", "t_project_product_link");
        addMetadata();
    }

    public QTProjectProductLink(String variable, String schema, String table) {
        super(TProjectProductLink.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTProjectProductLink(String variable, String schema) {
        super(TProjectProductLink.class, forVariable(variable), schema, "t_project_product_link");
        addMetadata();
    }

    public QTProjectProductLink(Path<? extends TProjectProductLink> path) {
        super(path.getType(), path.getMetadata(), "null", "t_project_product_link");
        addMetadata();
    }

    public QTProjectProductLink(PathMetadata metadata) {
        super(TProjectProductLink.class, metadata, "null", "t_project_product_link");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(createdBy, ColumnMetadata.named("CREATED_BY").withIndex(4).ofType(Types.VARCHAR).withSize(32));
        addMetadata(createdTime, ColumnMetadata.named("CREATED_TIME").withIndex(5).ofType(Types.TIMESTAMP).withSize(19));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.VARCHAR).withSize(32));
        addMetadata(productFunctionLinkId, ColumnMetadata.named("PRODUCT_FUNCTION_LINK_ID").withIndex(3).ofType(Types.VARCHAR).withSize(32));
        addMetadata(projectId, ColumnMetadata.named("PROJECT_ID").withIndex(2).ofType(Types.VARCHAR).withSize(32));
        addMetadata(updatedBy, ColumnMetadata.named("UPDATED_BY").withIndex(6).ofType(Types.VARCHAR).withSize(32));
        addMetadata(updatedTime, ColumnMetadata.named("UPDATED_TIME").withIndex(7).ofType(Types.TIMESTAMP).withSize(19));
    }

}

