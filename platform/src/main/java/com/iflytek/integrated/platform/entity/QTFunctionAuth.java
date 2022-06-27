package com.iflytek.integrated.platform.entity;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;

import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;


/**
 * QTArea is a Querydsl query type for TArea
 */
public class QTFunctionAuth extends com.querydsl.sql.RelationalPathBase<TFunctionAuth> {

    private static final long serialVersionUID = -201223167;

    public static final QTFunctionAuth qtFunctionAuth = new QTFunctionAuth("t_function_auth");

    public final StringPath interfaceId = createString("interfaceId");

    public final StringPath publishId = createString("publishId");

    public final StringPath createdBy = createString("createdBy");

    public final DateTimePath<java.util.Date> createdTime = createDateTime("createdTime", java.util.Date.class);

    public final StringPath id = createString("id");

    public final StringPath updatedBy = createString("updatedBy");

    public final DateTimePath<java.util.Date> updatedTime = createDateTime("updatedTime", java.util.Date.class);

    public final com.querydsl.sql.PrimaryKey<TFunctionAuth> primary = createPrimaryKey(id);

    public QTFunctionAuth(String variable) {
        super(TFunctionAuth.class, forVariable(variable), "null", "t_function_auth");
        addMetadata();
    }

    public QTFunctionAuth(String variable, String schema, String table) {
        super(TFunctionAuth.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTFunctionAuth(String variable, String schema) {
        super(TFunctionAuth.class, forVariable(variable), schema, "t_function_auth");
        addMetadata();
    }

    public QTFunctionAuth(Path<? extends TFunctionAuth> path) {
        super(path.getType(), path.getMetadata(), "null", "t_function_auth");
        addMetadata();
    }

    public QTFunctionAuth(PathMetadata metadata) {
        super(TFunctionAuth.class, metadata, "null", "t_function_auth");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(interfaceId, ColumnMetadata.named("INTERFACE_ID").withIndex(2).ofType(Types.VARCHAR).withSize(32));
        addMetadata(publishId, ColumnMetadata.named("PUBLISH_ID").withIndex(3).ofType(Types.VARCHAR).withSize(32));
        addMetadata(createdBy, ColumnMetadata.named("CREATED_BY").withIndex(4).ofType(Types.VARCHAR).withSize(32));
        addMetadata(createdTime, ColumnMetadata.named("CREATED_TIME").withIndex(5).ofType(Types.TIMESTAMP).withSize(19));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(updatedBy, ColumnMetadata.named("UPDATED_BY").withIndex(6).ofType(Types.VARCHAR).withSize(32));
        addMetadata(updatedTime, ColumnMetadata.named("UPDATED_TIME").withIndex(7).ofType(Types.TIMESTAMP).withSize(19));
    }

}

