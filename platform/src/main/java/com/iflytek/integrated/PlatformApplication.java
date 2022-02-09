package com.iflytek.integrated;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import com.iflytek.integrated.common.advice.SqlCloseListener;
import com.iflytek.medicalboot.core.exception.MedicalFatalException;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.MySQLTemplates;
import com.querydsl.sql.OracleTemplates;
import com.querydsl.sql.PostgreSQLTemplates;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.SpringSqlCloseListener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.util.Properties;

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
//    @Bean
//    public SQLQueryFactory queryFactory(DataSource dataSource) {
//        Configuration configuration = new Configuration(MySQLTemplates.builder().build());
//        //不打印sql
//        configuration.addListener(new SqlCloseListener(dataSource));
//        return new SQLQueryFactory(
//                configuration,
//                () -> DataSourceUtils.getConnection(dataSource));
//    }
    
    @Bean
    @ConditionalOnMissingBean(SQLQueryFactory.class)
    public SQLQueryFactory queryFactory(@Autowired DataSource dataSource, @Value("${server.db}") String serverDB) {
        SQLTemplates sqlTemplates = null;
        if ("mysql".equalsIgnoreCase(serverDB)) {
            sqlTemplates = MySQLTemplates.builder().build();
        } else if ("oracle".equalsIgnoreCase(serverDB)) {
            sqlTemplates = OracleTemplates.builder().build();
        } else if ("postgresql".equalsIgnoreCase(serverDB)) {
            sqlTemplates = PostgreSQLTemplates.builder().build();
        } else {
            throw new MedicalFatalException("server.db未配置");
        }

        Configuration configuration = new Configuration(sqlTemplates);
        configuration.addListener(new SpringSqlCloseListener(dataSource));
        // 参考SpringConnectionProvider代码 取消强制事务 sharding-jdbc 和 mycat 为了性能最优化 查询不需要事务控制
        return new SQLQueryFactory(configuration, () -> DataSourceUtils.getConnection(dataSource));
    }

    @Bean
    @RefreshScope
    public DefaultKaptcha defaultKaptcha(Environment env) {
        DefaultKaptcha kaptcha = new DefaultKaptcha();
        kaptcha.setConfig(new Config(new Properties() {
            @Override
            public String getProperty(String key) {
                return env.getProperty(key);
            }

            @Override
            public String getProperty(String key, String defaultValue) {
                return env.getProperty(key, defaultValue);
            }
        }));
        return kaptcha;
    }

    public static void main(String[] args) {
        SpringApplication.run(PlatformApplication.class, args);
    }

}
