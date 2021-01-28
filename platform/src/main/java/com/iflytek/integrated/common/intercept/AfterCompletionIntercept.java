package com.iflytek.integrated.common.intercept;

import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.platform.common.RedisService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 缓存优化，统一接口返回处理
 * @author czzhan
 * @version 1.0
 * @date 2021/1/20 16:28
 */
@Data
@Component
public class AfterCompletionIntercept extends HandlerInterceptorAdapter {

    /**
     * 附加到request请求的名称
     */
    public static String Intercept = "intercept";

    private String key;

    private ResultDto resultDto;

    @Autowired
    private RedisService redisService;

    /**
     * 请求执行前方法
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        request.setAttribute(Intercept, this);
        return super.preHandle(request, response, handler);
    }

    /**
     * 业务处理器请求处理完成之后执行方法
     * @param request
     * @param response
     * @param handler
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        //获取返回结果
        ResultDto result = resultDto;
        if(result.getData() != null){
            redisService.delRedisKey(result.getCode(),result.getData().toString(), key);
        }
    }

    /**
     * 回收方法
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //如果请求中存在拼接的字段，手动去处
        if(request.getAttribute(Intercept) != null){
            request.removeAttribute(Intercept);
        }
        //如果有保存返回，清空返回
        if(resultDto != null){
            resultDto = null;
        }
    }


}
