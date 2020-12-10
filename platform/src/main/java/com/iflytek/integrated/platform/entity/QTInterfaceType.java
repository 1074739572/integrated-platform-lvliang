package com.iflytek.integrated.platform.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QTInterfaceType is a Querydsl query type for TInterfaceType
 */
public class QTInterfaceType extends com.querydsl.sql.RelationalPathBase<TInterfaceType> {

    private static final long serialVersionUID = 1791036191;

    public static final QTInterfaceType qTInterfaceType = new QTInterfaceType("t_interface_type");

    public final StringPath createdBy = createString("createdBy");

    public final DateTimePath<java.util.Date> createdTime = createDateTime("createdTime", java.util.Date.class);

    public final StringPath id = createString("id");

    public final StringPath interfaceTypeCode = createString("interfaceTypeCode");

    public final StringPath interfaceTypeName = createString("interfaceTypeName");

    public final StringPath updatedBy = createString("updatedBy");

    public final DateTimePath<java.util.Date> updatedTime = createDateTime("updatedTime", java.util.Date.class);

    public QTInterfaceType(String variable) {
        super(TInterfaceType.class, forVariable(variable), "null", "t_interface_type");
        addMetadata();
    }

    public QTInterfaceType(String variable, String schema, String table) {
        super(TInterfaceType.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTInterfaceType(String variable, String schema) {
        super(TInterfaceType.class, forVariable(variable), schema, "t_interface_type");
        addMetadata();
    }

    public QTInterfaceType(Path<? extends TInterfaceType> path) {
        super(path.getType(), path.getMetadata(), "null", "t_interface_type");
        addMetadata();
    }

    public QTInterfaceType(PathMetadata metadata) {
        super(TInterfaceType.class, metadata, "null", "t_interface_type");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(createdBy, ColumnMetadata.named("CREATED_BY").withIndex(4).ofType(Types.VARCHAR).withSize(32));
        addMetadata(createdTime, ColumnMetadata.named("CREATED_TIME").withIndex(5).ofType(Types.TIMESTAMP).withSize(19));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.VARCHAR).withSize(32));
        addMetadata(interfaceTypeCode, ColumnMetadata.named("INTERFACE_TYPE_CODE").withIndex(3).ofType(Types.VARCHAR).withSize(32));
        addMetadata(interfaceTypeName, ColumnMetadata.named("INTERFACE_TYPE_NAME").withIndex(2).ofType(Types.VARCHAR).withSize(32));
        addMetadata(updatedBy, ColumnMetadata.named("UPDATED_BY").withIndex(6).ofType(Types.VARCHAR).withSize(32));
        addMetadata(updatedTime, ColumnMetadata.named("UPDATED_TIME").withIndex(7).ofType(Types.TIMESTAMP).withSize(19));
    }

}

