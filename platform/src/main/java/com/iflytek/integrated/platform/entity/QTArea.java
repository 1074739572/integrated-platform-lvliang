package com.iflytek.integrated.platform.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QTArea is a Querydsl query type for TArea
 */
public class QTArea extends com.querydsl.sql.RelationalPathBase<TArea> {

    private static final long serialVersionUID = -201223167;

    public static final QTArea qTArea = new QTArea("t_area");

    public final StringPath areaCode = createString("areaCode");

    public final NumberPath<Integer> areaLevel = createNumber("areaLevel", Integer.class);

    public final StringPath areaName = createString("areaName");

    public final StringPath createdBy = createString("createdBy");

    public final DateTimePath<java.util.Date> createdTime = createDateTime("createdTime", java.util.Date.class);

    public final StringPath id = createString("id");

    public final StringPath superId = createString("superId");

    public final StringPath updatedBy = createString("updatedBy");

    public final DateTimePath<java.util.Date> updatedTime = createDateTime("updatedTime", java.util.Date.class);

    public final com.querydsl.sql.PrimaryKey<TArea> primary = createPrimaryKey(id);

    public QTArea(String variable) {
        super(TArea.class, forVariable(variable), "null", "t_area");
        addMetadata();
    }

    public QTArea(String variable, String schema, String table) {
        super(TArea.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTArea(String variable, String schema) {
        super(TArea.class, forVariable(variable), schema, "t_area");
        addMetadata();
    }

    public QTArea(Path<? extends TArea> path) {
        super(path.getType(), path.getMetadata(), "null", "t_area");
        addMetadata();
    }

    public QTArea(PathMetadata metadata) {
        super(TArea.class, metadata, "null", "t_area");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(areaCode, ColumnMetadata.named("AREA_CODE").withIndex(3).ofType(Types.VARCHAR).withSize(32));
        addMetadata(areaLevel, ColumnMetadata.named("AREA_LEVEL").withIndex(5).ofType(Types.INTEGER).withSize(10));
        addMetadata(areaName, ColumnMetadata.named("AREA_NAME").withIndex(4).ofType(Types.VARCHAR).withSize(64));
        addMetadata(createdBy, ColumnMetadata.named("CREATED_BY").withIndex(6).ofType(Types.VARCHAR).withSize(32));
        addMetadata(createdTime, ColumnMetadata.named("CREATED_TIME").withIndex(7).ofType(Types.TIMESTAMP).withSize(19));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(superId, ColumnMetadata.named("SUPER_ID").withIndex(2).ofType(Types.VARCHAR).withSize(32));
        addMetadata(updatedBy, ColumnMetadata.named("UPDATED_BY").withIndex(8).ofType(Types.VARCHAR).withSize(32));
        addMetadata(updatedTime, ColumnMetadata.named("UPDATED_TIME").withIndex(9).ofType(Types.TIMESTAMP).withSize(19));
    }

}

