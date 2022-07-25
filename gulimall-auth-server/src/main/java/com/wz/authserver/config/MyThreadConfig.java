package com.wz.authserver.config;

import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Configuration
public class MyThreadConfig {
    public ExecutorService executorService(TreadPoolConfigProperties treadPoolConfigProperties) {
        return new ThreadPoolExecutor(treadPoolConfigProperties.coreSize, treadPoolConfigProperties.maxSize, treadPoolConfigProperties.keepAliveTime, TimeUnit.SECONDS,new LinkedBlockingDeque<>(100000), Executors.defaultThreadFactory(),new ThreadPoolExecutor.AbortPolicy());
    }
}
