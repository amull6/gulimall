package com.wz.gulimall.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.fastjson.JSON;
import com.wz.common.exception.BizCodeEnum;
import com.wz.common.utils.R;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
public class SentinelGatewayConfig {
    public SentinelGatewayConfig() {
        GatewayCallbackManager.setBlockHandler(new BlockRequestHandler() {
            @Override
            public Mono<ServerResponse> handleRequest(ServerWebExchange serverWebExchange, Throwable throwable) {
                R r = R.error(BizCodeEnum.TOO_MANY_REQUEST_EXCEPTION.getCode(), BizCodeEnum.TOO_MANY_REQUEST_EXCEPTION.getMsg());
                return ServerResponse.ok().body(Mono.just(JSON.toJSONString(r)), String.class);
            }
        });
    }
}
