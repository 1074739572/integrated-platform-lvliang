package com.iflytek.integrated.platform.utils;

import com.iflytek.integrated.common.Constant;
import com.iflytek.integrated.common.utils.JackSonUtils;
import com.iflytek.integrated.common.utils.PinYinUtil;
import com.iflytek.integrated.platform.dto.ParamsDto;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLQueryFactory;
import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author czzhan
 * 公用方法
 */
@Component
public class Utils {
    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    @Autowired
    public SQLQueryFactory sqlQueryFactory;

    /**
     * 区域编码长度校验
     */
    public static final Pattern AREA_CODE = Pattern.compile("\\d{6}");


    /**
     * 获取模糊查询
     * @param text
     * @return
     */
    public static String createFuzzyText(String text) {
        return String.format(Constant.Fuzzy.FUZZY_SEARCH, text.trim());
    }

    /**
     * 获取右模糊查询
     * @param text
     * @return
     */
    public static String rightCreateFuzzyText(String text) {
        return String.format(Constant.Fuzzy.RIGHT_FUZZY_SEARCH, text.trim());
    }

    /**
     * 截取出省市区编码中的最小级
     * @param areaCode
     * @return
     */
    public static String subAreaCode(String areaCode){
        if(StringUtils.isEmpty(areaCode)){
            return "";
        }
        //校验是否符合区域编码格式
        if(AREA_CODE.matcher(areaCode).matches()){
            String three = areaCode.substring(4,6);
            if(Constant.NN_CODE.equals(three)){
                //如果区县没有选
                String two = areaCode.substring(2,4);
                if(Constant.NN_CODE.equals(two)){
                    //如果没有选市
                    return areaCode.substring(0,2);
                }
                return areaCode.substring(0,4);
            }
            return areaCode;
        }else {
            return areaCode;
        }
    }

    /**
     * 根据管理类型，自动生成code
     * @param path      表q类
     * @param codePath  code的字段
     * @param name      提交的名称
     * @return
     */
    public String generateCode(RelationalPath<?> path, StringPath codePath, String name){
        //校验类型是否为空
        if(StringUtils.isEmpty(name)){
            throw new RuntimeException("名称不能为空");
        }
        //名称中中文转拼音首字母小写
        String code = PinYinUtil.getFirstSpell(name);
        //查询编码已存在次数，递增
        Long count = sqlQueryFactory.select(path).from(path).where(codePath.eq(code)).fetchCount();
        if(count > 0){
            code += count;
        }
        logger.info("生成编码成功：" + code);
        return code;
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
