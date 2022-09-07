package com.iflytek.integrated.common.config;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServlet;
import java.util.Map;

@Configuration
public class ProxyServletConfiguration {
    /**
     * 读取配置文件中路由设置
     */
    @Value("${proxy.grafana.servlet_url}")
    private String servlet_url;
    /**
     * 读取配置中代理目标地址
     */
    @Value("${proxy.grafana.target_url}")
    private String target_url;

    @Value("${proxy.grafana.key}")
    private String key;

    @Bean
    public HttpServlet createProxyServlet() {
        /** 创建新的ProxyServlet */
        return new GrafanaProxyServlet();
    }

        @Bean
    public ServletRegistrationBean proxyServletRegistration() {
        ServletRegistrationBean registrationBean = new ServletRegistrationBean(createProxyServlet(), servlet_url);
        //设置网址以及参数
        Map<String, String> params = ImmutableMap.of("targetUri", target_url, "log", "true");
        registrationBean.setInitParameters(params);
        return registrationBean;
    }
}
