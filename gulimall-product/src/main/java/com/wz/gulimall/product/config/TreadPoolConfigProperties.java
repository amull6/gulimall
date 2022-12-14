package com.wz.gulimall.product.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "gulimall.thread")
@Component
@Data
public class TreadPoolConfigProperties {
    public Integer coreSize;
    public Integer maxSize;
    public Integer keepAliveTime;
}
