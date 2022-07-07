package com.iflytek.integrated.platform.entity;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;

import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;


/**
 * QTInterface is a Querydsl query type for TInterface
 */
public class QTInterfaceHis extends com.querydsl.sql.RelationalPathBase<TInterfaceHis> {

    private static final long serialVersionUID = 485864005;

    public static final QTInterfaceHis qTInterfaceHis = new QTInterfaceHis("t_interface_his");

    public final StringPath createdBy = createString("createdBy");

    public final DateTimePath<java.util.Date> createdTime = createDateTime("createdTime", java.util.Date.class);

    public final StringPath id = createString("id");

    public final StringPath inParamFormat = createString("inParamFormat");

    public final StringPath interfaceName = createString("interfaceName");

    public final StringPath interfaceUrl = createString("interfaceUrl");

    public final StringPath outParamFormat = createString("outParamFormat");

    public final StringPath paramOutStatus = createString("paramOutStatus");

    public final StringPath paramOutStatusSuccess = createString("paramOutStatusSuccess");

    public final StringPath typeId = createString("typeId");

    public final StringPath updatedBy = createString("updatedBy");

    public final StringPath inParamSchema = createString("inParamSchema");

    public final StringPath inParamFormatType = createString("inParamFormatType");

    public final StringPath outParamSchema = createString("outParamSchema");

    public final StringPath outParamFormatType = createString("outParamFormatType");

    public final StringPath allowLogDiscard = createString("allowLogDiscard");

    public final NumberPath<Integer> interfaceType = createNumber("interfaceType", Integer.class);

    public final NumberPath<Integer> asyncFlag = createNumber("asyncFlag", Integer.class);

    public final DateTimePath<java.util.Date> updatedTime = createDateTime("updatedTime", java.util.Date.class);

    public final NumberPath<Integer> encryptionType = createNumber("encryptionType", Integer.class);

    public final NumberPath<Integer> maskPosStart = createNumber("maskPosStart", Integer.class);

    public final NumberPath<Integer> maskPosEnd = createNumber("maskPosEnd", Integer.class);

    public final com.querydsl.sql.PrimaryKey<TInterfaceHis> primary = createPrimaryKey(id);

    public final StringPath originInterfaceId = createString("originInterfaceId");

    public QTInterfaceHis(String variable) {
        super(TInterfaceHis.class, forVariable(variable), "null", "t_interface_his");
        addMetadata();
    }

    public QTInterfaceHis(String variable, String schema, String table) {
        super(TInterfaceHis.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTInterfaceHis(String variable, String schema) {
        super(TInterfaceHis.class, forVariable(variable), schema, "t_interface_his");
        addMetadata();
    }

    public QTInterfaceHis(Path<? extends TInterfaceHis> path) {
        super(path.getType(), path.getMetadata(), "null", "t_interface_his");
        addMetadata();
    }

    public QTInterfaceHis(PathMetadata metadata) {
        super(TInterfaceHis.class, metadata, "null", "t_interface_his");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(createdBy, ColumnMetadata.named("CREATED_BY").withIndex(10).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(createdTime, ColumnMetadata.named("CREATED_TIME").withIndex(11).ofType(Types.TIMESTAMP).withSize(19).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(inParamFormat, ColumnMetadata.named("IN_PARAM_FORMAT").withIndex(6).ofType(Types.VARCHAR).withSize(1024).notNull());
        addMetadata(interfaceName, ColumnMetadata.named("INTERFACE_NAME").withIndex(3).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(interfaceUrl, ColumnMetadata.named("INTERFACE_URL").withIndex(5).ofType(Types.VARCHAR).withSize(64).notNull());
        addMetadata(outParamFormat, ColumnMetadata.named("OUT_PARAM_FORMAT").withIndex(7).ofType(Types.VARCHAR).withSize(1024).notNull());
        addMetadata(paramOutStatus, ColumnMetadata.named("PARAM_OUT_STATUS").withIndex(8).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(paramOutStatusSuccess, ColumnMetadata.named("PARAM_OUT_STATUS_SUCCESS").withIndex(9).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(typeId, ColumnMetadata.named("TYPE_ID").withIndex(4).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(updatedBy, ColumnMetadata.named("UPDATED_BY").withIndex(12).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(updatedTime, ColumnMetadata.named("UPDATED_TIME").withIndex(13).ofType(Types.TIMESTAMP).withSize(19).notNull());
        
        addMetadata(inParamSchema, ColumnMetadata.named("IN_PARAM_SCHEMA").withIndex(14).ofType(Types.LONGVARCHAR));
        addMetadata(inParamFormatType, ColumnMetadata.named("IN_PARAM_FORMAT_TYPE").withIndex(15).ofType(Types.VARCHAR).withSize(1).notNull());
        addMetadata(outParamSchema, ColumnMetadata.named("OUT_PARAM_SCHEMA").withIndex(16).ofType(Types.LONGVARCHAR));
        addMetadata(outParamFormatType, ColumnMetadata.named("OUT_PARAM_FORMAT_TYPE").withIndex(17).ofType(Types.VARCHAR).withSize(1).notNull());
        addMetadata(allowLogDiscard, ColumnMetadata.named("ALLOW_LOG_DISCARD").withIndex(18).ofType(Types.VARCHAR).withSize(1).notNull());
        addMetadata(interfaceType, ColumnMetadata.named("INTERFACE_TYPE").withIndex(19).ofType(Types.INTEGER).withSize(1).notNull());
        addMetadata(asyncFlag, ColumnMetadata.named("ASYNC_FLAG").withIndex(19).ofType(Types.INTEGER).withSize(1).notNull());

        addMetadata(encryptionType, ColumnMetadata.named("ENCRYPTION_TYPE").withIndex(20).ofType(Types.INTEGER).withSize(1).notNull());
        addMetadata(maskPosStart, ColumnMetadata.named("MASK_POS_START").withIndex(21).ofType(Types.INTEGER).withSize(4).notNull());
        addMetadata(maskPosEnd, ColumnMetadata.named("MASK_POS_END").withIndex(22).ofType(Types.INTEGER).withSize(4).notNull());

        addMetadata(originInterfaceId, ColumnMetadata.named("ORIGIN_INTERFACE_ID").withIndex(23).ofType(Types.VARCHAR).withSize(32).notNull());
    }

}

