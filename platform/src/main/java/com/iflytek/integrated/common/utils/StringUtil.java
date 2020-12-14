package com.iflytek.integrated.common.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
* String 工具类
* @author weihe9
* @date 2020/12/12 19:10
*/
@Component
public class StringUtil {

    @Autowired
    private RedisUtil redisUtil;


    /**
     * 编号生成规则
     * @param head  开始头部
     * @param digit 位数
     * @return
     */
    public String recountNew(String head, Integer digit) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        String str = format.format(new Date());
        int maxCount = 1;
        String key = head.toLowerCase() + str;
        Object maxNmber = redisUtil.get(key);
        if (null != maxNmber) {
            maxCount = Integer.parseInt(maxNmber.toString()) + 1;
        } else {
            //表示当天第一个
        }
        if (null == digit) { //默认赋值4位
            digit = 4;
        }
        redisUtil.set(key, maxCount, 60 * 60 * 36L); //默认保存36小时
        /**
         * 获取编号
         */
        NumberFormat nf = NumberFormat.getInstance();
        //设置是否使用分组
        nf.setGroupingUsed(false);
        //设置最大整数位数
        nf.setMaximumIntegerDigits(digit);
        //设置最小整数位数
        nf.setMinimumIntegerDigits(digit);

        return head + str + nf.format(maxCount);
    }




}
