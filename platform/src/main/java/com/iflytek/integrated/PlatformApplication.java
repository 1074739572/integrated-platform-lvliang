package com.iflytek.integrated;

import com.kvn.mockj.Function;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.MySQLTemplates;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.SpringSqlCloseListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;

/**
 * @author
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class PlatformApplication {

    @Value("${mock.string.len}")
    public String mockStringLen;

    @Value("${mock.number.min}")
    public String mockNumberMin;

    @Value("${mock.number.max}")
    public String mockNumberMax;

    @Value("${mock.array.size}")
    public String mockArraySize;

    /**
     * 定义queryFactory配置
     * @param dataSource
     * @return
     */
    @Bean
    public SQLQueryFactory queryFactory(DataSource dataSource) {
        Configuration configuration = new Configuration(MySQLTemplates.builder().build());
        configuration.addListener(new SpringSqlCloseListener(dataSource));
        return new SQLQueryFactory(
                configuration,
                () -> DataSourceUtils.getConnection(dataSource));
    }

    @Bean
    public Function Function(){
        return new Function(mockStringLen, mockNumberMin, mockNumberMax, mockArraySize);
    }

    public static void main(String[] args) {
        SpringApplication.run(PlatformApplication.class, args);
    }

}
