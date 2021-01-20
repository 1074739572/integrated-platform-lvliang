package com.iflytek.integrated.common.intercept;

import com.iflytek.integrated.common.Constant;
import com.iflytek.integrated.common.ResultDto;
import org.springframework.stereotype.Component;
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
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //获取返回结果
        ResultDto result = (ResultDto) request.getAttribute("response");
        if(result != null) {
            if(Constant.ResultCode.SUCCESS_CODE == result.getCode() && result.getData() != null){
                String id = result.getData().toString();
            }
        }
    }
}
