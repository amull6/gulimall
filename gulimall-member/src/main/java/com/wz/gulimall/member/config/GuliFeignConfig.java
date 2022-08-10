package com.wz.gulimall.member.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Configuration
public class GuliFeignConfig {
    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor() {

        RequestInterceptor requestInterceptor = new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
                ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (servletRequestAttributes != null) {
                    HttpServletRequest httpServletRequest = servletRequestAttributes.getRequest();
                    if (httpServletRequest != null) {
                        requestTemplate.header("cookie", httpServletRequest.getHeader("cookie"));
                    }
                }
            }
        };
        return requestInterceptor;
    }
}
