package com.iflytek.integrated.common.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.iflytek.integrated.common.intercept.AfterCompletionIntercept;
import com.iflytek.integrated.common.intercept.FileIntercept;
import com.iflytek.integrated.common.intercept.UserLoginIntercept;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.io.File;
import java.util.List;
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
    private FileIntercept fileIntercept;

    @Autowired
    private AfterCompletionIntercept afterCompletionIntercept;

    /**
     * 配置拦截器
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry){
        //文件拦截器
        InterceptorRegistration file = registry.addInterceptor(fileIntercept);
        file.addPathPatterns("/*/pt/vendor/addOrMod");
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

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter jackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        ObjectMapper objectMapper = jackson2HttpMessageConverter.getObjectMapper();
        //不显示为null的字段
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        objectMapper.registerModule(simpleModule);

        jackson2HttpMessageConverter.setObjectMapper(objectMapper);
        //放到第一个
        converters.add(0, jackson2HttpMessageConverter);
    }

}
