package com.iflytek.integrated.common.config;

import com.iflytek.integrated.common.filter.RequestLimitFilter;
import com.iflytek.integrated.common.filter.UrlFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * 过滤器配置
 * @author czzhan
 * @version 1.0
 * @date 2021/1/28 8:59
 */
@Configuration
public class WebFilterConfig {

    @Bean
    public FilterRegistrationBean<UrlFilter> urlFilter() {
        FilterRegistrationBean<UrlFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new UrlFilter());
        registration.addUrlPatterns("/integratedPlatform/*");
        registration.setName("urlFilter");
        registration.setOrder(1);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<RequestLimitFilter> reqResFilter() {
        FilterRegistrationBean<RequestLimitFilter> filterRegistrationBean = new FilterRegistrationBean<>();

        RequestLimitFilter requestLimitFilter = new RequestLimitFilter();
        filterRegistrationBean.setFilter(requestLimitFilter);

        //设置过滤器名称
        filterRegistrationBean.setName("reqResFilter");
        //配置多个过滤规则
        List<String> urls = new ArrayList<>();
        urls.add("/*");
        filterRegistrationBean.setUrlPatterns(urls);
        //执行次序
        filterRegistrationBean.setOrder(2);
        return filterRegistrationBean;
    }
}
