//package com.iflytek.integrated.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.client.ClientHttpRequestFactory;
//import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
//import org.springframework.http.client.SimpleClientHttpRequestFactory;
//import org.springframework.http.converter.HttpMessageConverter;
//import org.springframework.http.converter.StringHttpMessageConverter;
//import org.springframework.web.client.RestTemplate;
//import org.apache.http.client.HttpClient;
//import org.apache.http.impl.client.HttpClientBuilder;
//
//import java.nio.charset.StandardCharsets;
//import java.util.List;
//
///**
//*
//* @author weihe9
//* @date 2020/12/12 19:00
//*/
//@Configuration
//public class RestTemplateConfig {
//
//    @Value("${remote.maxTotalConnect}")
//    private int maxTotalConnect; //连接池的最大连接数
//    @Value("${remote.maxConnectPerRoute}")
//    private int maxConnectPerRoute; //单个主机的最大连接数
//    @Value("${remote.connectTimeout}")
//    private int connectTimeout; //连接超时
//    @Value("${remote.readTimeout}")
//    private int readTimeout; //读取超时
//
//    @Bean
//    public RestTemplate restTemplate(ClientHttpRequestFactory factory){
//        RestTemplate restTemplate = new RestTemplate(factory);
//        List<HttpMessageConverter<?>> converterList = restTemplate.getMessageConverters();
//        //重新设置StringHttpMessageConverter字符集为UTF-8，解决中文乱码问题
//        HttpMessageConverter<?> converterTarget = null;
//        for (HttpMessageConverter<?> item : converterList) {
//            if (StringHttpMessageConverter.class == item.getClass()) {
//                converterTarget = item;
//                break;
//            }
//        }
//        if (null != converterTarget) {
//            converterList.remove(converterTarget);
//        }
//        converterList.add(new StringHttpMessageConverter(StandardCharsets.UTF_8));
//        return restTemplate;
//    }
//
//    @Bean
//    public ClientHttpRequestFactory clientHttpRequestFactory(){// 创建httpCilent工厂
//        if(this.maxTotalConnect <= 0){
//            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
//            factory.setConnectTimeout(this.connectTimeout);
//            factory.setReadTimeout(this.readTimeout);
//            return factory;
//        }
//        HttpClient httpClient = HttpClientBuilder.create().setMaxConnTotal(this.maxTotalConnect).setMaxConnPerRoute(this.maxConnectPerRoute).build();
//        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
//        factory.setConnectTimeout(this.connectTimeout);
//        factory.setReadTimeout(this.readTimeout);
//        return factory;
//    }
//
//
//}
