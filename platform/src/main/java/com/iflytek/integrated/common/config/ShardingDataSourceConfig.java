package com.iflytek.integrated.common.config;

import com.querydsl.sql.PostgreSQLTemplates;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.SQLTemplates;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.InlineShardingStrategyConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
public class ShardingDataSourceConfig {
    @Value("${spring.shardingsphere.dataSource.names}")
    String dbNames;

    @Autowired
    private Environment env;

    private Map<String, DataSource> dataSourceMap = new HashMap<>();

    @Bean("shardingDateSource")
    public DataSource shardingdDataSource() throws SQLException {
        return buildDataSource();
    }

    @Bean("shardingSqlQueryFactory")
    public SQLQueryFactory shardingSqlQueryFactory(@Qualifier("shardingDateSource")  DataSource dataSource) {
        SQLTemplates templates = PostgreSQLTemplates.builder().build();
        com.querydsl.sql.Configuration configuration = new com.querydsl.sql.Configuration(templates);
        return new SQLQueryFactory(configuration, dataSource);
    }

    private DataSource buildDataSource() throws SQLException {
        initDataSourceMap();
        // 具体分库分表策略，按什么规则来分
        ShardingRuleConfiguration conf = new ShardingRuleConfiguration();

        // table rule
        TableRuleConfiguration tableRule = new TableRuleConfiguration("t_log", "ds0.t_log_$->{(1..31).collect{t ->t.toString().padLeft(2,'0')}}");

        conf.getTableRuleConfigs().add(tableRule);

        conf.setDefaultTableShardingStrategyConfig(
                new StandardShardingStrategyConfiguration("created_time",new DayPreciseShardingAlgorithm(),new DayRangeShardingAlgorithm()));
//                new InlineShardingStrategyConfiguration("created_time", "t_log_$->{created_time.toString().substring(8,10)}"));

        Properties properties = new Properties();
        properties.put("sql.show",true);

        DataSource dataSource = ShardingDataSourceFactory.createDataSource(dataSourceMap, conf,properties);

        return dataSource;

    }

    public void initDataSourceMap() {
        for (String name : dbNames.split(",")) {
            HikariDataSource ds = new HikariDataSource();
            ds.setDriverClassName(env.getProperty("spring.shardingsphere.dataSource."+name+".driver-class-name"));
            ds.setJdbcUrl(env.getProperty("spring.shardingsphere.dataSource."+name+".url"));
            ds.setUsername(env.getProperty("spring.shardingsphere.dataSource."+name+".username"));
            ds.setPassword(env.getProperty("spring.shardingsphere.dataSource."+name+".password"));
            dataSourceMap.put(name, ds);
        }

        System.out.println(dataSourceMap);
    }

}