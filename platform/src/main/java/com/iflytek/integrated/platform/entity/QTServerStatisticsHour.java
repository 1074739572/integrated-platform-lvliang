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
public class QTServerStatisticsHour extends com.querydsl.sql.RelationalPathBase<TServerStatisticsHour> {

    private static final long serialVersionUID = -200650066;

    public static final QTServerStatisticsHour qtServerStatisticsHour = new QTServerStatisticsHour("t_server_statistics_hour");

    public final DateTimePath<java.util.Date> updateTime = createDateTime("updateTime", java.util.Date.class);

    public final DateTimePath<java.util.Date> dt = createDateTime("dt", java.util.Date.class);

    public final StringPath id = createString("id");

    public final NumberPath<Long> serverRequestTotal = createNumber("serverRequestTotal",Long.class);


    public final com.querydsl.sql.PrimaryKey<TServerStatisticsHour> primary = createPrimaryKey(id);

    public QTServerStatisticsHour(String variable) {
        super(TServerStatisticsHour.class, forVariable(variable), "null", "t_server_statistics_hour");
        addMetadata();
    }

    public QTServerStatisticsHour(String variable, String schema, String table) {
        super(TServerStatisticsHour.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTServerStatisticsHour(String variable, String schema) {
        super(TServerStatisticsHour.class, forVariable(variable), schema, "t_server_statistics_hour");
        addMetadata();
    }

    public QTServerStatisticsHour(Path<? extends TServerStatisticsHour> path) {
        super(path.getType(), path.getMetadata(), "null", "t_server_statistics_hour");
        addMetadata();
    }

    public QTServerStatisticsHour(PathMetadata metadata) {
        super(TServerStatisticsHour.class, metadata, "null", "t_server_statistics_hour");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(dt, ColumnMetadata.named("DT").withIndex(10).ofType(Types.DATE).withSize(19));
        addMetadata(updateTime, ColumnMetadata.named("UPDATE_TIME").withIndex(11).ofType(Types.TIMESTAMP).withSize(19).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(serverRequestTotal, ColumnMetadata.named("SERVER_REQUEST_TOTAL").withIndex(5).ofType(Types.BIGINT));
    }

}

