package com.wz.gulimall.product.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;
import java.util.logging.Handler;

@Configuration
public class MyTreadConfig {
    @Bean
    public ExecutorService executorService(TreadPoolConfigProperties treadPoolConfigProperties) {
        return new ThreadPoolExecutor(treadPoolConfigProperties.coreSize, treadPoolConfigProperties.maxSize, treadPoolConfigProperties.keepAliveTime, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(100000), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
    }

}
