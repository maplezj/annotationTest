package com.sbys.zhaojian.httpan;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Created by zhaojian on 2018/3/29.
 */

@Target(METHOD)
@Retention(CLASS)
public @interface SupportOffline
{
}
