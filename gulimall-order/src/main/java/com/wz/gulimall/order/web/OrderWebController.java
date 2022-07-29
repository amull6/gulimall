package com.wz.gulimall.order.web;

import com.wz.gulimall.order.service.OrderService;
import com.wz.gulimall.order.vo.OrderConfirmVo;
import com.wz.gulimall.order.vo.OrderSubmitVo;
import com.wz.gulimall.order.vo.SubmitOrderResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.AccessType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.concurrent.ExecutionException;

@Controller
public class OrderWebController {
    @Autowired
    OrderService orderService;

    @RequestMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo orderSubmitVo, Model model) {
        SubmitOrderResponseVo responseVo = orderService.submitOrder(orderSubmitVo);
        if (responseVo != null && responseVo.getCode() == 0) {
            model.addAttribute("order", responseVo.getOrderEntity());
            return "pay";
        } else {
            return "redirect:http://order.gulimall.com/toTrade";
        }
    }

    @RequestMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {
        OrderConfirmVo orderConfirmVo = orderService.confirmOrder();
        model.addAttribute("orderConfirm", orderConfirmVo);
        return "confirm";
    }
}
