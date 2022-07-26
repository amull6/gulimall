package com.wz.gulimall.order.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Configuration
public class GuliFeignConfig {
    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor() {
        RequestInterceptor requestInterceptor = requestTemplate -> {
            ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest httpServletRequest = servletRequestAttributes.getRequest();
            if (httpServletRequest != null) {
                requestTemplate.header("cookie", httpServletRequest.getHeader("cookie"));
            }
        };
        return requestInterceptor;
    }
}
