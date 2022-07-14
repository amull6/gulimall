package com.wz.gulimall.testssoserver.controller;

import com.sun.deploy.net.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.jws.WebParam;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

@Controller
public class loginController {
    @Autowired
    RedisTemplate redisTemplate;

    @RequestMapping("/loginUser")
    public String getLoginUser(@RequestParam(value = "sso_token") String token) {
        String userName = (String) redisTemplate.opsForValue().get(token);
        return userName;
    }

    @RequestMapping("/loginIndex")
    public String loginIndex(@RequestParam(value = "res_redirect", required = false) String resUrl, Model model, @CookieValue(value = "sso_token", required = false) String token) {
        if (!StringUtils.isEmpty(token)) {
            return "redirect:" + resUrl + "?token=" + token;
        }
        model.addAttribute("res_redirect", resUrl);
        return "login";
    }

    @RequestMapping("/login")
    public String toLogin(@RequestParam("username") String username, @RequestParam("password") String password, Model model, @RequestParam(value = "res_redirect", required = false) String resUrl, HttpServletResponse response) {
        if (username.equals("admin") && password.equals("admin")) {
            String uuid = UUID.randomUUID().toString().replace("-", "");

            redisTemplate.opsForValue().set(uuid, username);
            Cookie cookie = new Cookie("sso_token", uuid);
            response.addCookie(cookie);
            return "redirect:" + resUrl + "?token=" + uuid;
        } else {
            model.addAttribute("msg", "用户名或密码错误");
            return "login";
        }
    }
}
