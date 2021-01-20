package com.iflytek.integrated.common.bean;

import com.iflytek.integrated.common.intercept.AfterCompletionIntercept;
import com.iflytek.integrated.common.intercept.UserLoginIntercept;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.Map;

/**
 * @author czzhan
 * @version 1.0
 * @date 2021/1/12 9:55
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "cache.redis")
public class InterceptConfigurerAdapter extends WebMvcConfigurationSupport {

    private Map<String, String[]> map;

    /**
     * 配置拦截器
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry){
        //用户登录拦截器
        InterceptorRegistration registration1 = registry.addInterceptor(new UserLoginIntercept());
        registration1.addPathPatterns("/*/pt/**");

        //通过获取配置文件，接口处理后执行配置文件
        for (String key: map.keySet()){
            InterceptorRegistration registration2 = registry.addInterceptor(new AfterCompletionIntercept(key));
            registration2.addPathPatterns(map.get(key));
        }
        super.addInterceptors(registry);
    }

}
