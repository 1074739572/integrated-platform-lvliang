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
 * QTBusinessInterface is a Querydsl query type for TBusinessInterface
 */
public class QTBusinessInterface extends com.querydsl.sql.RelationalPathBase<TBusinessInterface> {

    private static final long serialVersionUID = 417267301;

    public static final QTBusinessInterface qTBusinessInterface = new QTBusinessInterface("t_business_interface");

    public final StringPath businessInterfaceName = createString("businessInterfaceName");

    public final StringPath createdBy = createString("createdBy");

    public final DateTimePath<java.util.Date> createdTime = createDateTime("createdTime", java.util.Date.class);

    public final NumberPath<Integer> excErrOrder = createNumber("excErrOrder", Integer.class);

    public final StringPath excErrStatus = createString("excErrStatus");

    public final StringPath id = createString("id");
    
    public final StringPath requestSysconfigId = createString("requestSysconfigId");
    
    public final StringPath requestInterfaceId = createString("requestInterfaceId");
    
    public final StringPath requestedSysconfigId = createString("requestedSysconfigId");
    
    public final StringPath inParamFormat = createString("inParamFormat");

    public final StringPath inParamFormatType = createString("inParamFormatType");

    public final StringPath inParamSchema = createString("inParamSchema");

    public final StringPath inParamTemplate = createString("inParamTemplate");
    
    public final StringPath inParamTemplateType = createString("inParamTemplateType");

    public final StringPath interfaceType = createString("interfaceType");

    public final NumberPath<Integer> mockIsUse = createNumber("mockIsUse", Integer.class);

    public final NumberPath<Integer> asyncFlag = createNumber("asyncFlag", Integer.class);

    public final NumberPath<Integer> interfaceSlowFlag = createNumber("interfaceSlowFlag", Integer.class);

    public final NumberPath<Integer> replayFlag = createNumber("replayFlag", Integer.class);

    public final StringPath mockStatus = createString("mockStatus");

    public final StringPath mockTemplate = createString("mockTemplate");

    public final StringPath outParamFormat = createString("outParamFormat");

    public final StringPath outParamFormatType = createString("outParamFormatType");

    public final StringPath outParamSchema = createString("outParamSchema");

    public final StringPath outParamTemplate = createString("outParamTemplate");
    
    public final StringPath outParamTemplateType = createString("outParamTemplateType");

    public final StringPath pluginId = createString("pluginId");

    public final StringPath requestConstant = createString("requestConstant");

    public final StringPath requestType = createString("requestType");

    public final StringPath status = createString("status");

    public final StringPath updatedBy = createString("updatedBy");

    public final DateTimePath<java.util.Date> updatedTime = createDateTime("updatedTime", java.util.Date.class);

    public final StringPath QIId = createString("QIId");

    public final NumberPath<Integer> QIFlag = createNumber("QIFlag",Integer.class);

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
    	addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(requestSysconfigId, ColumnMetadata.named("REQUEST_SYSCONFIG_ID").withIndex(2).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(requestInterfaceId, ColumnMetadata.named("REQUEST_INTERFACE_ID").withIndex(3).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(requestedSysconfigId, ColumnMetadata.named("REQUESTED_SYSCONFIG_ID").withIndex(4).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(businessInterfaceName, ColumnMetadata.named("BUSINESS_INTERFACE_NAME").withIndex(5).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(requestType, ColumnMetadata.named("REQUEST_TYPE").withIndex(6).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(requestConstant, ColumnMetadata.named("REQUEST_CONSTANT").withIndex(7).ofType(Types.VARCHAR).withSize(1024).notNull());
        addMetadata(interfaceType, ColumnMetadata.named("INTERFACE_TYPE").withIndex(8).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(pluginId, ColumnMetadata.named("PLUGIN_ID").withIndex(9).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(inParamFormat, ColumnMetadata.named("IN_PARAM_FORMAT").withIndex(10).ofType(Types.LONGVARCHAR).withSize(65535));
        addMetadata(inParamSchema, ColumnMetadata.named("IN_PARAM_SCHEMA").withIndex(11).ofType(Types.LONGVARCHAR).withSize(65535));
        addMetadata(inParamTemplateType, ColumnMetadata.named("IN_PARAM_TEMPLATE_TYPE").withIndex(12).ofType(Types.TINYINT).withSize(1));
        addMetadata(inParamTemplate, ColumnMetadata.named("IN_PARAM_TEMPLATE").withIndex(13).ofType(Types.LONGVARCHAR).withSize(65535));
        addMetadata(inParamFormatType, ColumnMetadata.named("IN_PARAM_FORMAT_TYPE").withIndex(14).ofType(Types.VARCHAR).withSize(1).notNull());
        addMetadata(outParamFormat, ColumnMetadata.named("OUT_PARAM_FORMAT").withIndex(15).ofType(Types.LONGVARCHAR).withSize(65535));
        addMetadata(outParamSchema, ColumnMetadata.named("OUT_PARAM_SCHEMA").withIndex(16).ofType(Types.LONGVARCHAR).withSize(65535));
        addMetadata(outParamTemplateType, ColumnMetadata.named("OUT_PARAM_TEMPLATE_TYPE").withIndex(17).ofType(Types.TINYINT).withSize(1));
        addMetadata(outParamTemplate, ColumnMetadata.named("OUT_PARAM_TEMPLATE").withIndex(18).ofType(Types.LONGVARCHAR).withSize(65535));
        addMetadata(outParamFormatType, ColumnMetadata.named("OUT_PARAM_FORMAT_TYPE").withIndex(19).ofType(Types.VARCHAR).withSize(1).notNull());
        addMetadata(mockTemplate, ColumnMetadata.named("MOCK_TEMPLATE").withIndex(20).ofType(Types.LONGVARCHAR).withSize(65535));
        addMetadata(mockStatus, ColumnMetadata.named("MOCK_STATUS").withIndex(21).ofType(Types.VARCHAR).withSize(1).notNull());
        addMetadata(status, ColumnMetadata.named("STATUS").withIndex(22).ofType(Types.VARCHAR).withSize(1).notNull());
        addMetadata(excErrStatus, ColumnMetadata.named("EXC_ERR_STATUS").withIndex(23).ofType(Types.VARCHAR).withSize(1).notNull());
        addMetadata(excErrOrder, ColumnMetadata.named("EXC_ERR_ORDER").withIndex(24).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(mockIsUse, ColumnMetadata.named("MOCK_IS_USE").withIndex(25).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdBy, ColumnMetadata.named("CREATED_BY").withIndex(26).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(createdTime, ColumnMetadata.named("CREATED_TIME").withIndex(27).ofType(Types.TIMESTAMP).withSize(19).notNull());
        addMetadata(updatedBy, ColumnMetadata.named("UPDATED_BY").withIndex(28).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(updatedTime, ColumnMetadata.named("UPDATED_TIME").withIndex(29).ofType(Types.TIMESTAMP).withSize(19).notNull());
        addMetadata(asyncFlag, ColumnMetadata.named("ASYNC_FLAG").withIndex(30).ofType(Types.TINYINT).withSize(1).notNull());
        addMetadata(interfaceSlowFlag, ColumnMetadata.named("INTERFACE_SLOW_FLAG").withIndex(31).ofType(Types.TINYINT).withSize(1).notNull());
        addMetadata(replayFlag, ColumnMetadata.named("REPLAY_FLAG").withIndex(32).ofType(Types.TINYINT).withSize(1).notNull());
        addMetadata(QIId, ColumnMetadata.named("QI_ID").withIndex(33).ofType(Types.VARCHAR).withSize(32));
        addMetadata(QIFlag, ColumnMetadata.named("QI_FLAG").withIndex(34).ofType(Types.TINYINT).withSize(1));
    }

}

