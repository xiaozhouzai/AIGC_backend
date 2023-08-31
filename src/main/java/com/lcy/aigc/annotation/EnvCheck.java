package com.lcy.aigc.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnvCheck {
    /**
     * 必须是某个环境
     * @return
     */
    String mustEnv() default "";
}
