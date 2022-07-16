package com.wz.gulimall.cart.controller;

import com.wz.common.constant.CartConstant;
import com.wz.gulimall.cart.interceptor.CartInterceptor;
import com.wz.gulimall.cart.service.CartService;
import com.wz.gulimall.cart.vo.UserInfoTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Controller
public class CartController {
    @Autowired
    CartService cartService;

    @RequestMapping("/cart.html")
    public String cartListPage(HttpServletResponse response) {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        return "cartList";
    }

    @RequestMapping("/addToCart")
    public String addToCart(@RequestParam("count") int count, @RequestParam("skuId") Long skuId) {
        cartService.addToCart(count, skuId);
        return "cartList";
    }
}
