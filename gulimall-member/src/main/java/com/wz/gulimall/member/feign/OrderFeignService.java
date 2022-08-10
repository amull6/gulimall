package com.wz.gulimall.member.feign;

import com.wz.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient("gulimall-order")
public interface OrderFeignService {
    @PostMapping("/order/order/listOrderWithItem")
    R listOrderWithItem(@RequestBody Map<String, Object> params);

}
