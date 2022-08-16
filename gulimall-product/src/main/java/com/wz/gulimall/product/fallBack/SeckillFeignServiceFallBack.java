package com.wz.gulimall.product.fallBack;

import com.wz.common.exception.BizCodeEnum;
import com.wz.common.utils.R;
import com.wz.gulimall.product.feign.SeckillFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SeckillFeignServiceFallBack implements SeckillFeignService {
    @Override
    public R getSeckillBySkuId(Long skuId) {
        log.info("熔断方法调用...getSkuSeckilInfo");
        return R.error(BizCodeEnum.TOO_MANY_REQUEST_EXCEPTION.getCode(),BizCodeEnum.TOO_MANY_REQUEST_EXCEPTION.getMsg());
    }
}
