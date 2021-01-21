package com.iflytek.integrated.common.intercept;

import com.iflytek.integrated.common.Constant;
import com.iflytek.integrated.common.ResultDto;
import com.querydsl.sql.SQLQueryFactory;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author czzhan
 * @version 1.0
 * @date 2021/1/20 16:28
 */
@Data
@Component
public class AfterCompletionIntercept extends HandlerInterceptorAdapter {

    private String key;

    @Autowired
    private SQLQueryFactory sqlQueryFactory;

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
