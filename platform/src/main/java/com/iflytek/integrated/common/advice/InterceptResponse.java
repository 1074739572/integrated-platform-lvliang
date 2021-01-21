package com.iflytek.integrated.common.advice;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.servlet.http.HttpServletRequest;

/**
 * 全局处理接口返回
 * @author czzhan
 * @version 1.0
 * @date 2021/1/20 20:05
 */
@ControllerAdvice
public class InterceptResponse implements ResponseBodyAdvice<Object>{
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    /**
     * 拦截返回值,保存到attribute里面，在拦截器中读取
     */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
              Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        ServletServerHttpRequest req=(ServletServerHttpRequest)request;
        HttpServletRequest servletRequest = req.getServletRequest();
        servletRequest.setAttribute("response", body);
        return body;
    }

}