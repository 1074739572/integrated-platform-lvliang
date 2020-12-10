package com.iflytek.integrated.common;

public class ExceptionUtil {
    public ExceptionUtil() {
    }

    public static String dealException(Exception e) {
        return e instanceof IllegalArgumentException ? e.getMessage() : "FAILURE";
    }
}
