package com.wz.gulimall.seckill.controller;

import com.wz.common.utils.R;
import com.wz.gulimall.seckill.service.SeckillSkuService;
import com.wz.gulimall.seckill.to.SeckillSkuTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/seckill")
@Controller
public class SecKillController {
    @Autowired
    SeckillSkuService seckillSkuService;

    @RequestMapping("/getCurrentSeckillSkus")
    @ResponseBody
    public R getCurrentSeckillSkus() {
        List<SeckillSkuTo> seckillSkuToList = seckillSkuService.getCurrentSeckillSkus();
        return R.ok().setData(seckillSkuToList);
    }

    //    根据skuId获取当前时间秒杀信息
    @RequestMapping("/sku/secKill/{skuId}")
    @ResponseBody
    public R getSeckillBySkuId(@PathVariable("skuId") Long skuId) {
        SeckillSkuTo seckillSkuTo = seckillSkuService.getSeckillBySkuId(skuId);
        return R.ok().setData(seckillSkuTo);
    }

    @RequestMapping("/order")
    public String seckill(@RequestParam("killId") String killId,
                          @RequestParam("key") String key,
                          @RequestParam("num") Integer num, Model model) {
        String orderSn = seckillSkuService.seckill(killId, key, num);
        model.addAttribute("orderSn", orderSn);
        return "success";
    }
}
