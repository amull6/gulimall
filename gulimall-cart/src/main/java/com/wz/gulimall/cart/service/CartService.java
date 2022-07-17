package com.wz.gulimall.cart.service;

import com.wz.gulimall.cart.vo.CastItem;

import java.util.concurrent.ExecutionException;

public interface CartService {
    CastItem addToCart(int count, Long skuId) throws ExecutionException, InterruptedException;

    CastItem getCartItemBySkuId(Long skuId);
}
