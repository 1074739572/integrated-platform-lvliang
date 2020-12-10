package com.iflytek.integrated.platform.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QTHospital is a Querydsl query type for THospital
 */
public class QTHospital extends com.querydsl.sql.RelationalPathBase<THospital> {

    private static final long serialVersionUID = -2105284690;

    public static final QTHospital qTHospital = new QTHospital("t_hospital");

    public final StringPath areaId = createString("areaId");

    public final StringPath createdBy = createString("createdBy");

    public final DateTimePath<java.util.Date> createdTime = createDateTime("createdTime", java.util.Date.class);

    public final StringPath hospitalCode = createString("hospitalCode");

    public final StringPath hospitalName = createString("hospitalName");

    public final StringPath id = createString("id");

    public final StringPath status = createString("status");

    public final StringPath updatedBy = createString("updatedBy");

    public final DateTimePath<java.util.Date> updatedTime = createDateTime("updatedTime", java.util.Date.class);

    public QTHospital(String variable) {
        super(THospital.class, forVariable(variable), "null", "t_hospital");
        addMetadata();
    }

    public QTHospital(String variable, String schema, String table) {
        super(THospital.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTHospital(String variable, String schema) {
        super(THospital.class, forVariable(variable), schema, "t_hospital");
        addMetadata();
    }

    public QTHospital(Path<? extends THospital> path) {
        super(path.getType(), path.getMetadata(), "null", "t_hospital");
        addMetadata();
    }

    public QTHospital(PathMetadata metadata) {
        super(THospital.class, metadata, "null", "t_hospital");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(areaId, ColumnMetadata.named("AREA_ID").withIndex(5).ofType(Types.VARCHAR).withSize(32));
        addMetadata(createdBy, ColumnMetadata.named("CREATED_BY").withIndex(6).ofType(Types.VARCHAR).withSize(32));
        addMetadata(createdTime, ColumnMetadata.named("CREATED_TIME").withIndex(7).ofType(Types.TIMESTAMP).withSize(19));
        addMetadata(hospitalCode, ColumnMetadata.named("HOSPITAL_CODE").withIndex(3).ofType(Types.VARCHAR).withSize(32));
        addMetadata(hospitalName, ColumnMetadata.named("HOSPITAL_NAME").withIndex(2).ofType(Types.VARCHAR).withSize(32));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.VARCHAR).withSize(32));
        addMetadata(status, ColumnMetadata.named("STATUS").withIndex(4).ofType(Types.VARCHAR).withSize(1));
        addMetadata(updatedBy, ColumnMetadata.named("UPDATED_BY").withIndex(8).ofType(Types.VARCHAR).withSize(32));
        addMetadata(updatedTime, ColumnMetadata.named("UPDATED_TIME").withIndex(9).ofType(Types.TIMESTAMP).withSize(19));
    }

}

