package com.wz.gulimall.cart.config;

import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Configuration
public class MyThreadConfig {
    @Bean
    public ExecutorService executorService(TreadPoolConfigProperties treadPoolConfigProperties) {
        return new ThreadPoolExecutor(treadPoolConfigProperties.coreSize,treadPoolConfigProperties.maxSize,treadPoolConfigProperties.keepAliveTime, TimeUnit.SECONDS,new LinkedBlockingQueue<>(100000), Executors.defaultThreadFactory(),new ThreadPoolExecutor.AbortPolicy());
    }
}
