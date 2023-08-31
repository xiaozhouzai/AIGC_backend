package com.lcy.aigc.manager;

import com.lcy.aigc.common.ErrorCode;
import com.lcy.aigc.exception.BusinessException;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
@Service
public class RedisLimiterManager {

    @Resource
    private RedissonClient redissonClient;

    /**
     * redission限流方法
     * @param key 限流器的指定名key ， 区分不同的限流器，比如不同的用户 id 应该分别统计
     */
    public void doRedisLimiter(String key){


//        RateType.OVERALL：速率限制的类型。RateType.OVERALL表示整体速率限制，即对整个系统或资源进行速率限制。

//        5：速率限制的数量。表示在指定的时间间隔内允许通过的请求数量。

//        1：速率限制的时间间隔。表示速率限制的时间间隔，单位由RateIntervalUnit参数指定。

//        RateIntervalUnit.SECONDS：速率限制的时间单位。表示速率限制的时间间隔的单位，可以是秒、毫秒、微秒等。
        //创建一个限流器，名为key
        RRateLimiter limiter= redissonClient.getRateLimiter(key);

        //一秒最多请求5次
        //第一个参数: 5，每个时间单位允许最大访问几次，第二个参数 1，表示时间单位，第三个参数时间单位的类型:时分秒
        limiter.trySetRate(RateType.OVERALL,5,1, RateIntervalUnit.SECONDS);

        //每当来了一个操作后请求几个令牌
        //这里可以判断用户如果是普通，一次操作只能获取一个令牌
        //若果是会员，一次可以获得五个令牌
        boolean canOption = limiter.tryAcquire(2);

        //TODO 理解：5这个参数可以看成是一次操作消耗五个令牌，上面设置的一秒内最多获取五个令牌，说明一秒内只能执行一次操作
        //TODO 如果改为1，就是一次操作获取一个令牌，一秒内可以操作5次
        if (!canOption){
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST);
        }
    }
}
