package com.iflytek.integrated.common.advice;

import com.querydsl.sql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

/**
 * @author czzhan
 * @version 1.0
 * @date 2021/1/22 19:20
 */
public class SqlCloseListener extends SpringSqlCloseListener {
    private static final Logger log = LoggerFactory.getLogger(SqlCloseListener.class);

    public SqlCloseListener(DataSource dataSource) {
        super(dataSource);
    }

    /**
     * 重写了SpringSqlCloseListener的此方法，不再打印
     * @param context
     */
    @Override
    public void preExecute(SQLListenerContext context) {
        if (log.isDebugEnabled()) {
            SQLBindings sqlBindings = context.getSQLBindings();
            if (sqlBindings != null && sqlBindings.getNullFriendlyBindings() != null) {
                log.debug("SQL: {}", sqlBindings.getSQL());
                log.debug("Bindings: {}", sqlBindings.getNullFriendlyBindings().toString());
            }
        }
    }
}
