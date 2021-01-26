package com.iflytek.integrated.common.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 描述:
 *
 * @author djgao
 * @create 2020/8/12-17:36
 */
public class JackSonUtils {

    private static Logger logger = LoggerFactory.getLogger(JackSonUtils.class);

    private static ObjectMapper mapper = new ObjectMapper();

    static {
        //序列化的时候序列对象的所有属性
        mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);

        //反序列化的时候如果多了其他属性,不抛出异常
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        //如果是空对象的时候,不抛异常
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        //取消时间的转化格式,默认是时间戳,可以取消,同时需要设置要表现的时间格式
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 对象转为Json字符串
     * @param data
     * @return
     */
    public static String transferToJson(Object data) {
        String jsonStr = null;
        try {
            jsonStr = mapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            logger.error("转换json字符串出错，错误信息：{}",e.getMessage());
        }
        return jsonStr;
    }

    /**
     * 对象转为byte数组
     * @param data
     * @return
     */
    public static byte[] transferToBytes(Object data) {
        byte[] byteArr = null;
        try {
            byteArr = mapper.writeValueAsBytes(data);
        } catch (JsonProcessingException e) {
            logger.error("对象转为byte数组出错，错误信息：{}",e.getMessage());
        }
        return byteArr;
    }

    /**
     * json字符串转为对象
     * @param str
     * @param valueType
     * @return
     */
    public static <T> T jsonToTransfer(String str, Class<T> valueType) {
        T data = null;
        try {
            data = mapper.readValue(str, valueType);
        } catch (Exception e) {
            logger.error("json字符串转为对象出错，错误信息：{}",e.getMessage());
        }
        return data;
    }

    /**
     * byte数组转为对象
     * @param byteArr
     * @param valueType
     * @return
     */
    public static <T> T bytesToTransfer(byte[] byteArr, Class<T> valueType) {
        T data = null;
        try {
            data = mapper.readValue(byteArr, valueType);
        } catch (Exception e) {
            logger.error("byte数组转为对象出错，错误信息：{}",e.getMessage());
        }
        return data;
    }

    /**
     * json数组字符串转换为list对象
     * @param str
     * @param <T>
     * @return
     */
    public static <T> List<T> jsonToTransferList(String str,Class<T> valueType){
        List<T> list = null;
        try {
            JavaType javaType = mapper.getTypeFactory().constructParametricType(ArrayList.class, valueType);
            list = mapper.readValue(str, javaType);
        } catch (Exception e) {
            logger.error("json数组字符串转换为list对象出错，错误信息：{}",e.getMessage());
        }
        return list;
    }

    /**
     * json数组字符串转换为list对象
     * @param object
     * @param <T>
     * @return
     */
    public static <T> List<T> jsonToTransferList(Object object,Class<T> valueType){
        String str = transferToJson(object);
        return jsonToTransferList(str,valueType);
    }


}