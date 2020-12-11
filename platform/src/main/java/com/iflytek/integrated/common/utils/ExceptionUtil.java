package com.iflytek.integrated.common.utils;

/**
 * @author czzhan
 */
public class ExceptionUtil {
    public ExceptionUtil() {
    }

    public static String dealException(Exception e) {
        return e instanceof IllegalArgumentException ? e.getMessage() : "FAILURE";
    }
}
