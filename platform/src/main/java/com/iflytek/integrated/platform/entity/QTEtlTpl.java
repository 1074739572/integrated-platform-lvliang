package com.iflytek.integrated.platform.entity;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

import java.sql.Types;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;

/**
 * QTEtlTpl is a Querydsl query type for TEtlTpl
 */
public class QTEtlTpl extends com.querydsl.sql.RelationalPathBase<TEtlTpl> {

	private static final long serialVersionUID = -201223167;

	public static final QTEtlTpl qTEtlTpl = new QTEtlTpl("t_etl_tpl");

	public final StringPath tplName = createString("tplName");

	public final NumberPath<Integer> tplType = createNumber("tplType", Integer.class);

	public final NumberPath<Integer> tplFunType = createNumber("tplFunType", Integer.class);

	public final StringPath tplContent = createString("tplContent");
	
	public final StringPath tplDesp = createString("tplDesp");

	public final StringPath createdBy = createString("createdBy");

	public final DateTimePath<java.util.Date> createdTime = createDateTime("createdTime", java.util.Date.class);

	public final StringPath id = createString("id");

	public final StringPath updatedBy = createString("updatedBy");

	public final DateTimePath<java.util.Date> updatedTime = createDateTime("updatedTime", java.util.Date.class);

	public final com.querydsl.sql.PrimaryKey<TEtlTpl> primary = createPrimaryKey(id);

	public QTEtlTpl(String variable) {
		super(TEtlTpl.class, forVariable(variable), "null", "t_etl_tpl");
		addMetadata();
	}

	public QTEtlTpl(String variable, String schema, String table) {
		super(TEtlTpl.class, forVariable(variable), schema, table);
		addMetadata();
	}

	public QTEtlTpl(String variable, String schema) {
		super(TEtlTpl.class, forVariable(variable), schema, "t_etl_tpl");
		addMetadata();
	}

	public QTEtlTpl(Path<? extends TEtlTpl> path) {
		super(path.getType(), path.getMetadata(), "null", "t_etl_tpl");
		addMetadata();
	}

	public QTEtlTpl(PathMetadata metadata) {
		super(TEtlTpl.class, metadata, "null", "t_etl_tpl");
		addMetadata();
	}

	public void addMetadata() {
		addMetadata(tplName, ColumnMetadata.named("TPL_NAME").withIndex(2).ofType(Types.VARCHAR).withSize(32));
		addMetadata(tplType, ColumnMetadata.named("TPL_TYPE").withIndex(3).ofType(Types.INTEGER).withSize(1));
		addMetadata(tplFunType, ColumnMetadata.named("TPL_FUN_TYPE").withIndex(4).ofType(Types.INTEGER).withSize(1));
		addMetadata(tplContent, ColumnMetadata.named("TPL_CONTENT").withIndex(5).ofType(Types.LONGVARCHAR));
		addMetadata(tplDesp, ColumnMetadata.named("TPL_DESP").withIndex(5).ofType(Types.VARCHAR));
		addMetadata(createdBy, ColumnMetadata.named("CREATED_BY").withIndex(6).ofType(Types.VARCHAR).withSize(32));
		addMetadata(createdTime,
				ColumnMetadata.named("CREATED_TIME").withIndex(7).ofType(Types.TIMESTAMP).withSize(19));
		addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
		addMetadata(updatedBy, ColumnMetadata.named("UPDATED_BY").withIndex(8).ofType(Types.VARCHAR).withSize(32));
		addMetadata(updatedTime,
				ColumnMetadata.named("UPDATED_TIME").withIndex(9).ofType(Types.TIMESTAMP).withSize(19));
	}

}
