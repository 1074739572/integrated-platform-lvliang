package com.iflytek.integrated.platform.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QTHospitalVendorLink is a Querydsl query type for THospitalVendorLink
 */
public class QTHospitalVendorLink extends com.querydsl.sql.RelationalPathBase<THospitalVendorLink> {

    private static final long serialVersionUID = -1414436944;

    public static final QTHospitalVendorLink qTHospitalVendorLink = new QTHospitalVendorLink("t_hospital_vendor_link");

    public final StringPath createdBy = createString("createdBy");

    public final DateTimePath<java.util.Date> createdTime = createDateTime("createdTime", java.util.Date.class);

    public final StringPath hospitalId = createString("hospitalId");

    public final StringPath id = createString("id");

    public final StringPath updatedBy = createString("updatedBy");

    public final DateTimePath<java.util.Date> updatedTime = createDateTime("updatedTime", java.util.Date.class);

    public final StringPath vendorConfigId = createString("vendorConfigId");

    public final StringPath vendorHospitalId = createString("vendorHospitalId");

    public final com.querydsl.sql.PrimaryKey<THospitalVendorLink> primary = createPrimaryKey(id);

    public QTHospitalVendorLink(String variable) {
        super(THospitalVendorLink.class, forVariable(variable), "null", "t_hospital_vendor_link");
        addMetadata();
    }

    public QTHospitalVendorLink(String variable, String schema, String table) {
        super(THospitalVendorLink.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTHospitalVendorLink(String variable, String schema) {
        super(THospitalVendorLink.class, forVariable(variable), schema, "t_hospital_vendor_link");
        addMetadata();
    }

    public QTHospitalVendorLink(Path<? extends THospitalVendorLink> path) {
        super(path.getType(), path.getMetadata(), "null", "t_hospital_vendor_link");
        addMetadata();
    }

    public QTHospitalVendorLink(PathMetadata metadata) {
        super(THospitalVendorLink.class, metadata, "null", "t_hospital_vendor_link");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(createdBy, ColumnMetadata.named("CREATED_BY").withIndex(5).ofType(Types.VARCHAR).withSize(32));
        addMetadata(createdTime, ColumnMetadata.named("CREATED_TIME").withIndex(6).ofType(Types.TIMESTAMP).withSize(19));
        addMetadata(hospitalId, ColumnMetadata.named("HOSPITAL_ID").withIndex(3).ofType(Types.VARCHAR).withSize(32));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(updatedBy, ColumnMetadata.named("UPDATED_BY").withIndex(7).ofType(Types.VARCHAR).withSize(32));
        addMetadata(updatedTime, ColumnMetadata.named("UPDATED_TIME").withIndex(8).ofType(Types.TIMESTAMP).withSize(19));
        addMetadata(vendorConfigId, ColumnMetadata.named("VENDOR_CONFIG_ID").withIndex(2).ofType(Types.VARCHAR).withSize(32));
        addMetadata(vendorHospitalId, ColumnMetadata.named("VENDOR_HOSPITAL_ID").withIndex(4).ofType(Types.VARCHAR).withSize(32));
    }

}

