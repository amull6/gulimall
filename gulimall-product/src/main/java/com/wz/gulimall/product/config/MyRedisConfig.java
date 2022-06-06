package com.wz.gulimall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyRedisConfig {
    @Bean
    public RedissonClient redisson() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://124.221.206.12:6379");
        return Redisson.create(config);
    }
}
