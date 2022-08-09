package com.wz.gulimall.order.web;


import com.alipay.api.AlipayApiException;
import com.wz.gulimall.order.config.AlipayTemplate;
import com.wz.gulimall.order.service.OrderService;
import com.wz.gulimall.order.vo.PayVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PayWebController {
    @Autowired
    OrderService orderService;

    @Autowired
    AlipayTemplate alipayTemplate;

    @RequestMapping(value = "/payOrder",produces = "text/html")
    public String payOrder(@RequestParam("orderSn") String orderSn) throws AlipayApiException {
        PayVo payVo = orderService.handlePayVo(orderSn);
        return alipayTemplate.pay(payVo);
    }
}
