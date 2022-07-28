package com.wz.gulimall.order.feign;

import com.wz.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("gulimall-ware")
public interface WareFeignService {
    /**
     * 查询sku是否有库存
     */
    @PostMapping(value = "/ware/waresku/hasStock")
    R hasStock(@RequestBody List<Long> skuIds);
}
