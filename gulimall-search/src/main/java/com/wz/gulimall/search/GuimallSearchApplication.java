package com.wz.gulimall.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = "com.wz.gulimall",exclude = DataSourceAutoConfiguration.class)
@EnableDiscoveryClient
public class GuimallSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(GuimallSearchApplication.class, args);
    }

}
