package com.kvn.mockj;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

/**
 * @author
 */
public class Mock {

    public static String mock(String template){
        try {
            JSONObject jsonObject = JSON.parseObject(template);
            Object rlt = Handler.gen(jsonObject, null, new Context());
            return JSON.toJSONString(rlt);
        }catch (JSONException e){
            throw new RuntimeException("解析JSON失败，格式有错误");
        }
    }

    public static String mock(String template, Context context){
        JSONObject jsonObject = JSON.parseObject(template);
        Object rlt = Handler.gen(jsonObject, null, context);
        return JSON.toJSONString(rlt);
    }

    /**
     * @param template mock模板
     * @return 返回 mock 对象
     */
    public static <T> T mock(String template, Class<T> rtnClass) {
        String mockStr = mock(template);
        return JSON.parseObject(mockStr, rtnClass);
    }


}
