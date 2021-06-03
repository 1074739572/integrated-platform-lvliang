package com.iflytek.integrated.platform.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QTSysDriveLink is a Querydsl query type for TSysDriveLink
 */
public class QTSysDriveLink extends com.querydsl.sql.RelationalPathBase<TSysDriveLink> {

    private static final long serialVersionUID = -890822584;

    public static final QTSysDriveLink qTSysDriveLink = new QTSysDriveLink("t_sys_drive_link");

    public final StringPath createdBy = createString("createdBy");

    public final DateTimePath<java.util.Date> createdTime = createDateTime("createdTime", java.util.Date.class);

    public final StringPath driveId = createString("driveId");

    public final NumberPath<Integer> driveOrder = createNumber("driveOrder", Integer.class);

    public final StringPath id = createString("id");

    public final StringPath updatedBy = createString("updatedBy");

    public final DateTimePath<java.util.Date> updatedTime = createDateTime("updatedTime", java.util.Date.class);

    public final StringPath sysId = createString("sysId");

    public final com.querydsl.sql.PrimaryKey<TSysDriveLink> primary = createPrimaryKey(id);

    public QTSysDriveLink(String variable) {
        super(TSysDriveLink.class, forVariable(variable), "null", "t_sys_drive_link");
        addMetadata();
    }

    public QTSysDriveLink(String variable, String schema, String table) {
        super(TSysDriveLink.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTSysDriveLink(String variable, String schema) {
        super(TSysDriveLink.class, forVariable(variable), schema, "t_sys_drive_link");
        addMetadata();
    }

    public QTSysDriveLink(Path<? extends TSysDriveLink> path) {
        super(path.getType(), path.getMetadata(), "null", "t_sys_drive_link");
        addMetadata();
    }

    public QTSysDriveLink(PathMetadata metadata) {
        super(TSysDriveLink.class, metadata, "null", "t_sys_drive_link");
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
        addMetadata(sysId, ColumnMetadata.named("SYS_ID").withIndex(2).ofType(Types.VARCHAR).withSize(32).notNull());
    }

}

