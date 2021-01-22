package com.iflytek.integrated;

import com.querydsl.sql.Configuration;
import com.querydsl.sql.MySQLTemplates;
import com.querydsl.sql.SQLQueryFactory;
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

    /**
     * 定义queryFactory配置
     * @param dataSource
     * @return
     */
    @Bean
    public SQLQueryFactory queryFactory(DataSource dataSource) {
        Configuration configuration = new Configuration(MySQLTemplates.builder().build());

        //开启时日志打印sql，部署时无需打印
        //* configuration.addListener(new SpringSqlCloseListener(dataSource));
        return new SQLQueryFactory(
                configuration,
                () -> DataSourceUtils.getConnection(dataSource));
    }

    public static void main(String[] args) {
        SpringApplication.run(PlatformApplication.class, args);
    }

}
