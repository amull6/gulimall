package com.wz.gulimall.testssoserver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;

@Controller
public class loginController {
    @Autowired
    RedisTemplate redisTemplate;

    @RequestMapping("/loginIndex")
    public String loginIndex() {
        return "login";
    }

    @RequestMapping("/login")
    public String toLogin(@RequestParam("username") String username, @RequestParam("password") String password, Model model, HttpSession httpSession) {
        if (username.equals("admin") && password.equals("admin")) {
            httpSession.setAttribute("loginUser", username);
            return "redirect:http://client01.com:8081/index";
        } else {
            model.addAttribute("msg", "用户名或密码错误");
            return "login";
        }
    }
}
