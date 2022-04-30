package com.wz.gulimall.member.feign;

import com.wz.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author: 2014015130
 * @Date: 2022/4/29 21:38
 * @Description:
 */
@FeignClient("gulimall-coupon")
public interface CouponFeignSerice {
    @RequestMapping("coupon/coupon/member/list")
    public R memberCoupon();
}