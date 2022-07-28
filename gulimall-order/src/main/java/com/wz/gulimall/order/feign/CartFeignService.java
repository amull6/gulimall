package com.wz.gulimall.order.feign;

import com.wz.gulimall.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@FeignClient("gulimall-cart")
public interface CartFeignService {
    @RequestMapping("/getCastItems")
    @ResponseBody
    List<OrderItemVo> getCastItem();
}
