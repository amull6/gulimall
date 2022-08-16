package com.wz.gulimall.product.config;

import com.wz.gulimall.product.fallBack.SeckillFeignServiceFallBackFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DefaultFeignConfig {
    @Bean
    public SeckillFeignServiceFallBackFactory seckillFeignServiceFallBackFactory() {
        return new SeckillFeignServiceFallBackFactory();
    }
}
