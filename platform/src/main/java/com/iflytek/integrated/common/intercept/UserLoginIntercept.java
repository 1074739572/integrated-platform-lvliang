package com.iflytek.integrated.common.intercept;

import com.iflytek.integrated.common.dto.UserDto;
import com.iflytek.integrated.platform.utils.JwtTokenUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
    private static final Logger logger = LoggerFactory.getLogger(UserLoginIntercept.class);

    public static UserDto LOGIN_USER = new UserDto();

    public static String HEADER_X_USER = "x-user";

    public static String LOGIN_USER_ID = "loginUserId";

    public static String LOGIN_USER_NAME = "loginUserName";

    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    @Value("${jwt.username}")
    private String username;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception{
        try {
            String authHeader = request.getHeader(TOKEN_HEADER);
            if (StringUtils.isBlank(authHeader) || !authHeader.startsWith(TOKEN_PREFIX)) {
                throw new Exception("当前用户没有登录");
            }

            String token = authHeader.substring(7);
            String name = (String)JwtTokenUtils.getUsername(token);
            if(!username.equals(name)){
                throw new Exception("访问token无效");
            }
            LOGIN_USER.setName(username);

            //获取请求参数中的用户信息
            Map req = request.getParameterMap();
            if(req.containsKey(LOGIN_USER_ID) && req.get(LOGIN_USER_ID) != null){
                String[] loginUserId = (String[]) req.get(LOGIN_USER_ID);
                if(loginUserId.length > 0){
                    LOGIN_USER.setLoginUserId(loginUserId[0]);
                }
            }
            if(req.containsKey(LOGIN_USER_NAME) && req.get(LOGIN_USER_NAME)!= null){
                String[] loginUserName = (String[]) req.get(LOGIN_USER_NAME);
                if(loginUserName.length > 0){
                    LOGIN_USER.setLoginUserName(loginUserName[0]);
                }
            }
        }
        catch (Exception e){
            logger.error("登录用户获取失败");
            throw new Exception("访问token无效");
        }
        return super.preHandle(request, response, handler);
    }
}
