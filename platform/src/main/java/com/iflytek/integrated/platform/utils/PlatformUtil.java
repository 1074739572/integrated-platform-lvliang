package com.iflytek.integrated.platform.utils;

import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.common.utils.JackSonUtils;
import com.iflytek.integrated.platform.dto.ParamsDto;
import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author czzhan
 * 公用方法
 */
public class PlatformUtil {

    /**
     * 获取模糊查询
     * @param text
     * @return
     */
    public static String createFuzzyText(String text) {
        return String.format(Constant.Fuzzy.FUZZY_SEARCH, text.trim());
    }

    /**
     * 校验字符串是否是json或者xml
     * @param mock
     */
    public static String strIsJsonOrXml(String mock){
        if(StringUtils.isEmpty(mock)){
            throw new RuntimeException("不能为空！");
        }
        try {
            //判断是否是JSONObject类型
            new JSONObject(mock);
            return Constant.ParamFormatType.JSON.getType();
        } catch (JSONException e) {
            try {
                //判断是否是JSONArray类型
                new JSONArray(mock);
                return Constant.ParamFormatType.JSON.getType();
            } catch (JSONException f) {
                //判断是否是xml结构
                try {
                    //如果是xml结构
                    DocumentHelper.parseText(mock);
                    return Constant.ParamFormatType.XML.getType();
                } catch (DocumentException i) {
                    throw new RuntimeException("格式错误，非标准json或者xml格式！");
                }
            }
        }
    }

    /**
     * 解析json获取建值数组
     * @param paramJson
     * @return
     */
    public static List<ParamsDto> jsonFormat(String paramJson){
        List<ParamsDto> dtoList = new ArrayList<>();
        Object object;
        try{
            object = JackSonUtils.jsonToTransfer(paramJson, LinkedHashMap.class);
        }
        catch (IOException e){
            object = JackSonUtils.jsonToTransferList(paramJson, LinkedHashMap.class);
        }
        format(object, dtoList,new ParamsDto());
        return dtoList;
    }

    /**
     * 根据参数模板（json）获取key-value
     * @param object
     * @param dtoList
     */
    private static void format(Object object, List<ParamsDto> dtoList, ParamsDto paramsDto){
        //LinkedHashMap类型
        if(object instanceof LinkedHashMap) {
            LinkedHashMap hashMap = (LinkedHashMap) object;
            for (Object key: hashMap.keySet()) {
                Object o = hashMap.get(key);
                ParamsDto dto = new ParamsDto();
                dto.setParamKey((String) key);
                if(o instanceof LinkedHashMap) {
                    dto.setParamValue("");
                    dto.setParamType("object");
                }
                else {
                    dto.setParamType(o != null ? o.getClass().getSimpleName(): "");
                    dto.setParamValue(o);
                }
                //继续循环，直到所有字段都取到最后一层
                format(o,dtoList, dto);
            }
        }
        else if(object instanceof List){
            List list = (List) object;
            for(int i = 0; i < list.size(); i ++) {
                if(list.get(i) instanceof LinkedHashMap){
                    paramsDto.setParamType("object[]");
                    paramsDto.setParamValue("");
                    dtoListAddParamsDto(dtoList,paramsDto);
                }else {
                    paramsDto.setParamType("List");
                    paramsDto.setParamValue(list.get(i));
                }
                format(list.get(i),dtoList, paramsDto);
            }
        }
        else {
            dtoListAddParamsDto(dtoList,paramsDto);
        }
    }

    /**
     * 保存键值对到实体列表
     * @param dtoList
     * @param paramsDto
     */
    private static void dtoListAddParamsDto(List<ParamsDto> dtoList, ParamsDto paramsDto){
        //保存键值对，并去掉已经存在的key
        if(paramsDto != null && StringUtils.isNotBlank(paramsDto.getParamKey())){
            List<String> keyList = dtoList.stream().map(ParamsDto::getParamKey).collect(Collectors.toList());
            String paramsKey = paramsDto.getParamKey();
            if(!keyList.contains(paramsKey)){
                dtoList.add(paramsDto);
            }
        }
    }
}
