package com.wz.gulimall.ware.feign;

import com.wz.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("gulimall-order")
public interface OrderFeignService {
    @RequestMapping("/order/order/status/{orderSn}")
    R getOrderStatus(@PathVariable String orderSn);
}
