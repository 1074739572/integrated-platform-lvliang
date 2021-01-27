package com.iflytek.integrated.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author czzhan
 * @version 1.0
 * @date 2021/1/27 10:25
 */
@EnableSwagger2
@Configuration
public class SwaggerConfig {

    @Value("${swagger.enable:true}")
    private boolean enableSwagger;

    @Value("${swagger.basePackage:com.iflytek.integrated.platform}")
    private String basePackage;

    @Value("${swagger.title:智医数据对接平台}")
    private String title;

    @Value("${swagger.current.version:v1}")
    String currentVersion;

    @Value("${swagger.compatible.version:v1}")
    String compatibleVersion;

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage(basePackage))
                .paths(PathSelectors.any())
                .build()
                .enable(enableSwagger);
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
            .title(title)
            .description("当前API版本" + currentVersion + " 兼容API版本" + compatibleVersion)
            .version(currentVersion)
            .build();
    }
}
