package com.kvn.mockj;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * @author
 */
public class Mock {

    public static String mock(String template){
        JSONObject jsonT = JSON.parseObject(template);
        Object rlt = Handler.gen(jsonT, null, new Context());
        return JSON.toJSONString(rlt);
    }

    public static String mock(String template, Context context){
        JSONObject jsonT = JSON.parseObject(template);
        Object rlt = Handler.gen(jsonT, null, context);
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
