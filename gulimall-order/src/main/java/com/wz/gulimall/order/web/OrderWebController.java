package com.wz.gulimall.order.web;

import com.wz.gulimall.order.entity.OrderEntity;
import com.wz.gulimall.order.exception.NoStockException;
import com.wz.gulimall.order.service.OrderService;
import com.wz.gulimall.order.vo.OrderConfirmVo;
import com.wz.gulimall.order.vo.OrderSubmitVo;
import com.wz.gulimall.order.vo.SubmitOrderResponseVo;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.AccessType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Controller
public class OrderWebController {
    @Autowired
    OrderService orderService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @RequestMapping("/test/createOrder")
    @ResponseBody
    public String createOrder() {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(UUID.randomUUID().toString());
//        String var1, String var2, Object var3, MessagePostProcessor var4, CorrelationData var5
        rabbitTemplate.convertAndSend("order.event.exchange","order.create.order",orderEntity);
        return "ok";
    }

    @RequestMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo orderSubmitVo, Model model, RedirectAttributes redirectAttributes) {
        try {
            SubmitOrderResponseVo responseVo = orderService.submitOrder(orderSubmitVo);
            if (responseVo != null && responseVo.getCode() == 0) {
                model.addAttribute("order", responseVo);
                return "pay";
            } else {
                String msg = "下单失败;";
                switch (responseVo.getCode()){
                    case 1 :  msg +="订单信息过期，请刷新在提交"; break;
                    case 2 :  msg +="订单价格发生变化，请确认后再提交"; break;
                }
                redirectAttributes.addFlashAttribute("msg", msg);
                return "redirect:http://order.gulimall.com/toTrade";
            }
        } catch (Exception e) {
            if (e instanceof NoStockException) {
                String message = e.getMessage();
                redirectAttributes.addFlashAttribute("msg", message);
            }
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
