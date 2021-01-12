package com.iflytek.integrated.common.bean;

import com.alibaba.fastjson.JSON;
import com.iflytek.integrated.common.UserDto;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception{
        try {
            //获取登录信息，并进行base64解码
            String userBase64 = request.getHeader(HEADER_X_USER);
            if(StringUtils.isNotBlank(userBase64)){
                byte[] bytes = Base64.decodeBase64(userBase64);
                String user = new String(bytes, "UTF-8");
                UserDto dto = JSON.parseObject(user, UserDto.class);
                if(StringUtils.isNotBlank(dto.getName())){
                    LOGIN_USER.setName(dto.getName());
                }
            }

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
        }
        return super.preHandle(request, response, handler);
    }
}
