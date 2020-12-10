package com.iflytek.integrated.platform.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QTVendor is a Querydsl query type for TVendor
 */
public class QTVendor extends com.querydsl.sql.RelationalPathBase<TVendor> {

    private static final long serialVersionUID = 487545788;

    public static final QTVendor qTVendor = new QTVendor("t_vendor");

    public final StringPath createdBy = createString("createdBy");

    public final DateTimePath<java.util.Date> createdTime = createDateTime("createdTime", java.util.Date.class);

    public final StringPath id = createString("id");

    public final StringPath updatedBy = createString("updatedBy");

    public final DateTimePath<java.util.Date> updatedTime = createDateTime("updatedTime", java.util.Date.class);

    public final StringPath vendorCode = createString("vendorCode");

    public final StringPath vendorName = createString("vendorName");

    public final com.querydsl.sql.PrimaryKey<TVendor> primary = createPrimaryKey(id);

    public QTVendor(String variable) {
        super(TVendor.class, forVariable(variable), "null", "t_vendor");
        addMetadata();
    }

    public QTVendor(String variable, String schema, String table) {
        super(TVendor.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTVendor(String variable, String schema) {
        super(TVendor.class, forVariable(variable), schema, "t_vendor");
        addMetadata();
    }

    public QTVendor(Path<? extends TVendor> path) {
        super(path.getType(), path.getMetadata(), "null", "t_vendor");
        addMetadata();
    }

    public QTVendor(PathMetadata metadata) {
        super(TVendor.class, metadata, "null", "t_vendor");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(createdBy, ColumnMetadata.named("CREATED_BY").withIndex(4).ofType(Types.VARCHAR).withSize(32));
        addMetadata(createdTime, ColumnMetadata.named("CREATED_TIME").withIndex(5).ofType(Types.TIMESTAMP).withSize(19));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(updatedBy, ColumnMetadata.named("UPDATED_BY").withIndex(6).ofType(Types.VARCHAR).withSize(32));
        addMetadata(updatedTime, ColumnMetadata.named("UPDATED_TIME").withIndex(7).ofType(Types.TIMESTAMP).withSize(19));
        addMetadata(vendorCode, ColumnMetadata.named("VENDOR_CODE").withIndex(2).ofType(Types.VARCHAR).withSize(32));
        addMetadata(vendorName, ColumnMetadata.named("VENDOR_NAME").withIndex(3).ofType(Types.VARCHAR).withSize(32));
    }

}

