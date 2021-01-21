package com.iflytek.integrated.common.advice;
import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.common.intercept.AfterCompletionIntercept;
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
     * 拦截返回值,判断是否存在拦截器实体中，有配置请求返回拦截器，则将接口请求传给拦截器
     */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
              Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        ServletServerHttpRequest req=(ServletServerHttpRequest)request;
        HttpServletRequest servletRequest = req.getServletRequest();
        AfterCompletionIntercept intercept = (AfterCompletionIntercept) servletRequest.getAttribute(AfterCompletionIntercept.Intercept);
        if(intercept != null){
            intercept.setResultDto((ResultDto) body);
        }
        return body;
    }

}