package com.iflytek.integrated.platform.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @description: 自定义注解：防止表单重复提交
 * @author:
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AvoidRepeatCommit {
    /**
     * 指定时间内不可重复提交,单位毫秒,设置500毫秒
     *
     * @return long
     */
    long timeout() default 500;
}
