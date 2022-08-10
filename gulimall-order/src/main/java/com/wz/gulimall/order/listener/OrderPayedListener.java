package com.wz.gulimall.order.listener;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderPayedListener {

    @RequestMapping(value = "/payed/notify")
    public String payedNotify() {
        return "success";
    }
}
