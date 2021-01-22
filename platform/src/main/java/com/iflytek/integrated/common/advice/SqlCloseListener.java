package com.iflytek.integrated.common.advice;

import com.querydsl.sql.*;

import javax.sql.DataSource;

/**
 * @author czzhan
 * @version 1.0
 * @date 2021/1/22 19:20
 */
public class SqlCloseListener extends SpringSqlCloseListener {

    public SqlCloseListener(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void preExecute(SQLListenerContext context) {
    }
}
