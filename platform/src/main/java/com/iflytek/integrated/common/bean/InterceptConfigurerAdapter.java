package com.iflytek.integrated.common.bean;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

/**
 * @author czzhan
 * @version 1.0
 * @date 2021/1/12 9:55
 */
@Configuration
public class InterceptConfigurerAdapter extends WebMvcConfigurationSupport {

    /**
     * 配置拦截器
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(new UserLoginIntercept()).addPathPatterns("/**");
        super.addInterceptors(registry);
    }
}
