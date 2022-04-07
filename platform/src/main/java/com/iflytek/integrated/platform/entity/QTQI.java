package com.iflytek.integrated.platform.entity;

import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.RelationalPathBase;

import java.sql.Types;
import java.util.Date;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

public class QTQI extends RelationalPathBase<TQI> {

    private static final long serialVersionUID = 4220878056249910189L;

    public static final QTQI qi = new QTQI("t_quality_inspection");

    public final StringPath QIId = createString("QIId");

    public final StringPath QIName = createString("QIName");

    public final StringPath QIScript = createString("QIScript");

    public final StringPath createdBy = createString("createdBy");

    public final DateTimePath<Date> createdTime = createDateTime("createdTime", java.util.Date.class);

    public final com.querydsl.sql.PrimaryKey<TQI> primary = createPrimaryKey(QIId);

    public QTQI(String variable) {
        super(TQI.class, forVariable(variable), "null", "t_quality_inspection");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(QIId, ColumnMetadata.named("QI_ID").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(QIName, ColumnMetadata.named("QI_NAME").withIndex(2).ofType(Types.VARCHAR).withSize(255));
        addMetadata(QIScript, ColumnMetadata.named("QI_SCRIPT").withIndex(3).ofType(Types.LONGVARCHAR).withSize(65535));
        addMetadata(createdTime, ColumnMetadata.named("CREATED_TIME").withIndex(4).ofType(Types.TIMESTAMP).withSize(19));
        addMetadata(createdBy, ColumnMetadata.named("CREATED_BY").withIndex(5).ofType(Types.VARCHAR).withSize(32));
    }
}