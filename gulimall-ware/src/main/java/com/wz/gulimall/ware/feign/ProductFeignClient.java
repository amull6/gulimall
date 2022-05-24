package com.wz.gulimall.ware.feign;

import com.wz.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author: 2014015130
 * @Date: 2022/5/24 13:50
 * @Description:
 */
@FeignClient("gulimall-product")
public interface ProductFeignClient {
    @RequestMapping("/product/skuinfo/info/{skuId}")
    public R info(@PathVariable("skuId") Long skuId);
}
