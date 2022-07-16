package com.wz.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.wz.common.utils.R;
import com.wz.gulimall.cart.feign.ProductFeignService;
import com.wz.gulimall.cart.interceptor.CartInterceptor;
import com.wz.gulimall.cart.service.CartService;
import com.wz.gulimall.cart.vo.CastItem;
import com.wz.gulimall.cart.vo.SkuInfoVo;
import com.wz.gulimall.cart.vo.UserInfoTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    RedisTemplate redisTemplate;

    private static final String PREFIX_CART = "gulimall:cart:";

    @Override
    public void addToCart(int count, Long skuId) {
        BoundHashOperations<String,Object,Object> redisOps = gerRedisOps();
//        组装CastItem
        CastItem castItem = new CastItem();
//        查询Sku信息加入CastItem
        R r = productFeignService.info(skuId);
        SkuInfoVo skuInfoVo = r.getData("skuInfo", new TypeReference<SkuInfoVo>() {
        });
        castItem.setSkuId(skuId);
        castItem.setCheck(true);
        castItem.setImage(skuInfoVo.getSkuDefaultImg());
        castItem.setTitle(skuInfoVo.getSkuTitle());
        castItem.setPrice(skuInfoVo.getPrice());
        castItem.setCount(count);
//        查询SkuAttr
        List<String> attrList = productFeignService.listBySkuId(skuId);
        castItem.setSkuAttr(attrList);
//        保存到redis
        String castItemJson = JSON.toJSONString(castItem);
        redisOps.put(skuId, castItemJson);
    }

    private BoundHashOperations<String,Object,Object> gerRedisOps() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
//        判断是临时用户还是登录用户
        //        生成保存到redis的key
        String cartKey = "";
        Long userId = userInfoTo.getUserId();
        if (userId == null) {
//            临时
            cartKey = PREFIX_CART + userInfoTo.getUserKey();
        } else {
            cartKey = PREFIX_CART + userInfoTo.getUserId().toString();
        }
        return redisTemplate.boundHashOps(cartKey);
    }
}
