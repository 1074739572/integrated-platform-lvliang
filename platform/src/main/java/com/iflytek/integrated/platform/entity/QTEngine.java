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
 * QTArea is a Querydsl query type for TArea
 */
public class QTEngine extends com.querydsl.sql.RelationalPathBase<TEngine> {

    private static final long serialVersionUID = -201223167;

    public static final QTEngine qtEngine = new QTEngine("t_engine");

    public final StringPath engineName = createString("engineName");

    public final StringPath engineCode = createString("engineCode");

    public final StringPath engineUrl = createString("engineUrl");

    public final StringPath engineUser = createString("engineUser");

    public final StringPath enginePwd = createString("enginePwd");

    public final StringPath createdBy = createString("createdBy");

    public final DateTimePath<java.util.Date> createdTime = createDateTime("createdTime", java.util.Date.class);

    public final StringPath id = createString("id");

    public final StringPath updatedBy = createString("updatedBy");

    public final DateTimePath<java.util.Date> updatedTime = createDateTime("updatedTime", java.util.Date.class);

    public final com.querydsl.sql.PrimaryKey<TEngine> primary = createPrimaryKey(id);

    public QTEngine(String variable) {
        super(TEngine.class, forVariable(variable), "null", "t_engine");
        addMetadata();
    }

    public QTEngine(String variable, String schema, String table) {
        super(TEngine.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTEngine(String variable, String schema) {
        super(TEngine.class, forVariable(variable), schema, "t_engine");
        addMetadata();
    }

    public QTEngine(Path<? extends TEngine> path) {
        super(path.getType(), path.getMetadata(), "null", "t_engine");
        addMetadata();
    }

    public QTEngine(PathMetadata metadata) {
        super(TEngine.class, metadata, "null", "t_engine");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(engineName, ColumnMetadata.named("ENGINE_NAME").withIndex(3).ofType(Types.VARCHAR).withSize(32));
        addMetadata(engineCode, ColumnMetadata.named("ENGINE_CODE").withIndex(11).ofType(Types.VARCHAR).withSize(32));
        addMetadata(engineUrl, ColumnMetadata.named("ENGINE_URL").withIndex(4).ofType(Types.VARCHAR).withSize(255));
        addMetadata(engineUser, ColumnMetadata.named("ENGINE_USER").withIndex(5).ofType(Types.VARCHAR).withSize(255));
        addMetadata(enginePwd, ColumnMetadata.named("ENGINE_PWD").withIndex(6).ofType(Types.VARCHAR).withSize(255));
        addMetadata(createdBy, ColumnMetadata.named("CREATED_BY").withIndex(7).ofType(Types.VARCHAR).withSize(32));
        addMetadata(createdTime, ColumnMetadata.named("CREATED_TIME").withIndex(8).ofType(Types.TIMESTAMP).withSize(19));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(updatedBy, ColumnMetadata.named("UPDATED_BY").withIndex(9).ofType(Types.VARCHAR).withSize(32));
        addMetadata(updatedTime, ColumnMetadata.named("UPDATED_TIME").withIndex(10).ofType(Types.TIMESTAMP).withSize(19));
    }

}

