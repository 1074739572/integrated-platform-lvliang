package com.iflytek.integrated.platform.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QTInterface is a Querydsl query type for TInterface
 */
public class QTInterface extends com.querydsl.sql.RelationalPathBase<TInterface> {

    private static final long serialVersionUID = 485864005;

    public static final QTInterface qTInterface = new QTInterface("t_interface");

    public final StringPath createdBy = createString("createdBy");

    public final DateTimePath<java.util.Date> createdTime = createDateTime("createdTime", java.util.Date.class);

    public final StringPath id = createString("id");

    public final StringPath interfaceFormat = createString("interfaceFormat");

    public final StringPath interfaceName = createString("interfaceName");

    public final StringPath interfaceUrl = createString("interfaceUrl");

    public final StringPath paramOutStatus = createString("paramOutStatus");

    public final StringPath paramOutStatusSuccess = createString("paramOutStatusSuccess");

    public final StringPath typeId = createString("typeId");

    public final StringPath updatedBy = createString("updatedBy");

    public final DateTimePath<java.util.Date> updatedTime = createDateTime("updatedTime", java.util.Date.class);

    public final com.querydsl.sql.PrimaryKey<TInterface> primary = createPrimaryKey(id);

    public QTInterface(String variable) {
        super(TInterface.class, forVariable(variable), "null", "t_interface");
        addMetadata();
    }

    public QTInterface(String variable, String schema, String table) {
        super(TInterface.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTInterface(String variable, String schema) {
        super(TInterface.class, forVariable(variable), schema, "t_interface");
        addMetadata();
    }

    public QTInterface(Path<? extends TInterface> path) {
        super(path.getType(), path.getMetadata(), "null", "t_interface");
        addMetadata();
    }

    public QTInterface(PathMetadata metadata) {
        super(TInterface.class, metadata, "null", "t_interface");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(createdBy, ColumnMetadata.named("CREATED_BY").withIndex(8).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(createdTime, ColumnMetadata.named("CREATED_TIME").withIndex(9).ofType(Types.TIMESTAMP).withSize(19).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(interfaceFormat, ColumnMetadata.named("INTERFACE_FORMAT").withIndex(5).ofType(Types.VARCHAR).withSize(1024).notNull());
        addMetadata(interfaceName, ColumnMetadata.named("INTERFACE_NAME").withIndex(2).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(interfaceUrl, ColumnMetadata.named("INTERFACE_URL").withIndex(4).ofType(Types.VARCHAR).withSize(64).notNull());
        addMetadata(paramOutStatus, ColumnMetadata.named("PARAM_OUT_STATUS").withIndex(6).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(paramOutStatusSuccess, ColumnMetadata.named("PARAM_OUT_STATUS_SUCCESS").withIndex(7).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(typeId, ColumnMetadata.named("TYPE_ID").withIndex(3).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(updatedBy, ColumnMetadata.named("UPDATED_BY").withIndex(10).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(updatedTime, ColumnMetadata.named("UPDATED_TIME").withIndex(11).ofType(Types.TIMESTAMP).withSize(19).notNull());
    }

}

