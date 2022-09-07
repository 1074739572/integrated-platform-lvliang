package com.iflytek.integrated.common.config;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.mitre.dsmiley.httpproxy.ProxyServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class GrafanaProxyServlet extends ProxyServlet {
    private static final Logger logger = LoggerFactory.getLogger(ProxyServlet.class);
    @Value("${proxy.grafana.key}")
    private String key;

    @Override
    protected HttpResponse doExecute(HttpServletRequest servletRequest, HttpServletResponse servletResponse, HttpRequest proxyRequest) throws IOException {
        if (this.doLog) {
            this.log("proxy " + servletRequest.getMethod() + " uri: " + servletRequest.getRequestURI() + " -- " + proxyRequest.getRequestLine().getUri());
        }

        logger.info("进入拦截中。。。。。。");

        proxyRequest.setHeader("Authorization", "Bearer "+key);

        HttpResponse response = super.doExecute(servletRequest, servletResponse, proxyRequest);

        return response;
    }

}
