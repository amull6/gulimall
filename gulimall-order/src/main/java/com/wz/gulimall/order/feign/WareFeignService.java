package com.wz.gulimall.order.feign;

import com.wz.common.utils.R;
import com.wz.gulimall.order.vo.WareLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("gulimall-ware")
public interface WareFeignService {
    /**
     * 查询sku是否有库存
     */
    @PostMapping(value = "/ware/waresku/hasStock")
    R hasStock(@RequestBody List<Long> skuIds);

    @RequestMapping("/ware/wareinfo/fare")
    R getFare(@RequestParam("addrId") Long addrId);

    @RequestMapping("/ware/waresku/lock/order")
    R lockOrder(@RequestBody WareLockVo wareLockVo);
}
