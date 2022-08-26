package com.iflytek.integrated.common.config;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.querydsl.sql.PostgreSQLTemplates;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.SQLTemplates;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {
    @Primary
    @Bean(name = "masterDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.master")
    public DataSource masterDataSource() {
        return new AtomikosDataSourceBean();
    }

    @Bean(name = "sqlQueryFactory")
    public SQLQueryFactory sqlQueryFactory(@Qualifier("masterDataSource") DataSource dataSource) {
        SQLTemplates templates = PostgreSQLTemplates.builder().build();
        com.querydsl.sql.Configuration configuration = new com.querydsl.sql.Configuration(templates);
        return new SQLQueryFactory(configuration, dataSource);
    }
//    // 主数据源配置 ds1数据源
//    @Primary
//    @Bean(name = "ds1DataSourceProperties")
//    @ConfigurationProperties(prefix = "spring.datasource.master")
//    public DataSourceProperties ds1DataSourceProperties() {
//        return new DataSourceProperties();
//    }
//
//    // 主数据源 ds1数据源
//    @Primary
//    @Bean(name = "masterDataSource")
//    public DataSource ds1DataSource(@Qualifier("ds1DataSourceProperties") DataSourceProperties dataSourceProperties) {
//        return dataSourceProperties.initializeDataSourceBuilder().build();
//    }
//
//    @Primary
//    @Bean(name = "sqlQueryFactory")
//    public SQLQueryFactory sqlQueryFactory(@Qualifier("masterDataSource") DataSource dataSource) {
//        SQLTemplates templates = PostgreSQLTemplates.builder().printSchema().build();
//        com.querydsl.sql.Configuration configuration = new com.querydsl.sql.Configuration(templates);
//        return new SQLQueryFactory(configuration, dataSource);
//    }
}