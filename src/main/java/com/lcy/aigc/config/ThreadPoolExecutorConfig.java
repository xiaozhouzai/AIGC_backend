package com.lcy.aigc.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolExecutorConfig {

    @Bean
    public ThreadPoolExecutor poolExecutor(){
        /*
        int corePoolSize 核心线程数
        int maximumPoolSize  最大线程数  极限情况下，任务再多也只不能超过最大线程数,考虑实际，考虑系统的瓶颈，比如ai最大接受四个线程
        long keepAliveTime  空闲线程存活时间
        TimeUnit unit 空闲线程存活时间单位
        BlockingQueue<Runnable> workQueue  工作队列（阻塞队列），存放线程执行的任务 一定要设置队列长度
        threadFactory（线程工厂）： 控制每个线程的创建，线程属性
        RejectedExecutionHandler handler  饱和策略 默认抛异常 什么时候采取什么措施，自定义策略
        资源隔离策略：比如重要的任务（会员） 会员定义一个队列，非会员定义一个队列 保证这两队列互不干扰
         */
        ThreadFactory threadFactory = new ThreadFactory() {
            int count = 1;
            @Override
            public Thread newThread(@NotNull Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("线程"+count);
                count++;
                return  thread;
            }
        };
        //自定义线程池
        ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(2,4,
                1000, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(4), threadFactory);
        return poolExecutor;
    }
}
