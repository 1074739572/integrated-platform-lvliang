package com.iflytek.integrated.common.bean;

import com.iflytek.integrated.common.utils.ase.AesUtil;
import com.kvn.mockj.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 特殊配置文件初始化调用
 * @author czzhan
 * @version 1.0
 * @date 2021/1/11 9:14
 */
@Configuration
public class InitConfigBean {

    @Value("${mock.string.len}")
    public String mockStringLen;

    @Value("${mock.number.min}")
    public String mockNumberMin;

    @Value("${mock.number.max}")
    public String mockNumberMax;

    @Value("${mock.array.size}")
    public String mockArraySize;

    @Value("${mock.date.paramStr}")
    public String paramStr;

    @Value("${aes.key:w5xv7[Nmc0Z/3U^X}")
    private String key;

    @Bean
    public void initVoid(){

        //mock模拟参数入参
        new Function(mockStringLen, mockNumberMin, mockNumberMax, mockArraySize, paramStr);

        //加密秘钥配置
        new AesUtil(key);
    }
}
