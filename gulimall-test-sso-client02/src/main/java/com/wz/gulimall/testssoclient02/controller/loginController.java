package com.wz.gulimall.testssoclient02.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class loginController {
    @RequestMapping("/login")
    @ResponseBody
    public String toLogin() {
        return "login";
    }
    @RequestMapping("/index")
    public String index() {
        return "index";
    }
}
