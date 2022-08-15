package com.wz.gulimall.product.feign;

import com.wz.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("gulimall-seckill")
public interface SeckillFeignService {
    @RequestMapping("/seckill/sku/secKill/{skuId}")
    R getSeckillBySkuId(@PathVariable("skuId") Long skuId);
}
