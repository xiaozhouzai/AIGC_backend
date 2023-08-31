package com.lcy.aigc.aop;

import com.lcy.aigc.annotation.EnvCheck;
import lombok.Data;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 权限校验 AOP
 *
 */
@Aspect
@Component
@ConfigurationProperties("spring.profiles")
@Data
public class EnvInterceptor {

    /**
     * 环境
     */
    private String active;

    /**
     * 执行拦截
     *
     * @param joinPoint
     * @param envCheck
     * @return
     */
    @Around("@within(envCheck)")
    public Object checkAuth(ProceedingJoinPoint joinPoint, EnvCheck envCheck) throws Throwable {
        // 获取被注解的类
//        Class<?> targetClass = joinPoint.getTarget().getClass();
        String env = envCheck.mustEnv();
        // 判断是否满足某个条件，例如检查是否有权限
        if (!env.equals(active)) {
            return null;
        }
        return joinPoint.proceed();
    }
}
