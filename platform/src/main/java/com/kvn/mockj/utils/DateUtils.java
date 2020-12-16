package com.kvn.mockj.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author czzhan
 */
public class DateUtils {

        /**
         * 生成随机时间
         * @return
         */
        public static Date randomDate() {
            try {
                Calendar c = Calendar.getInstance();
                c.setTime(new Date());

                // 构造开始日期
                c.add(Calendar.YEAR, - 5);
                Date start = c.getTime();
                // 构造结束日期
                c.add(Calendar.YEAR, + 10);
                Date end = c.getTime();
                // getTime()表示返回自 1970 年 1 月 1 日 00:00:00 GMT 以来此 Date 对象表示的毫秒数。
                if (start.getTime() >= end.getTime()) {
                    return null;
                }
                long date = random(start.getTime(), end.getTime());
                return new Date(date);
            } catch (Exception e) {
            }
            return null;
        }

        public static long random(long begin, long end) {
            long rtn = begin + (long) (Math.random() * (end - begin));
            // 如果返回的是开始时间和结束时间，则递归调用本函数查找随机值
            if (rtn == begin || rtn == end) {
                return random(begin, end);
            }
            return rtn;
        }
}
