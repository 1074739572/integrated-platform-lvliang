package com.iflytek.integrated.platform.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QTSys is a Querydsl query type for TProduct
 */
public class QTSys extends com.querydsl.sql.RelationalPathBase<TSys> {

    private static final long serialVersionUID = 1572070651;

    public static final QTSys qTSys = new QTSys("t_sys");

    public final StringPath createdBy = createString("createdBy");

    public final DateTimePath<java.util.Date> createdTime = createDateTime("createdTime", java.util.Date.class);

    public final StringPath id = createString("id");

    public final StringPath isValid = createString("isValid");

    public final StringPath sysCode = createString("sysCode");

    public final StringPath sysName = createString("sysName");

    public final StringPath updatedBy = createString("updatedBy");

    public final DateTimePath<java.util.Date> updatedTime = createDateTime("updatedTime", java.util.Date.class);

    public final StringPath vendorId = createString("vendorId");

    public final StringPath sysDesc = createString("sysDesc");

    public final com.querydsl.sql.PrimaryKey<TSys> primary = createPrimaryKey(id);

    public QTSys(String variable) {
        super(TSys.class, forVariable(variable), "null", "t_sys");
        addMetadata();
    }

    public QTSys(String variable, String schema, String table) {
        super(TSys.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTSys(String variable, String schema) {
        super(TSys.class, forVariable(variable), schema, "t_sys");
        addMetadata();
    }

    public QTSys(Path<? extends TSys> path) {
        super(path.getType(), path.getMetadata(), "null", "t_sys");
        addMetadata();
    }

    public QTSys(PathMetadata metadata) {
        super(TSys.class, metadata, "null", "t_sys");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(sysName, ColumnMetadata.named("SYS_NAME").withIndex(2).ofType(Types.VARCHAR).withSize(32));
        addMetadata(sysCode, ColumnMetadata.named("SYS_CODE").withIndex(3).ofType(Types.VARCHAR).withSize(32));
        addMetadata(isValid, ColumnMetadata.named("IS_VALID").withIndex(4).ofType(Types.VARCHAR).withSize(1));
        addMetadata(createdBy, ColumnMetadata.named("CREATED_BY").withIndex(5).ofType(Types.VARCHAR).withSize(32));
        addMetadata(createdTime, ColumnMetadata.named("CREATED_TIME").withIndex(6).ofType(Types.TIMESTAMP).withSize(19));
        addMetadata(updatedBy, ColumnMetadata.named("UPDATED_BY").withIndex(7).ofType(Types.VARCHAR).withSize(32));
        addMetadata(updatedTime, ColumnMetadata.named("UPDATED_TIME").withIndex(8).ofType(Types.TIMESTAMP).withSize(19));
        addMetadata(vendorId, ColumnMetadata.named("vendor_id").withIndex(9).ofType(Types.VARCHAR).withSize(32));
        addMetadata(sysDesc, ColumnMetadata.named("sys_desc").withIndex(10).ofType(Types.VARCHAR).withSize(500));
    }

}

