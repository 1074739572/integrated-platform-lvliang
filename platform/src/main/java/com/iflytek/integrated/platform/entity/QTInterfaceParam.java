package com.iflytek.integrated.platform.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QTInterfaceParam is a Querydsl query type for TInterfaceParam
 */
public class QTInterfaceParam extends com.querydsl.sql.RelationalPathBase<TInterfaceParam> {

    private static final long serialVersionUID = -316860088;

    public static final QTInterfaceParam qTInterfaceParam = new QTInterfaceParam("t_interface_param");

    public final StringPath createdBy = createString("createdBy");

    public final DateTimePath<java.util.Date> createdTime = createDateTime("createdTime", java.util.Date.class);

    public final StringPath id = createString("id");

    public final StringPath interfaceId = createString("interfaceId");

    public final StringPath paramInOut = createString("paramInOut");

    public final StringPath paramInstruction = createString("paramInstruction");

    public final NumberPath<Integer> paramLength = createNumber("paramLength", Integer.class);

    public final StringPath paramName = createString("paramName");

    public final StringPath paramType = createString("paramType");

    public final StringPath updatedBy = createString("updatedBy");

    public final DateTimePath<java.util.Date> updatedTime = createDateTime("updatedTime", java.util.Date.class);

    public final NumberPath<Integer> encryptionStatus = createNumber("encryptionStatus", Integer.class);

    public final NumberPath<Integer> maskStatus = createNumber("maskStatus", Integer.class);

    public final com.querydsl.sql.PrimaryKey<TInterfaceParam> primary = createPrimaryKey(id);

    public QTInterfaceParam(String variable) {
        super(TInterfaceParam.class, forVariable(variable), "null", "t_interface_param");
        addMetadata();
    }

    public QTInterfaceParam(String variable, String schema, String table) {
        super(TInterfaceParam.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTInterfaceParam(String variable, String schema) {
        super(TInterfaceParam.class, forVariable(variable), schema, "t_interface_param");
        addMetadata();
    }

    public QTInterfaceParam(Path<? extends TInterfaceParam> path) {
        super(path.getType(), path.getMetadata(), "null", "t_interface_param");
        addMetadata();
    }

    public QTInterfaceParam(PathMetadata metadata) {
        super(TInterfaceParam.class, metadata, "null", "t_interface_param");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(createdBy, ColumnMetadata.named("CREATED_BY").withIndex(8).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(createdTime, ColumnMetadata.named("CREATED_TIME").withIndex(9).ofType(Types.TIMESTAMP).withSize(19).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(interfaceId, ColumnMetadata.named("INTERFACE_ID").withIndex(4).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(paramInOut, ColumnMetadata.named("PARAM_IN_OUT").withIndex(7).ofType(Types.VARCHAR).withSize(1).notNull());
        addMetadata(paramInstruction, ColumnMetadata.named("PARAM_INSTRUCTION").withIndex(3).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(paramLength, ColumnMetadata.named("PARAM_LENGTH").withIndex(6).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(paramName, ColumnMetadata.named("PARAM_NAME").withIndex(2).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(paramType, ColumnMetadata.named("PARAM_TYPE").withIndex(5).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(updatedBy, ColumnMetadata.named("UPDATED_BY").withIndex(10).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(updatedTime, ColumnMetadata.named("UPDATED_TIME").withIndex(11).ofType(Types.TIMESTAMP).withSize(19).notNull());

        addMetadata(encryptionStatus, ColumnMetadata.named("ENCRYPTION_STATUS").withIndex(12).ofType(Types.INTEGER).withSize(2).notNull());
        addMetadata(maskStatus, ColumnMetadata.named("MASK_STATUS").withIndex(13).ofType(Types.INTEGER).withSize(2).notNull());
    }

}

