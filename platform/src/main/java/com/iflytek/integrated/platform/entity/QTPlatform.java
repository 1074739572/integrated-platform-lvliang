package com.iflytek.integrated.platform.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QTPlatform is a Querydsl query type for TPlatform
 */
public class QTPlatform extends com.querydsl.sql.RelationalPathBase<TPlatform> {

    private static final long serialVersionUID = 73028071;

    public static final QTPlatform qTPlatform = new QTPlatform("t_platform");

    public final StringPath createdBy = createString("createdBy");

    public final DateTimePath<java.util.Date> createdTime = createDateTime("createdTime", java.util.Date.class);

    public final StringPath id = createString("id");

    public final StringPath platformCode = createString("platformCode");

    public final StringPath platformName = createString("platformName");

    public final StringPath platformStatus = createString("platformStatus");

    public final StringPath platformType = createString("platformType");

    public final StringPath projectId = createString("projectId");

    public final StringPath updatedBy = createString("updatedBy");

    public final DateTimePath<java.util.Date> updatedTime = createDateTime("updatedTime", java.util.Date.class);

    public QTPlatform(String variable) {
        super(TPlatform.class, forVariable(variable), "null", "t_platform");
        addMetadata();
    }

    public QTPlatform(String variable, String schema, String table) {
        super(TPlatform.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTPlatform(String variable, String schema) {
        super(TPlatform.class, forVariable(variable), schema, "t_platform");
        addMetadata();
    }

    public QTPlatform(Path<? extends TPlatform> path) {
        super(path.getType(), path.getMetadata(), "null", "t_platform");
        addMetadata();
    }

    public QTPlatform(PathMetadata metadata) {
        super(TPlatform.class, metadata, "null", "t_platform");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(createdBy, ColumnMetadata.named("CREATED_BY").withIndex(7).ofType(Types.VARCHAR).withSize(32));
        addMetadata(createdTime, ColumnMetadata.named("CREATED_TIME").withIndex(8).ofType(Types.TIMESTAMP).withSize(19));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.VARCHAR).withSize(32));
        addMetadata(platformCode, ColumnMetadata.named("PLATFORM_CODE").withIndex(4).ofType(Types.VARCHAR).withSize(32));
        addMetadata(platformName, ColumnMetadata.named("PLATFORM_NAME").withIndex(3).ofType(Types.VARCHAR).withSize(32));
        addMetadata(platformStatus, ColumnMetadata.named("PLATFORM_STATUS").withIndex(5).ofType(Types.VARCHAR).withSize(1));
        addMetadata(platformType, ColumnMetadata.named("PLATFORM_TYPE").withIndex(6).ofType(Types.VARCHAR).withSize(1));
        addMetadata(projectId, ColumnMetadata.named("PROJECT_ID").withIndex(2).ofType(Types.VARCHAR).withSize(32));
        addMetadata(updatedBy, ColumnMetadata.named("UPDATED_BY").withIndex(9).ofType(Types.VARCHAR).withSize(32));
        addMetadata(updatedTime, ColumnMetadata.named("UPDATED_TIME").withIndex(10).ofType(Types.TIMESTAMP).withSize(19));
    }

}

