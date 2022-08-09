package com.wz.gulimall.member.feign;

import com.wz.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient("gulimall-order")
public interface OrderFeignService {

    @RequestMapping("/order/order/listOrderWithItem")
    R listOrderWithItem(@RequestBody Map<String, Object> params) ;

}
