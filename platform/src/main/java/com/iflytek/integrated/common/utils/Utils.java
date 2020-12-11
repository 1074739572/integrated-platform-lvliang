package com.iflytek.integrated.common.utils;

import com.iflytek.integrated.common.Constant;
import org.bouncycastle.pqc.math.linearalgebra.IntUtils;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * @author czzhan
 * 公用方法
 */
public class Utils {

    public static final Pattern AREA_CODE = Pattern.compile("\\d{6}");

    /**
     * 获取模糊查询
     * @param text
     * @return
     */
    public static String createFuzzyText(String text) {
        return String.format(Constant.FUZZY_SEARCH, text.trim());
    }

    /**
     * 获取右模糊查询
     * @param text
     * @return
     */
    public static String rightCreateFuzzyText(String text) {
        return String.format(Constant.RIGHT_FUZZY_SEARCH, text.trim());
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
}
