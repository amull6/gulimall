package com.wz.gulimall.product.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ItemController {
    @RequestMapping("/{skuId}.html")
    public String skuItem(@PathVariable String skuId) {
        return "item";
    }
}
