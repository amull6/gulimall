package com.wz.gulimall.seckill.feign;

import com.wz.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("gulimall-coupon")
public interface CouponFeignService {
    @RequestMapping("/coupon/seckillsession/lates3DaySession")
    R getSeckillSessionWithSkuIn3Days();
}
