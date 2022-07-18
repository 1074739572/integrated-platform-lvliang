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
public class QTCadFile extends com.querydsl.sql.RelationalPathBase<TCadFile> {

    private static final long serialVersionUID = -200650066;

    public static final QTCadFile qtCadFile = new QTCadFile("t_cad_file");

    public final StringPath createdBy = createString("createdBy");

    public final DateTimePath<java.util.Date> createdTime = createDateTime("createdTime", java.util.Date.class);

    public final StringPath id = createString("id");

    public final StringPath docNo = createString("docNo");

    public final StringPath docTheme = createString("docTheme");

    public final StringPath docStandardNo = createString("docStandardNo");

    public final StringPath docStandardDesc = createString("docStandardDesc");

    public final StringPath docFileName = createString("docFileName");

    public final StringPath filePath = createString("filePath");

    public final StringPath updatedBy = createString("updatedBy");

    public final DateTimePath<java.util.Date> updatedTime = createDateTime("updatedTime", java.util.Date.class);

    public final com.querydsl.sql.PrimaryKey<TCadFile> primary = createPrimaryKey(id);

    public QTCadFile(String variable) {
        super(TCadFile.class, forVariable(variable), "null", "t_cad_file");
        addMetadata();
    }

    public QTCadFile(String variable, String schema, String table) {
        super(TCadFile.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTCadFile(String variable, String schema) {
        super(TCadFile.class, forVariable(variable), schema, "t_cad_file");
        addMetadata();
    }

    public QTCadFile(Path<? extends TCadFile> path) {
        super(path.getType(), path.getMetadata(), "null", "t_cad_file");
        addMetadata();
    }

    public QTCadFile(PathMetadata metadata) {
        super(TCadFile.class, metadata, "null", "t_cad_file");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(createdBy, ColumnMetadata.named("CREATED_BY").withIndex(11).ofType(Types.VARCHAR).withSize(32));
        addMetadata(createdTime, ColumnMetadata.named("CREATED_TIME").withIndex(12).ofType(Types.TIMESTAMP).withSize(19).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(docNo, ColumnMetadata.named("DOC_NO").withIndex(2).ofType(Types.VARCHAR).withSize(10).notNull());
        addMetadata(docTheme, ColumnMetadata.named("DOC_THEME").withIndex(3).ofType(Types.VARCHAR).withSize(100).notNull());
        addMetadata(docStandardNo, ColumnMetadata.named("DOC_STANDARD_NO").withIndex(4).ofType(Types.VARCHAR).withSize(100).notNull());
        addMetadata(docStandardDesc, ColumnMetadata.named("DOC_STANDARD_DESC").withIndex(5).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(docFileName, ColumnMetadata.named("DOC_FILE_NAME").withIndex(6).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(docFileName, ColumnMetadata.named("DOC_FILE_NAME").withIndex(7).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(filePath, ColumnMetadata.named("FILE_PATH").withIndex(8).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(updatedBy, ColumnMetadata.named("UPDATED_BY").withIndex(9).ofType(Types.VARCHAR).withSize(32));
        addMetadata(updatedTime, ColumnMetadata.named("UPDATED_TIME").withIndex(10).ofType(Types.TIMESTAMP).withSize(19));
    }

}

