package com.wz.gulimall.testssoclient01.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Controller
public class loginController {
    @RequestMapping("/login")
    @ResponseBody
    public String toLogin() {
        return "login";
    }

    @RequestMapping("/index")
    public String index(Model model, HttpSession httpSession) {
//        判断是否登录
        if (httpSession.getAttribute("loginUser") == null) {
//            重定向ossServer登录页面
            return "redirect:http://ossserver.com:8080/loginIndex";
        } else {
            List<String> users = new ArrayList<>();
            users.add("小明");
            users.add("小王");
            users.add("小宋");
            model.addAttribute("users", users);
            return "index";
        }
    }
}
