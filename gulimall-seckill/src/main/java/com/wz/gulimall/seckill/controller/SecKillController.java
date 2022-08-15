package com.wz.gulimall.seckill.controller;

import com.wz.common.utils.R;
import com.wz.gulimall.seckill.service.SeckillSkuService;
import com.wz.gulimall.seckill.to.SeckillSkuTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/seckill")
public class SecKillController {
    @Autowired
    SeckillSkuService seckillSkuService;

    @RequestMapping("/getCurrentSeckillSkus")
    public R getCurrentSeckillSkus() {
        List<SeckillSkuTo> seckillSkuToList = seckillSkuService.getCurrentSeckillSkus();
        return R.ok().setData(seckillSkuToList);
    }

    //    根据skuId获取当前时间秒杀信息
    @RequestMapping("/sku/secKill/{skuId}")
    public R getSeckillBySkuId(@PathVariable("skuId") Long skuId) {
        SeckillSkuTo seckillSkuTo = seckillSkuService.getSeckillBySkuId(skuId);
        return R.ok().setData(seckillSkuTo);
    }


}
