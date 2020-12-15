package com.iflytek.integrated.common.utils;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.iflytek.integrated.common.Constant;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLQueryFactory;
import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

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
    public static void strIsJsonOrXml(String mock){
        if(StringUtils.isEmpty(mock)){
            logger.error("不能为空！");
        }
        try {
            //判断是否是json类型
            JSONObject.parseObject(mock);
        } catch (JSONException e) {
            //判断是否是xml结构
            try {
                //如果是xml结构
                DocumentHelper.parseText(mock);
            } catch (DocumentException i) {
                logger.error("格式错误，非标准json或者xml格式！");
            }
        }
    }

}
