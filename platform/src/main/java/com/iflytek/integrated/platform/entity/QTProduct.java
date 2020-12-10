package com.iflytek.integrated.platform.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QTProduct is a Querydsl query type for TProduct
 */
public class QTProduct extends com.querydsl.sql.RelationalPathBase<TProduct> {

    private static final long serialVersionUID = 1572070651;

    public static final QTProduct qTProduct = new QTProduct("t_product");

    public final StringPath createdBy = createString("createdBy");

    public final DateTimePath<java.util.Date> createdTime = createDateTime("createdTime", java.util.Date.class);

    public final StringPath id = createString("id");

    public final StringPath isValid = createString("isValid");

    public final StringPath productCode = createString("productCode");

    public final StringPath productName = createString("productName");

    public final StringPath updatedBy = createString("updatedBy");

    public final DateTimePath<java.util.Date> updatedTime = createDateTime("updatedTime", java.util.Date.class);

    public final com.querydsl.sql.PrimaryKey<TProduct> primary = createPrimaryKey(id);

    public QTProduct(String variable) {
        super(TProduct.class, forVariable(variable), "null", "t_product");
        addMetadata();
    }

    public QTProduct(String variable, String schema, String table) {
        super(TProduct.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTProduct(String variable, String schema) {
        super(TProduct.class, forVariable(variable), schema, "t_product");
        addMetadata();
    }

    public QTProduct(Path<? extends TProduct> path) {
        super(path.getType(), path.getMetadata(), "null", "t_product");
        addMetadata();
    }

    public QTProduct(PathMetadata metadata) {
        super(TProduct.class, metadata, "null", "t_product");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(createdBy, ColumnMetadata.named("CREATED_BY").withIndex(5).ofType(Types.VARCHAR).withSize(32));
        addMetadata(createdTime, ColumnMetadata.named("CREATED_TIME").withIndex(6).ofType(Types.TIMESTAMP).withSize(19));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(isValid, ColumnMetadata.named("IS_VALID").withIndex(4).ofType(Types.VARCHAR).withSize(1));
        addMetadata(productCode, ColumnMetadata.named("PRODUCT_CODE").withIndex(3).ofType(Types.VARCHAR).withSize(32));
        addMetadata(productName, ColumnMetadata.named("PRODUCT_NAME").withIndex(2).ofType(Types.VARCHAR).withSize(32));
        addMetadata(updatedBy, ColumnMetadata.named("UPDATED_BY").withIndex(7).ofType(Types.VARCHAR).withSize(32));
        addMetadata(updatedTime, ColumnMetadata.named("UPDATED_TIME").withIndex(8).ofType(Types.TIMESTAMP).withSize(19));
    }

}

