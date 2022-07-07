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
public class QTServerStatisticsDay extends com.querydsl.sql.RelationalPathBase<TServerStatisticsDay> {

    private static final long serialVersionUID = -200650066;

    public static final QTServerStatisticsDay qTServerStatisticsDay = new QTServerStatisticsDay("t_server_statistics_day");

    public final DateTimePath<java.util.Date> updateTime = createDateTime("updateTime", java.util.Date.class);

    public final DateTimePath<java.util.Date> dt = createDateTime("dt", java.util.Date.class);

    public final StringPath id = createString("id");

    public final StringPath serverId = createString("serverId");

    public final StringPath typeId = createString("typeId");

    public final StringPath vendorId = createString("vendorId");

    public final NumberPath<Long> currRequestTotal = createNumber("currRequestTotal",Long.class);

    public final NumberPath<Long> currRequestOkTotal = createNumber("currRequestOkTotal",Long.class);

    public final NumberPath<Long> currResponseTimeTotal = createNumber("currResponseTimeTotal",Long.class);


    public final com.querydsl.sql.PrimaryKey<TServerStatisticsDay> primary = createPrimaryKey(id);

    public QTServerStatisticsDay(String variable) {
        super(TServerStatisticsDay.class, forVariable(variable), "null", "t_server_statistics_day");
        addMetadata();
    }

    public QTServerStatisticsDay(String variable, String schema, String table) {
        super(TServerStatisticsDay.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTServerStatisticsDay(String variable, String schema) {
        super(TServerStatisticsDay.class, forVariable(variable), schema, "t_server_statistics_day");
        addMetadata();
    }

    public QTServerStatisticsDay(Path<? extends TServerStatisticsDay> path) {
        super(path.getType(), path.getMetadata(), "null", "t_server_statistics_day");
        addMetadata();
    }

    public QTServerStatisticsDay(PathMetadata metadata) {
        super(TServerStatisticsDay.class, metadata, "null", "t_server_statistics_day");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(dt, ColumnMetadata.named("DT").withIndex(10).ofType(Types.DATE).withSize(19));
        addMetadata(updateTime, ColumnMetadata.named("UPDATE_TIME").withIndex(11).ofType(Types.TIMESTAMP).withSize(19).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(serverId, ColumnMetadata.named("SERVER_ID").withIndex(2).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(typeId, ColumnMetadata.named("TYPE_ID").withIndex(3).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(vendorId, ColumnMetadata.named("VENDOR_ID").withIndex(4).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(currRequestTotal, ColumnMetadata.named("CURR_REQUEST_TOTAL").withIndex(5).ofType(Types.BIGINT));
        addMetadata(currRequestOkTotal, ColumnMetadata.named("CURR_REQUEST_OK_TOTAL").withIndex(6).ofType(Types.BIGINT));
        addMetadata(currResponseTimeTotal, ColumnMetadata.named("CURR_RESPONSE_TIME_TOTAL").withIndex(7).ofType(Types.BIGINT));
    }

}

