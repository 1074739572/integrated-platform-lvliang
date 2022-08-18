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
 * QTType is a Querydsl query type for TType
 */
public class QTServerAlert extends com.querydsl.sql.RelationalPathBase<TServerAlert> {

    private static final long serialVersionUID = -200650066;

    public static final QTServerAlert qtServerAlert = new QTServerAlert("t_server_alert");

    public final StringPath createdBy = createString("createdBy");

    public final DateTimePath<java.util.Date> createdTime = createDateTime("createdTime", java.util.Date.class);

    public final StringPath id = createString("id");

    public final NumberPath<Integer> type = createNumber("type", Integer.class);

    public final StringPath url = createString("url");

    public final StringPath theme = createString("theme");

    public final NumberPath<Integer> level = createNumber("level", Integer.class);

    public final StringPath token = createString("token");

    public final StringPath updatedBy = createString("updatedBy");

    public final DateTimePath<java.util.Date> updatedTime = createDateTime("updatedTime", java.util.Date.class);

    public final com.querydsl.sql.PrimaryKey<TServerAlert> primary = createPrimaryKey(id);

    public QTServerAlert(String variable) {
        super(TServerAlert.class, forVariable(variable), "null", "t_server_alert");
        addMetadata();
    }

    public QTServerAlert(String variable, String schema, String table) {
        super(TServerAlert.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTServerAlert(String variable, String schema) {
        super(TServerAlert.class, forVariable(variable), schema, "t_server_alert");
        addMetadata();
    }

    public QTServerAlert(Path<? extends TServerAlert> path) {
        super(path.getType(), path.getMetadata(), "null", "t_server_alert");
        addMetadata();
    }

    public QTServerAlert(PathMetadata metadata) {
        super(TServerAlert.class, metadata, "null", "t_server_alert");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(createdBy, ColumnMetadata.named("CREATED_BY").withIndex(7).ofType(Types.VARCHAR).withSize(32));
        addMetadata(createdTime, ColumnMetadata.named("CREATED_TIME").withIndex(8).ofType(Types.TIMESTAMP).withSize(19).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(type, ColumnMetadata.named("TYPE").withIndex(2).ofType(Types.INTEGER).withSize(2).notNull());
        addMetadata(url, ColumnMetadata.named("URL").withIndex(3).ofType(Types.VARCHAR).withSize(100).notNull());
        addMetadata(theme, ColumnMetadata.named("THEME").withIndex(4).ofType(Types.VARCHAR).withSize(50).notNull());
        addMetadata(token, ColumnMetadata.named("TOKEN").withIndex(5).ofType(Types.VARCHAR).withSize(100).notNull());
        addMetadata(level, ColumnMetadata.named("LEVEL").withIndex(6).ofType(Types.INTEGER).withSize(2).notNull());
        addMetadata(updatedBy, ColumnMetadata.named("UPDATED_BY").withIndex(9).ofType(Types.VARCHAR).withSize(32));
        addMetadata(updatedTime, ColumnMetadata.named("UPDATED_TIME").withIndex(10).ofType(Types.TIMESTAMP).withSize(19));
    }

}

