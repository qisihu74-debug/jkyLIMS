package com.stu.manage.demo.filter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author gjl
 * @version V1.0
 * @Package com.stu.manage.demo.filter
 * @desc 用来跳过认证的PassToken
 * @date 2021/8/27 14:47
 * @Copyright © 河南交科院
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PassToken {
    boolean required() default true;
}
