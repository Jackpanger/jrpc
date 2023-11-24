package com.jackpang.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * description: JrpcApi
 * date: 11/23/23 2:39 AM
 * author: jinhao_pang
 * version: 1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface JrpcApi {
    // group name
    String group() default "default";
}
