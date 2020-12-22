package com.iflytek.integrated.platform.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QTPlugin is a Querydsl query type for TPlugin
 */
public class QTPlugin extends com.querydsl.sql.RelationalPathBase<TPlugin> {

    private static final long serialVersionUID = 322446759;

    public static final QTPlugin qTPlugin = new QTPlugin("t_plugin");

    public final StringPath createdBy = createString("createdBy");

    public final DateTimePath<java.util.Date> createdTime = createDateTime("createdTime", java.util.Date.class);

    public final StringPath id = createString("id");

    public final StringPath pluginCode = createString("pluginCode");

    public final StringPath pluginContent = createString("pluginContent");

    public final StringPath pluginInstruction = createString("pluginInstruction");

    public final StringPath pluginName = createString("pluginName");

    public final StringPath typeId = createString("typeId");

    public final StringPath updatedBy = createString("updatedBy");

    public final DateTimePath<java.util.Date> updatedTime = createDateTime("updatedTime", java.util.Date.class);

    public final com.querydsl.sql.PrimaryKey<TPlugin> primary = createPrimaryKey(id);

    public QTPlugin(String variable) {
        super(TPlugin.class, forVariable(variable), "null", "t_plugin");
        addMetadata();
    }

    public QTPlugin(String variable, String schema, String table) {
        super(TPlugin.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTPlugin(String variable, String schema) {
        super(TPlugin.class, forVariable(variable), schema, "t_plugin");
        addMetadata();
    }

    public QTPlugin(Path<? extends TPlugin> path) {
        super(path.getType(), path.getMetadata(), "null", "t_plugin");
        addMetadata();
    }

    public QTPlugin(PathMetadata metadata) {
        super(TPlugin.class, metadata, "null", "t_plugin");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(createdBy, ColumnMetadata.named("CREATED_BY").withIndex(7).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(createdTime, ColumnMetadata.named("CREATED_TIME").withIndex(8).ofType(Types.TIMESTAMP).withSize(19).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(pluginCode, ColumnMetadata.named("PLUGIN_CODE").withIndex(3).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(pluginContent, ColumnMetadata.named("PLUGIN_CONTENT").withIndex(6).ofType(Types.LONGVARCHAR).withSize(65535));
        addMetadata(pluginInstruction, ColumnMetadata.named("PLUGIN_INSTRUCTION").withIndex(5).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(pluginName, ColumnMetadata.named("PLUGIN_NAME").withIndex(2).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(typeId, ColumnMetadata.named("TYPE_ID").withIndex(4).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(updatedBy, ColumnMetadata.named("UPDATED_BY").withIndex(9).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(updatedTime, ColumnMetadata.named("UPDATED_TIME").withIndex(10).ofType(Types.TIMESTAMP).withSize(19).notNull());
    }

}

