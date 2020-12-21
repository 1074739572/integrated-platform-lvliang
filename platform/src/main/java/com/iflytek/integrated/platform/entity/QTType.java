package com.iflytek.integrated.platform.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QTType is a Querydsl query type for TType
 */
public class QTType extends com.querydsl.sql.RelationalPathBase<TType> {

    private static final long serialVersionUID = -200650066;

    public static final QTType qTType = new QTType("t_type");

    public final StringPath createdBy = createString("createdBy");

    public final DateTimePath<java.util.Date> createdTime = createDateTime("createdTime", java.util.Date.class);

    public final StringPath id = createString("id");

    public final NumberPath<Integer> type = createNumber("type", Integer.class);

    public final StringPath typeCode = createString("typeCode");

    public final StringPath typeName = createString("typeName");

    public final StringPath updatedBy = createString("updatedBy");

    public final DateTimePath<java.util.Date> updatedTime = createDateTime("updatedTime", java.util.Date.class);

    public final com.querydsl.sql.PrimaryKey<TType> primary = createPrimaryKey(id);

    public QTType(String variable) {
        super(TType.class, forVariable(variable), "null", "t_type");
        addMetadata();
    }

    public QTType(String variable, String schema, String table) {
        super(TType.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTType(String variable, String schema) {
        super(TType.class, forVariable(variable), schema, "t_type");
        addMetadata();
    }

    public QTType(Path<? extends TType> path) {
        super(path.getType(), path.getMetadata(), "null", "t_type");
        addMetadata();
    }

    public QTType(PathMetadata metadata) {
        super(TType.class, metadata, "null", "t_type");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(createdBy, ColumnMetadata.named("CREATED_BY").withIndex(5).ofType(Types.VARCHAR).withSize(32));
        addMetadata(createdTime, ColumnMetadata.named("CREATED_TIME").withIndex(6).ofType(Types.TIMESTAMP).withSize(19).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(type, ColumnMetadata.named("TYPE").withIndex(4).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(typeCode, ColumnMetadata.named("TYPE_CODE").withIndex(3).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(typeName, ColumnMetadata.named("TYPE_NAME").withIndex(2).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(updatedBy, ColumnMetadata.named("UPDATED_BY").withIndex(7).ofType(Types.VARCHAR).withSize(32));
        addMetadata(updatedTime, ColumnMetadata.named("UPDATED_TIME").withIndex(8).ofType(Types.TIMESTAMP).withSize(19));
    }

}

