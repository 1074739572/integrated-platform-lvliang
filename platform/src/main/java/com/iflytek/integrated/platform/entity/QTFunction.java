package com.iflytek.integrated.platform.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QTFunction is a Querydsl query type for TFunction
 */
public class QTFunction extends com.querydsl.sql.RelationalPathBase<TFunction> {

    private static final long serialVersionUID = -420717236;

    public static final QTFunction qTFunction = new QTFunction("t_function");

    public final StringPath createdBy = createString("createdBy");

    public final DateTimePath<java.util.Date> createdTime = createDateTime("createdTime", java.util.Date.class);

    public final StringPath functionCode = createString("functionCode");

    public final StringPath functionName = createString("functionName");

    public final StringPath id = createString("id");

    public final StringPath updatedBy = createString("updatedBy");

    public final DateTimePath<java.util.Date> updatedTime = createDateTime("updatedTime", java.util.Date.class);

    public QTFunction(String variable) {
        super(TFunction.class, forVariable(variable), "null", "t_function");
        addMetadata();
    }

    public QTFunction(String variable, String schema, String table) {
        super(TFunction.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTFunction(String variable, String schema) {
        super(TFunction.class, forVariable(variable), schema, "t_function");
        addMetadata();
    }

    public QTFunction(Path<? extends TFunction> path) {
        super(path.getType(), path.getMetadata(), "null", "t_function");
        addMetadata();
    }

    public QTFunction(PathMetadata metadata) {
        super(TFunction.class, metadata, "null", "t_function");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(createdBy, ColumnMetadata.named("CREATED_BY").withIndex(4).ofType(Types.VARCHAR).withSize(32));
        addMetadata(createdTime, ColumnMetadata.named("CREATED_TIME").withIndex(5).ofType(Types.TIMESTAMP).withSize(19));
        addMetadata(functionCode, ColumnMetadata.named("FUNCTION_CODE").withIndex(3).ofType(Types.VARCHAR).withSize(32));
        addMetadata(functionName, ColumnMetadata.named("FUNCTION_NAME").withIndex(2).ofType(Types.VARCHAR).withSize(32));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.VARCHAR).withSize(32));
        addMetadata(updatedBy, ColumnMetadata.named("UPDATED_BY").withIndex(6).ofType(Types.VARCHAR).withSize(32));
        addMetadata(updatedTime, ColumnMetadata.named("UPDATED_TIME").withIndex(7).ofType(Types.TIMESTAMP).withSize(19));
    }

}

