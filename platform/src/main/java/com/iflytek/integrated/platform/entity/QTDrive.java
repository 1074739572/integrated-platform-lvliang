package com.iflytek.integrated.platform.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QTDrive is a Querydsl query type for TDrive
 */
public class QTDrive extends com.querydsl.sql.RelationalPathBase<TDrive> {

    private static final long serialVersionUID = -1940175722;

    public static final QTDrive qTDrive = new QTDrive("t_drive");

    public final StringPath createdBy = createString("createdBy");

    public final DateTimePath<java.util.Date> createdTime = createDateTime("createdTime", java.util.Date.class);

    public final StringPath driveCode = createString("driveCode");

    public final StringPath driveContent = createString("driveContent");

    public final StringPath driveInstruction = createString("driveInstruction");

    public final StringPath driveName = createString("driveName");

    public final StringPath id = createString("id");

    public final StringPath typeId = createString("typeId");

    public final StringPath updatedBy = createString("updatedBy");

    public final DateTimePath<java.util.Date> updatedTime = createDateTime("updatedTime", java.util.Date.class);
    
    public final StringPath driveCallType = createString("driveCallType");

    public final com.querydsl.sql.PrimaryKey<TDrive> primary = createPrimaryKey(id);

    public QTDrive(String variable) {
        super(TDrive.class, forVariable(variable), "null", "t_drive");
        addMetadata();
    }

    public QTDrive(String variable, String schema, String table) {
        super(TDrive.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTDrive(String variable, String schema) {
        super(TDrive.class, forVariable(variable), schema, "t_drive");
        addMetadata();
    }

    public QTDrive(Path<? extends TDrive> path) {
        super(path.getType(), path.getMetadata(), "null", "t_drive");
        addMetadata();
    }

    public QTDrive(PathMetadata metadata) {
        super(TDrive.class, metadata, "null", "t_drive");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(createdBy, ColumnMetadata.named("CREATED_BY").withIndex(7).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(createdTime, ColumnMetadata.named("CREATED_TIME").withIndex(8).ofType(Types.TIMESTAMP).withSize(19).notNull());
        addMetadata(driveCode, ColumnMetadata.named("DRIVE_CODE").withIndex(3).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(driveContent, ColumnMetadata.named("DRIVE_CONTENT").withIndex(6).ofType(Types.LONGVARCHAR).withSize(65535));
        addMetadata(driveInstruction, ColumnMetadata.named("DRIVE_INSTRUCTION").withIndex(5).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(driveName, ColumnMetadata.named("DRIVE_NAME").withIndex(2).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(typeId, ColumnMetadata.named("TYPE_ID").withIndex(4).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(updatedBy, ColumnMetadata.named("UPDATED_BY").withIndex(9).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(updatedTime, ColumnMetadata.named("UPDATED_TIME").withIndex(10).ofType(Types.TIMESTAMP).withSize(19).notNull());
        addMetadata(driveCallType, ColumnMetadata.named("DRIVE_CALL_TYPE").withIndex(11).ofType(Types.VARCHAR).withSize(1).notNull());
    }

}

