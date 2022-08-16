package com.wz.gulimall.order.config;

import com.alibaba.csp.sentinel.adapter.servlet.callback.UrlBlockHandler;
import com.alibaba.csp.sentinel.adapter.servlet.callback.WebCallbackManager;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.wz.common.exception.BizCodeEnum;
import com.wz.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
@Slf4j
public class MySentinelConfig {
    public MySentinelConfig() {
        WebCallbackManager.setUrlBlockHandler(
                new UrlBlockHandler() {
                    @Override
                    public void blocked(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, BlockException e) throws IOException {
                        R r = R.error(BizCodeEnum.TOO_MANY_REQUEST_EXCEPTION.getCode(), BizCodeEnum.TOO_MANY_REQUEST_EXCEPTION.getMsg());
                        httpServletResponse.setContentType("application/json");
                        httpServletResponse.setCharacterEncoding("utf-8");
                        httpServletResponse.getWriter().print(r);
                    }
                }
        );
    }
}
