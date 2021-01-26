package com.iflytek.mock;

import com.iflytek.integrated.common.utils.JackSonUtils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author
 */
public class Mock {

    public static String mock(String template){
        try {
            JSONObject jsonObject = new JSONObject(template);
            Object rlt = Handler.gen(jsonObject, null, new Context());
            if(rlt instanceof JSONObject){
                return rlt.toString();
            }
            return JackSonUtils.transferToJson(rlt);
        }catch (JSONException e){
            throw new RuntimeException("解析JSON失败，格式有错误");
        }
    }

    public static String mock(String template, Context context){
        JSONObject jsonObject = new JSONObject(template);
        Object rlt = Handler.gen(jsonObject, null, context);
        if(rlt instanceof JSONObject){
            return rlt.toString();
        }
        return JackSonUtils.transferToJson(rlt);
    }

    /**
     * @param template mock模板
     * @return 返回 mock 对象
     */
    public static <T> T mock(String template, Class<T> rtnClass) {
        try {
            String mockStr = mock(template);
            return JackSonUtils.jsonToTransfer(mockStr, rtnClass);
        }catch (Exception e){
            throw new RuntimeException("解析JSON失败，格式有错误");
        }
    }


}
