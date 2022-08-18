package com.iflytek.integrated.common.config;

import com.iflytek.integrated.common.intercept.AfterCompletionIntercept;
import com.iflytek.integrated.common.intercept.UserLoginIntercept;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.io.File;
import java.util.Map;

/**
 * WebMvcConfig，对url配置
 * @author czzhan
 * @version 1.0
 * @date 2021/1/12 9:55
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "cache.redis")
public class WebInterceptConfig extends WebMvcConfigurationSupport {

    private Map<String, String[]> map;

    @Autowired
    private UserLoginIntercept userLoginIntercept;

    @Autowired
    private AfterCompletionIntercept afterCompletionIntercept;

    /**
     * 配置拦截器
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry){
        //用户登录拦截器
//        InterceptorRegistration rationUserLogin = registry.addInterceptor(userLoginIntercept);
//        rationUserLogin.addPathPatterns("/*/pt/**").excludePathPatterns("/**/loginManage/**");

//        //通过获取配置文件，接口处理后执行配置文件
//        for (String key: map.keySet()){
//            //创建一个AfterCompletionIntercept拦截器
//            AfterCompletionIntercept completionIntercept = new AfterCompletionIntercept();
//            //给拦截器添加key和调用方法
//            BeanUtils.copyProperties(afterCompletionIntercept,completionIntercept);
//            completionIntercept.setKey(key);
//            //添加拦截器
//            InterceptorRegistration rationAfterCompletion = registry.addInterceptor(completionIntercept);
//            rationAfterCompletion.addPathPatterns(map.get(key));
//        }
        super.addInterceptors(registry);
    }

    /**
     * swagger访问配置
     * @param registry
     */
    @Override
    public  void addResourceHandlers(ResourceHandlerRegistry registry) {
        //新增图片静态资源处理
        ApplicationHome h = new ApplicationHome(getClass());
        File jarF = h.getSource();
        String dirPath = jarF.getParentFile().getParentFile().toString() + "/upload/";

        String os = System.getProperty("os.name");

        if (os.toLowerCase().startsWith("win")) {  //如果是Windows系统
            registry.addResourceHandler("/file/**").addResourceLocations("file:" + dirPath);
        } else {
            registry.addResourceHandler("/file/**").addResourceLocations("file:" + dirPath);
        }

        registry.addResourceHandler("swagger-ui.html").addResourceLocations(
                "classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations(
                "classpath:/META-INF/resources/webjars/");

        super.addResourceHandlers(registry);
    }
}
