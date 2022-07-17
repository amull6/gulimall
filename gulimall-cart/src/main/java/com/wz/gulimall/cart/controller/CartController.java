package com.wz.gulimall.cart.controller;

import com.wz.gulimall.cart.interceptor.CartInterceptor;
import com.wz.gulimall.cart.service.CartService;
import com.wz.gulimall.cart.vo.CastItem;
import com.wz.gulimall.cart.vo.UserInfoTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.ExecutionException;

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
    public String addToCart(@RequestParam("count") int count, @RequestParam("skuId") Long skuId, RedirectAttributes redirectAttributes) throws ExecutionException, InterruptedException {
        cartService.addToCart(count, skuId);
        redirectAttributes.addAttribute("skuId", skuId);
        return "redirect:http://cart.gulimall.com/success";
    }

    @RequestMapping("/success")
    public String addToCartSuccess(@RequestParam("skuId") Long skuId, Model model) {
        CastItem castItem = cartService.getCartItemBySkuId(skuId);
        model.addAttribute("item", castItem);
        return "success";
    }
}
