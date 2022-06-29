package com.iflytek.integrated.platform.entity;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;

import java.sql.Types;
import java.util.Date;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

public class QTVendor extends com.querydsl.sql.RelationalPathBase<TVendor> {

    private static final long serialVersionUID = -3257250143153089318L;

    public static final QTVendor qtVendor = new QTVendor("t_vendor");

    public final StringPath createdBy = createString("createdBy");

    public final DateTimePath<Date> createdTime = createDateTime("createdTime", java.util.Date.class);

    public final StringPath id = createString("id");

    public final StringPath isValid = createString("isValid");

    public final StringPath vendorCode = createString("vendorCode");

    public final StringPath vendorName = createString("vendorName");

    public final StringPath updatedBy = createString("updatedBy");

    public final StringPath logo = createString("logo");

    public final DateTimePath<java.util.Date> updatedTime = createDateTime("updatedTime", java.util.Date.class);

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
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(vendorName, ColumnMetadata.named("vendor_name").withIndex(2).ofType(Types.VARCHAR).withSize(32));
        addMetadata(vendorCode, ColumnMetadata.named("vendor_code").withIndex(3).ofType(Types.VARCHAR).withSize(32));
        addMetadata(isValid, ColumnMetadata.named("is_valid").withIndex(4).ofType(Types.VARCHAR).withSize(1));
        addMetadata(createdBy, ColumnMetadata.named("created_by").withIndex(5).ofType(Types.VARCHAR).withSize(32));
        addMetadata(createdTime, ColumnMetadata.named("created_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(19));
        addMetadata(updatedBy, ColumnMetadata.named("updated_by").withIndex(7).ofType(Types.VARCHAR).withSize(32));
        addMetadata(updatedTime, ColumnMetadata.named("updated_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(19));
        addMetadata(logo, ColumnMetadata.named("logo").withIndex(9).ofType(Types.LONGVARCHAR));
    }
}
