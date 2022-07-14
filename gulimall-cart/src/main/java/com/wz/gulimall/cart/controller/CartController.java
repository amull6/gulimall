package com.wz.gulimall.cart.controller;

import com.wz.common.constant.CartConstant;
import com.wz.gulimall.cart.interceptor.CartInterceptor;
import com.wz.gulimall.cart.vo.UserInfoTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Controller
public class CartController {
    @RequestMapping("/cart.html")
    public String cartListPage(HttpServletResponse response) {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        return "cartList";
    }
}
