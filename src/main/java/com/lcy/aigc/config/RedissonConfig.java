package com.lcy.aigc.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties("spring.redis")
@Data
public class RedissonConfig {
    private Integer database;
    private String port;
    private String host;

    /**
     * 配置一个redissionClient,根据yml配置动态生成
     * @return
     */
    @Bean
    public RedissonClient getRedissonClient(){
        Config config = new Config();
        config.useSingleServer()
                .setDatabase(database)
                .setAddress("redis://" + host + ":" + port);
        return Redisson.create(config);
    }
}
