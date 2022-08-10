package com.wz.gulimall.member.web;

import com.alibaba.fastjson.TypeReference;
import com.wz.common.utils.PageUtils;
import com.wz.common.utils.R;
import com.wz.gulimall.member.feign.OrderFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@Controller
public class MemberWebController {
    @Autowired
    OrderFeignService orderFeignService;

    @RequestMapping("/memberOrder.html")
    public String memberOrderPage(@RequestParam(value = "pageNum", defaultValue = "1") String pageNum, Model model) {
        Map<String, Object> params = new HashMap<>();
        params.put("pageNum", pageNum);
        R r = orderFeignService.listOrderWithItem(params);
        model.addAttribute("orders", r);
        return "orderList";
    }
}