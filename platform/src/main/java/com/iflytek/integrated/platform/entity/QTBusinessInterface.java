package com.iflytek.integrated.platform.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QTBusinessInterface is a Querydsl query type for TBusinessInterface
 */
public class QTBusinessInterface extends com.querydsl.sql.RelationalPathBase<TBusinessInterface> {

    private static final long serialVersionUID = 417267301;

    public static final QTBusinessInterface qTBusinessInterface = new QTBusinessInterface("t_business_interface");

    public final StringPath afterInterface = createString("afterInterface");

    public final StringPath businessInterfaceName = createString("businessInterfaceName");

    public final StringPath createdBy = createString("createdBy");

    public final DateTimePath<java.util.Date> createdTime = createDateTime("createdTime", java.util.Date.class);

    public final NumberPath<Integer> excErrOrder = createNumber("excErrOrder", Integer.class);

    public final StringPath excErrStatus = createString("excErrStatus");

    public final StringPath frontInterface = createString("frontInterface");

    public final StringPath id = createString("id");

    public final StringPath inParamFormat = createString("inParamFormat");

    public final StringPath inParamFormatType = createString("inParamFormatType");

    public final StringPath inParamSchema = createString("inParamSchema");

    public final StringPath inParamTemplate = createString("inParamTemplate");

    public final StringPath interfaceId = createString("interfaceId");

    public final StringPath interfaceType = createString("interfaceType");

    public final StringPath mockStatus = createString("mockStatus");

    public final StringPath mockTemplate = createString("mockTemplate");

    public final StringPath outParamFormat = createString("outParamFormat");

    public final StringPath outParamFormatType = createString("outParamFormatType");

    public final StringPath outParamSchema = createString("outParamSchema");

    public final StringPath outParamTemplate = createString("outParamTemplate");

    public final StringPath pluginId = createString("pluginId");

    public final StringPath productFunctionLinkId = createString("productFunctionLinkId");

    public final StringPath requestConstant = createString("requestConstant");

    public final StringPath requestType = createString("requestType");

    public final StringPath status = createString("status");

    public final StringPath updatedBy = createString("updatedBy");

    public final DateTimePath<java.util.Date> updatedTime = createDateTime("updatedTime", java.util.Date.class);

    public final StringPath vendorConfigId = createString("vendorConfigId");

    public final com.querydsl.sql.PrimaryKey<TBusinessInterface> primary = createPrimaryKey(id);

    public QTBusinessInterface(String variable) {
        super(TBusinessInterface.class, forVariable(variable), "null", "t_business_interface");
        addMetadata();
    }

    public QTBusinessInterface(String variable, String schema, String table) {
        super(TBusinessInterface.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTBusinessInterface(String variable, String schema) {
        super(TBusinessInterface.class, forVariable(variable), schema, "t_business_interface");
        addMetadata();
    }

    public QTBusinessInterface(Path<? extends TBusinessInterface> path) {
        super(path.getType(), path.getMetadata(), "null", "t_business_interface");
        addMetadata();
    }

    public QTBusinessInterface(PathMetadata metadata) {
        super(TBusinessInterface.class, metadata, "null", "t_business_interface");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(afterInterface, ColumnMetadata.named("AFTER_INTERFACE").withIndex(11).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(businessInterfaceName, ColumnMetadata.named("BUSINESS_INTERFACE_NAME").withIndex(5).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(createdBy, ColumnMetadata.named("CREATED_BY").withIndex(25).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(createdTime, ColumnMetadata.named("CREATED_TIME").withIndex(26).ofType(Types.TIMESTAMP).withSize(19).notNull());
        addMetadata(excErrOrder, ColumnMetadata.named("EXC_ERR_ORDER").withIndex(24).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(excErrStatus, ColumnMetadata.named("EXC_ERR_STATUS").withIndex(23).ofType(Types.VARCHAR).withSize(1).notNull());
        addMetadata(frontInterface, ColumnMetadata.named("FRONT_INTERFACE").withIndex(10).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(inParamFormat, ColumnMetadata.named("IN_PARAM_FORMAT").withIndex(12).ofType(Types.LONGVARCHAR).withSize(65535));
        addMetadata(inParamFormatType, ColumnMetadata.named("IN_PARAM_FORMAT_TYPE").withIndex(15).ofType(Types.VARCHAR).withSize(1).notNull());
        addMetadata(inParamSchema, ColumnMetadata.named("IN_PARAM_SCHEMA").withIndex(13).ofType(Types.LONGVARCHAR).withSize(65535));
        addMetadata(inParamTemplate, ColumnMetadata.named("IN_PARAM_TEMPLATE").withIndex(14).ofType(Types.LONGVARCHAR).withSize(65535));
        addMetadata(interfaceId, ColumnMetadata.named("INTERFACE_ID").withIndex(3).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(interfaceType, ColumnMetadata.named("INTERFACE_TYPE").withIndex(8).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(mockStatus, ColumnMetadata.named("MOCK_STATUS").withIndex(21).ofType(Types.VARCHAR).withSize(1).notNull());
        addMetadata(mockTemplate, ColumnMetadata.named("MOCK_TEMPLATE").withIndex(20).ofType(Types.LONGVARCHAR).withSize(65535));
        addMetadata(outParamFormat, ColumnMetadata.named("OUT_PARAM_FORMAT").withIndex(16).ofType(Types.LONGVARCHAR).withSize(65535));
        addMetadata(outParamFormatType, ColumnMetadata.named("OUT_PARAM_FORMAT_TYPE").withIndex(19).ofType(Types.VARCHAR).withSize(1).notNull());
        addMetadata(outParamSchema, ColumnMetadata.named("OUT_PARAM_SCHEMA").withIndex(17).ofType(Types.LONGVARCHAR).withSize(65535));
        addMetadata(outParamTemplate, ColumnMetadata.named("OUT_PARAM_TEMPLATE").withIndex(18).ofType(Types.LONGVARCHAR).withSize(65535));
        addMetadata(pluginId, ColumnMetadata.named("PLUGIN_ID").withIndex(9).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(productFunctionLinkId, ColumnMetadata.named("PRODUCT_FUNCTION_LINK_ID").withIndex(2).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(requestConstant, ColumnMetadata.named("REQUEST_CONSTANT").withIndex(7).ofType(Types.VARCHAR).withSize(1024).notNull());
        addMetadata(requestType, ColumnMetadata.named("REQUEST_TYPE").withIndex(6).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(status, ColumnMetadata.named("STATUS").withIndex(22).ofType(Types.VARCHAR).withSize(1).notNull());
        addMetadata(updatedBy, ColumnMetadata.named("UPDATED_BY").withIndex(27).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(updatedTime, ColumnMetadata.named("UPDATED_TIME").withIndex(28).ofType(Types.TIMESTAMP).withSize(19).notNull());
        addMetadata(vendorConfigId, ColumnMetadata.named("VENDOR_CONFIG_ID").withIndex(4).ofType(Types.VARCHAR).withSize(32).notNull());
    }

}

