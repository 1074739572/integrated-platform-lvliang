package com.iflytek.integrated.platform.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QTVendorConfig is a Querydsl query type for TVendorConfig
 */
public class QTVendorConfig extends com.querydsl.sql.RelationalPathBase<TVendorConfig> {

    private static final long serialVersionUID = -23654882;

    public static final QTVendorConfig qTVendorConfig = new QTVendorConfig("t_vendor_config");

    public final StringPath addressUrl = createString("addressUrl");

    public final StringPath connectionType = createString("connectionType");

    public final StringPath createdBy = createString("createdBy");

    public final DateTimePath<java.util.Date> createdTime = createDateTime("createdTime", java.util.Date.class);

    public final StringPath databaseDriver = createString("databaseDriver");

    public final StringPath databaseName = createString("databaseName");

    public final StringPath databaseUrl = createString("databaseUrl");

    public final StringPath endpointUrl = createString("endpointUrl");

    public final StringPath id = createString("id");

    public final StringPath jsonParams = createString("jsonParams");

    public final StringPath namespaceUrl = createString("namespaceUrl");

    public final StringPath platformId = createString("platformId");

    public final StringPath updatedBy = createString("updatedBy");

    public final DateTimePath<java.util.Date> updatedTime = createDateTime("updatedTime", java.util.Date.class);

    public final StringPath userName = createString("userName");

    public final StringPath userPassword = createString("userPassword");

    public final StringPath vendorId = createString("vendorId");

    public final StringPath versionId = createString("versionId");

    public QTVendorConfig(String variable) {
        super(TVendorConfig.class, forVariable(variable), "null", "t_vendor_config");
        addMetadata();
    }

    public QTVendorConfig(String variable, String schema, String table) {
        super(TVendorConfig.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTVendorConfig(String variable, String schema) {
        super(TVendorConfig.class, forVariable(variable), schema, "t_vendor_config");
        addMetadata();
    }

    public QTVendorConfig(Path<? extends TVendorConfig> path) {
        super(path.getType(), path.getMetadata(), "null", "t_vendor_config");
        addMetadata();
    }

    public QTVendorConfig(PathMetadata metadata) {
        super(TVendorConfig.class, metadata, "null", "t_vendor_config");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(addressUrl, ColumnMetadata.named("ADDRESS_URL").withIndex(6).ofType(Types.VARCHAR).withSize(128));
        addMetadata(connectionType, ColumnMetadata.named("CONNECTION_TYPE").withIndex(5).ofType(Types.VARCHAR).withSize(32));
        addMetadata(createdBy, ColumnMetadata.named("CREATED_BY").withIndex(15).ofType(Types.VARCHAR).withSize(32));
        addMetadata(createdTime, ColumnMetadata.named("CREATED_TIME").withIndex(16).ofType(Types.TIMESTAMP).withSize(19));
        addMetadata(databaseDriver, ColumnMetadata.named("DATABASE_DRIVER").withIndex(11).ofType(Types.VARCHAR).withSize(128));
        addMetadata(databaseName, ColumnMetadata.named("DATABASE_NAME").withIndex(9).ofType(Types.VARCHAR).withSize(128));
        addMetadata(databaseUrl, ColumnMetadata.named("DATABASE_URL").withIndex(10).ofType(Types.VARCHAR).withSize(128));
        addMetadata(endpointUrl, ColumnMetadata.named("ENDPOINT_URL").withIndex(7).ofType(Types.VARCHAR).withSize(128));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.VARCHAR).withSize(32));
        addMetadata(jsonParams, ColumnMetadata.named("JSON_PARAMS").withIndex(12).ofType(Types.LONGVARCHAR).withSize(65535));
        addMetadata(namespaceUrl, ColumnMetadata.named("NAMESPACE_URL").withIndex(8).ofType(Types.VARCHAR).withSize(128));
        addMetadata(platformId, ColumnMetadata.named("PLATFORM_ID").withIndex(2).ofType(Types.VARCHAR).withSize(32));
        addMetadata(updatedBy, ColumnMetadata.named("UPDATED_BY").withIndex(17).ofType(Types.VARCHAR).withSize(32));
        addMetadata(updatedTime, ColumnMetadata.named("UPDATED_TIME").withIndex(18).ofType(Types.TIMESTAMP).withSize(19));
        addMetadata(userName, ColumnMetadata.named("USER_NAME").withIndex(13).ofType(Types.VARCHAR).withSize(32));
        addMetadata(userPassword, ColumnMetadata.named("USER_PASSWORD").withIndex(14).ofType(Types.VARCHAR).withSize(128));
        addMetadata(vendorId, ColumnMetadata.named("VENDOR_ID").withIndex(3).ofType(Types.VARCHAR).withSize(32));
        addMetadata(versionId, ColumnMetadata.named("VERSION_ID").withIndex(4).ofType(Types.VARCHAR).withSize(32));
    }

}

