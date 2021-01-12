package com.iflytek.integrated.common.bean;

import com.iflytek.integrated.common.UserDto;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 用户登录拦截器
 * @author czzhan
 * @version 1.0
 * @date 2021/1/12 9:25
 */
@Component
public class UserLoginIntercept extends HandlerInterceptorAdapter {

    public static UserDto LOGIN_USER = new UserDto();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception{
        Map req = request.getParameterMap();
        if(req.containsKey("loginUserId") && req.get("loginUserId") != null){
            String[] loginUserId = (String[]) req.get("loginUserId");
            if(loginUserId.length > 0){
                LOGIN_USER.setLoginUserId(loginUserId[0]);
            }
        }
        if(req.containsKey("loginUserName") && req.get("loginUserName")!= null){
            String[] loginUserName = (String[]) req.get("loginUserName");
            if(loginUserName.length > 0){
                LOGIN_USER.setLoginUserName(loginUserName[0]);
            }
        }
        return super.preHandle(request, response, handler);
    }
}
