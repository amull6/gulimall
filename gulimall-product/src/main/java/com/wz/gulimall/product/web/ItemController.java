package com.wz.gulimall.product.web;

import com.wz.gulimall.product.service.SkuInfoService;
import com.wz.gulimall.product.vo.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ItemController {
    @Autowired
    SkuInfoService skuInfoService;
    @RequestMapping("/{skuId}.html")
    public String skuItem(@PathVariable Long skuId, Model model) {
        SkuItemVo skuItemVo = skuInfoService.item(skuId);
        model.addAttribute("item", skuItemVo);
        return "item";
    }
}
