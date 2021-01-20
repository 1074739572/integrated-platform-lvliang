package com.iflytek.integrated.common.bean;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author czzhan
 * @version 1.0
 * @date 2021/1/20 16:28
 */
@Component
public class AfterCompletionIntercept extends HandlerInterceptorAdapter {

    private String key;

    public AfterCompletionIntercept(){
    }

    public AfterCompletionIntercept(String key){
        this.key = key;
    }

    /**
     * 业务处理器请求处理完成之后执行方法
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception{

        System.out.println("afterCompletion...");
    }
}
