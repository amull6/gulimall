package com.wz.gulimall.testssoclient02.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Controller
public class loginController {
    @Autowired
    RedisTemplate redisTemplate;
    @RequestMapping("/login")
    @ResponseBody
    public String toLogin() {
        return "login";
    }

    @RequestMapping("/index")
    public String index(Model model, @RequestParam(value = "token",required = false) String token,HttpSession session) {
//        判断是否登录
        if (!StringUtils.isEmpty(token)) {
            String loginUser = (String) redisTemplate.opsForValue().get(token);
            session.setAttribute("loginUser", loginUser);
        }
        Object loginUser = session.getAttribute("loginUser");
        if (loginUser == null) {
//            重定向ossServer登录页面
            return "redirect:http://ossserver.com:8080/loginIndex?res_redirect=http://client02.com:8082/index";
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
