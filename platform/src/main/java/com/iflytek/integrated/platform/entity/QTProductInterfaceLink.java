package com.iflytek.integrated.platform.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QTProductInterfaceLink is a Querydsl query type for TProductInterfaceLink
 */
public class QTProductInterfaceLink extends com.querydsl.sql.RelationalPathBase<TProductInterfaceLink> {

    private static final long serialVersionUID = 126928792;

    public static final QTProductInterfaceLink qTProductInterfaceLink = new QTProductInterfaceLink("t_product_interface_link");

    public final StringPath createdBy = createString("createdBy");

    public final DateTimePath<java.util.Date> createdTime = createDateTime("createdTime", java.util.Date.class);

    public final StringPath id = createString("id");

    public final StringPath interfaceId = createString("interfaceId");

    public final StringPath productId = createString("productId");

    public final StringPath updatedBy = createString("updatedBy");

    public final DateTimePath<java.util.Date> updatedTime = createDateTime("updatedTime", java.util.Date.class);

    public QTProductInterfaceLink(String variable) {
        super(TProductInterfaceLink.class, forVariable(variable), "null", "t_product_interface_link");
        addMetadata();
    }

    public QTProductInterfaceLink(String variable, String schema, String table) {
        super(TProductInterfaceLink.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTProductInterfaceLink(String variable, String schema) {
        super(TProductInterfaceLink.class, forVariable(variable), schema, "t_product_interface_link");
        addMetadata();
    }

    public QTProductInterfaceLink(Path<? extends TProductInterfaceLink> path) {
        super(path.getType(), path.getMetadata(), "null", "t_product_interface_link");
        addMetadata();
    }

    public QTProductInterfaceLink(PathMetadata metadata) {
        super(TProductInterfaceLink.class, metadata, "null", "t_product_interface_link");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(createdBy, ColumnMetadata.named("CREATED_BY").withIndex(4).ofType(Types.VARCHAR).withSize(32));
        addMetadata(createdTime, ColumnMetadata.named("CREATED_TIME").withIndex(5).ofType(Types.TIMESTAMP).withSize(19));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.VARCHAR).withSize(32));
        addMetadata(interfaceId, ColumnMetadata.named("INTERFACE_ID").withIndex(3).ofType(Types.VARCHAR).withSize(32));
        addMetadata(productId, ColumnMetadata.named("PRODUCT_ID").withIndex(2).ofType(Types.VARCHAR).withSize(32));
        addMetadata(updatedBy, ColumnMetadata.named("UPDATED_BY").withIndex(6).ofType(Types.VARCHAR).withSize(32));
        addMetadata(updatedTime, ColumnMetadata.named("UPDATED_TIME").withIndex(7).ofType(Types.TIMESTAMP).withSize(19));
    }

}

