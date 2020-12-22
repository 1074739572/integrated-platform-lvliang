package com.iflytek.integrated.platform.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QTVendorDriveLink is a Querydsl query type for TVendorDriveLink
 */
public class QTVendorDriveLink extends com.querydsl.sql.RelationalPathBase<TVendorDriveLink> {

    private static final long serialVersionUID = -890822584;

    public static final QTVendorDriveLink qTVendorDriveLink = new QTVendorDriveLink("t_vendor_drive_link");

    public final StringPath createdBy = createString("createdBy");

    public final DateTimePath<java.util.Date> createdTime = createDateTime("createdTime", java.util.Date.class);

    public final StringPath driveId = createString("driveId");

    public final NumberPath<Integer> driveOrder = createNumber("driveOrder", Integer.class);

    public final StringPath id = createString("id");

    public final StringPath updatedBy = createString("updatedBy");

    public final DateTimePath<java.util.Date> updatedTime = createDateTime("updatedTime", java.util.Date.class);

    public final StringPath vendorId = createString("vendorId");

    public final com.querydsl.sql.PrimaryKey<TVendorDriveLink> primary = createPrimaryKey(id);

    public QTVendorDriveLink(String variable) {
        super(TVendorDriveLink.class, forVariable(variable), "null", "t_vendor_drive_link");
        addMetadata();
    }

    public QTVendorDriveLink(String variable, String schema, String table) {
        super(TVendorDriveLink.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTVendorDriveLink(String variable, String schema) {
        super(TVendorDriveLink.class, forVariable(variable), schema, "t_vendor_drive_link");
        addMetadata();
    }

    public QTVendorDriveLink(Path<? extends TVendorDriveLink> path) {
        super(path.getType(), path.getMetadata(), "null", "t_vendor_drive_link");
        addMetadata();
    }

    public QTVendorDriveLink(PathMetadata metadata) {
        super(TVendorDriveLink.class, metadata, "null", "t_vendor_drive_link");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(createdBy, ColumnMetadata.named("CREATED_BY").withIndex(5).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(createdTime, ColumnMetadata.named("CREATED_TIME").withIndex(6).ofType(Types.TIMESTAMP).withSize(19).notNull());
        addMetadata(driveId, ColumnMetadata.named("DRIVE_ID").withIndex(3).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(driveOrder, ColumnMetadata.named("DRIVE_ORDER").withIndex(4).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(updatedBy, ColumnMetadata.named("UPDATED_BY").withIndex(7).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(updatedTime, ColumnMetadata.named("UPDATED_TIME").withIndex(8).ofType(Types.TIMESTAMP).withSize(19).notNull());
        addMetadata(vendorId, ColumnMetadata.named("VENDOR_ID").withIndex(2).ofType(Types.VARCHAR).withSize(32).notNull());
    }

}

