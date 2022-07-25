package com.wz.gulimall.order.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HelloController {
    @RequestMapping("{page}.html")
    public String index(@PathVariable String page){
        return page;
    }
}
