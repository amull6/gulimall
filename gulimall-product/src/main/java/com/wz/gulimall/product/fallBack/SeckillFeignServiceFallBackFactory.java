package com.wz.gulimall.product.fallBack;

import com.wz.common.exception.BizCodeEnum;
import com.wz.common.utils.R;
import com.wz.gulimall.product.feign.SeckillFeignService;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SeckillFeignServiceFallBackFactory implements FallbackFactory<SeckillFeignService> {
    @Override
    public SeckillFeignService create(Throwable throwable) {
        return new SeckillFeignService() {
            @Override
            public R getSeckillBySkuId(Long skuId) {
                log.info("熔断方法调用FallbackFactory...getSkuSeckilInfo");
                R r = R.error(BizCodeEnum.TOO_MANY_REQUEST_EXCEPTION.getCode(), BizCodeEnum.TOO_MANY_REQUEST_EXCEPTION.getMsg());
                return r;
            }
        };
    }
}
