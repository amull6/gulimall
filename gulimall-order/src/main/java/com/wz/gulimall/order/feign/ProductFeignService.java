package com.wz.gulimall.order.feign;

import com.wz.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("gulimall-product")
public interface ProductFeignService {
    @RequestMapping("/product/spuinfo/skuId/{id}")
    R getSpuInfoBySkuId(@PathVariable Long id);
}
