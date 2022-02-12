package com.iflytek.integrated.platform.entity;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.RelationalPathBase;

import java.sql.Types;
import java.util.Date;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

public class QTHistory extends RelationalPathBase<THistory> {

    private static final long serialVersionUID = -4435159848551102751L;

    public static final QTHistory qtHistory = new QTHistory("t_history");

    public final StringPath pkId = createString("pkId");

    public final NumberPath<Integer> hisType = createNumber("hisType",Integer.class);

    public final StringPath hisShow = createString("hisShow");

    public final StringPath hisContent = createString("hisContent");

    public final StringPath createdBy = createString("createdBy");

    public final DateTimePath<Date> createdTime = createDateTime("createdTime", java.util.Date.class);

    public final StringPath originId = createString("originId");

    public final StringPath recordId = createString("recordId");

    public final com.querydsl.sql.PrimaryKey<THistory> primary = createPrimaryKey(pkId);

    public QTHistory(String variable) {
        super(THistory.class, forVariable(variable), "null", "t_history");
        addMetadata();
    }

    public QTHistory(String variable, String schema, String table) {
        super(THistory.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTHistory(String variable, String schema) {
        super(THistory.class, forVariable(variable), schema, "t_history");
        addMetadata();
    }

    public QTHistory(Path<? extends THistory> path) {
        super(path.getType(), path.getMetadata(), "null", "t_history");
        addMetadata();
    }

    public QTHistory(PathMetadata metadata) {
        super(THistory.class, metadata, "null", "t_history");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(pkId, ColumnMetadata.named("PK_ID").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(hisType, ColumnMetadata.named("HIS_TYPE").withIndex(2).ofType(Types.INTEGER).withSize(1));
        addMetadata(hisShow, ColumnMetadata.named("HIS_SHOW").withIndex(3).ofType(Types.LONGVARCHAR).withSize(65535));
        addMetadata(hisContent, ColumnMetadata.named("HIS_CONTENT").withIndex(4).ofType(Types.LONGVARCHAR).withSize(65535));
        addMetadata(createdBy, ColumnMetadata.named("CREATED_BY").withIndex(5).ofType(Types.VARCHAR).withSize(32));
        addMetadata(createdTime, ColumnMetadata.named("CREATED_TIME").withIndex(6).ofType(Types.TIMESTAMP).withSize(19));
        addMetadata(originId, ColumnMetadata.named("ORIGIN_ID").withIndex(7).ofType(Types.VARCHAR).withSize(500));
        addMetadata(recordId, ColumnMetadata.named("RECORD_ID").withIndex(8).ofType(Types.VARCHAR).withSize(500));
    }

}
