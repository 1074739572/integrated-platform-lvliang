package com.kvn.mockj;

import com.kvn.mockj.utils.DateUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Random;

/**
 * 扩展函数
 * @author
 */
public class Function {
    private static final Random RANDOM = new Random();
    private static final String LETTERS = "abcdefghijklmnopqrstuvwxyz";

    public static String stringLen;

    public static String numberMin;

    public static String numberMax;

    public static String arraySize;

    public Function(String mockStringLen, String mockNumberMin, String mockNumberMax, String mockArraySize){
        //初始化配置
        Function.stringLen = mockStringLen;
        Function.numberMin = mockNumberMin;
        Function.numberMax = mockNumberMax;
        Function.arraySize = mockArraySize;
    }

    public static boolean $boolean(String paramStr){
        return RANDOM.nextBoolean();
    }

    public static Integer $natural(String paramStr){
        return Math.abs($integer(paramStr));
    }

    public static char $character(String paramStr){
        String baseChars = paramStr == null ? LETTERS : paramStr;
        return LETTERS.charAt(RANDOM.nextInt(baseChars.length()));
    }

    /**
     * 生成随机string，@string(a) a表示字符个数
     * @param paramStr
     * @return
     */
    public static String $string(String paramStr){
        if(StringUtils.isBlank(paramStr)){
            paramStr = stringLen;
        }
        if (StringUtils.isBlank(paramStr)){
            return RandomStringUtils.randomAlphanumeric(10);
        }
        return RandomStringUtils.randomAlphanumeric(Integer.parseInt(paramStr));
    }

    /**
     * 生成随机int，@integer(a,b)（a，b）表示数值范围
     * @param paramStr
     * @return
     */
    public static Integer $integer(String paramStr){
        if (StringUtils.isBlank(paramStr)
                && StringUtils.isNotBlank(numberMin)
                    && StringUtils.isNotBlank(numberMax)) {
            return RandomUtils.nextInt(Integer.parseInt(numberMin.trim()),
                    Integer.parseInt(numberMax.trim()) + 1);
        }
        String[] params = paramStr.split(",");
        if (params.length == 1) {
            return RANDOM.nextInt(Integer.parseInt(params[0].trim()));
        }

        if (params.length == 2) {
            return RandomUtils.nextInt(Integer.parseInt(params[0].trim()), Integer.parseInt(params[1].trim()) + 1);
        }
        return RANDOM.nextInt();
    }

    /**
     * 生成随机float，@float(a,b)（a，b）表示数值范围
     * @param paramStr
     * @return
     */
    public static Float $float(String paramStr){
        Float aFloat = RANDOM.nextFloat();
        if (StringUtils.isBlank(paramStr)
                && StringUtils.isNotBlank(numberMin)
                    && StringUtils.isNotBlank(numberMax)) {
            aFloat = RandomUtils.nextFloat(Integer.parseInt(numberMin.trim()),
                    Integer.parseInt(numberMax.trim()) + 1);
        }else {
            String[] params = paramStr.split(",");
            if (params.length == 1) {
                aFloat = RandomUtils.nextFloat(Float.NaN,Float.parseFloat(params[0].trim()));
            }
            else if (params.length == 2) {
                aFloat = RandomUtils.nextFloat(Integer.parseInt(params[0].trim()), Integer.parseInt(params[1].trim()) + 1);
            }
        }
        return (float)(Math.round(aFloat*100))/100;
    }

    public static String $now(String paramStr){
        if (StringUtils.isBlank(paramStr)) {
            return new Date().toInstant().toString();
        }
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(paramStr));
    }

    public static Date $date(String paramStr) {
        return DateUtils.randomDate();
    }

    public static String $datetime(String paramStr){
        ZoneId zoneId = ZoneId.systemDefault();
        Date date = DateUtils.randomDate();
        if (StringUtils.isBlank(paramStr)) {
            return date.toInstant().toString();
        }
        LocalDateTime time = LocalDateTime.ofInstant(date.toInstant(),zoneId);
        return time.format(DateTimeFormatter.ofPattern(paramStr));
    }
}
