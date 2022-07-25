package com.wz.gulimall.cart.controller;

import com.wz.gulimall.cart.interceptor.CartInterceptor;
import com.wz.gulimall.cart.service.CartService;
import com.wz.gulimall.cart.vo.Cast;
import com.wz.gulimall.cart.vo.CastItem;
import com.wz.gulimall.cart.vo.UserInfoTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.jws.WebParam;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.ExecutionException;

@Controller
public class CartController {
    @Autowired
    CartService cartService;

    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId) {
        cartService.deleteItem(skuId);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    @RequestMapping("/countItem")
    public String countItem(@RequestParam("skuId") Long skuId, @RequestParam("count") Integer count) {
        cartService.changeCountItem(skuId, count);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    @RequestMapping("/checkItem")
    public String checkItem(@RequestParam("skuId") String skuId, @RequestParam("check") Integer check) {
        cartService.checkItem(skuId, check);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    @RequestMapping("/cart.html")
    public String cartListPage(Model model) throws ExecutionException, InterruptedException {

        Cast cast = cartService.getCast();
        model.addAttribute("cast", cast);
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
