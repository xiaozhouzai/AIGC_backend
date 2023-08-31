package com.lcy.aigc.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class RedissionLimiterManagerTest {
    @Resource
    private RedisLimiterManager limiterManager;

    @Test
    void doRedisLimiter() throws InterruptedException {
        String userId = "11";

        for (int i = 0; i < 2; i++) {
            limiterManager.doRedisLimiter(userId);
            //循环五次，就是执行五次，如果limiter.tryAcquire(5);设置为5，执行一次要获取五个令牌，一秒内最大获取5个令牌
            //所以一秒内只能执行成功一次，其他四次都会失败
            System.out.println("成功");
        }

        Thread.sleep(1000);
        //limiter.tryAcquire(2)设置为2，最大5，一次获取2，一秒最大获取5，一秒最多执行两次，5/2=2.5
        //休眠一秒，再执行就是新的一秒了，新的一秒还可以最多执行两次,多了就限流，抛出错误
        for (int i = 0; i < 3; i++) {
            limiterManager.doRedisLimiter(userId);
            //循环五次，就是执行五次，如果limiter.tryAcquire(5);设置为5，执行一次要获取五个令牌，一秒内最大获取5个令牌
            //所以一秒内只能执行成功一次，其他四次都会失败
            System.out.println("成功");
        }

    }
}