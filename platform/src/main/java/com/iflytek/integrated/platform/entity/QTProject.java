package com.iflytek.integrated.platform.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QTProject is a Querydsl query type for TProject
 */
public class QTProject extends com.querydsl.sql.RelationalPathBase<TProject> {

    private static final long serialVersionUID = 1572234021;

    public static final QTProject qTProject = new QTProject("t_project");

    public final StringPath createdBy = createString("createdBy");

    public final DateTimePath<java.util.Date> createdTime = createDateTime("createdTime", java.util.Date.class);

    public final StringPath id = createString("id");

    public final StringPath projectCode = createString("projectCode");

    public final StringPath projectName = createString("projectName");

    public final StringPath projectStatus = createString("projectStatus");

    public final StringPath projectType = createString("projectType");

    public final StringPath updatedBy = createString("updatedBy");

    public final DateTimePath<java.util.Date> updatedTime = createDateTime("updatedTime", java.util.Date.class);

    public final com.querydsl.sql.PrimaryKey<TProject> primary = createPrimaryKey(id);

    public QTProject(String variable) {
        super(TProject.class, forVariable(variable), "null", "t_project");
        addMetadata();
    }

    public QTProject(String variable, String schema, String table) {
        super(TProject.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTProject(String variable, String schema) {
        super(TProject.class, forVariable(variable), schema, "t_project");
        addMetadata();
    }

    public QTProject(Path<? extends TProject> path) {
        super(path.getType(), path.getMetadata(), "null", "t_project");
        addMetadata();
    }

    public QTProject(PathMetadata metadata) {
        super(TProject.class, metadata, "null", "t_project");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(createdBy, ColumnMetadata.named("CREATED_BY").withIndex(6).ofType(Types.VARCHAR).withSize(32));
        addMetadata(createdTime, ColumnMetadata.named("CREATED_TIME").withIndex(7).ofType(Types.TIMESTAMP).withSize(19));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(projectCode, ColumnMetadata.named("PROJECT_CODE").withIndex(3).ofType(Types.VARCHAR).withSize(32));
        addMetadata(projectName, ColumnMetadata.named("PROJECT_NAME").withIndex(2).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(projectStatus, ColumnMetadata.named("PROJECT_STATUS").withIndex(4).ofType(Types.VARCHAR).withSize(1));
        addMetadata(projectType, ColumnMetadata.named("PROJECT_TYPE").withIndex(5).ofType(Types.VARCHAR).withSize(1));
        addMetadata(updatedBy, ColumnMetadata.named("UPDATED_BY").withIndex(8).ofType(Types.VARCHAR).withSize(32));
        addMetadata(updatedTime, ColumnMetadata.named("UPDATED_TIME").withIndex(9).ofType(Types.TIMESTAMP).withSize(19));
    }

}

