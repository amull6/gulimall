package com.wz.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.wz.common.utils.R;
import com.wz.gulimall.cart.feign.ProductFeignService;
import com.wz.gulimall.cart.interceptor.CartInterceptor;
import com.wz.gulimall.cart.service.CartService;
import com.wz.gulimall.cart.vo.Cast;
import com.wz.gulimall.cart.vo.CastItem;
import com.wz.gulimall.cart.vo.SkuInfoVo;
import com.wz.gulimall.cart.vo.UserInfoTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    ExecutorService executorService;

    private static final String PREFIX_CART = "gulimall:cart:";

    @Override
    public CastItem addToCart(int count, Long skuId) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> redisOps = gerRedisOps();
        String castItemRedis = (String) redisOps.get(String.valueOf(skuId));
        if (StringUtils.isEmpty(castItemRedis)) {
            CastItem castItem = new CastItem();
            CompletableFuture getSkuInfoTask = CompletableFuture.runAsync(() -> {
                //        组装CastItem
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
            }, executorService);

            CompletableFuture getSkuSaleAttrValues = CompletableFuture.runAsync(() -> {
                //        查询SkuAttr
                List<String> attrList = productFeignService.listBySkuId(skuId);
                castItem.setSkuAttr(attrList);
            }, executorService);
            CompletableFuture.allOf(getSkuInfoTask, getSkuSaleAttrValues).get();
//        保存到redis
            String castItemJson = JSON.toJSONString(castItem);
            redisOps.put(String.valueOf(skuId), castItemJson);
            return castItem;
        } else {
            CastItem castItem = JSONObject.parseObject(castItemRedis, new TypeReference<CastItem>() {
            });
            castItem.setCount(castItem.getCount() + count);
            String castItemJson = JSON.toJSONString(castItem);
            redisOps.put(String.valueOf(skuId), castItemJson);
            return castItem;
        }
    }

    @Override
    public CastItem getCartItemBySkuId(Long skuId) {
        BoundHashOperations<String, Object, Object> operations = gerRedisOps();
        String castItemStr = (String) operations.get(skuId.toString());
        return JSONObject.parseObject(castItemStr, CastItem.class);
    }

    @Override
    public Cast getCast() throws ExecutionException, InterruptedException {
        Cast cast = new Cast();
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
//        区分登录用户，临时用户
        if (StringUtils.isEmpty(userInfoTo.getUserId())) {
            String tempKey = PREFIX_CART + userInfoTo.getUserKey();
//            临时用户
            List<CastItem> castItems = getCastItems(tempKey);
            cast.setCastItems(castItems);
        } else {
//            登录用户
            String tempKey = PREFIX_CART + userInfoTo.getUserKey();
            List<CastItem> castItems = getCastItems(tempKey);
            if (castItems.size() > 0) {
                for (CastItem castItem : castItems) {
                    addToCart(castItem.getCount(), castItem.getSkuId());
                }
                //            清空临时购物车
                redisTemplate.delete(tempKey);
            }
            String key = PREFIX_CART + userInfoTo.getUserId();
            List<CastItem> items = getCastItems(key);
            cast.setCastItems(items);
        }
//        封装Casta
        return cast;
    }

    @Override
    public void checkItem(String skuId, Integer check) {
        BoundHashOperations<String, Object, Object> ops = this.gerRedisOps();
        CastItem castItem = getCartItemBySkuId(Long.valueOf(skuId));
        castItem.setCheck(check == 1 ? true : false);
        ops.put(skuId.toString(), JSON.toJSONString(castItem));
    }

    @Override
    public void changeCountItem(Long skuId, Integer count) {
        BoundHashOperations<String, Object, Object> boundHashOperations = this.gerRedisOps();
        String itemStr = (String) boundHashOperations.get(skuId);
        CastItem castItem = JSONObject.parseObject(itemStr, CastItem.class);
        castItem.setCount(count);
        boundHashOperations.put(skuId, JSON.toJSONString(castItem));
    }

    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> boundHashOperations = gerRedisOps();
        boundHashOperations.delete(skuId.toString());
    }

    @Override
    public List<CastItem> getCastItems() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo != null) {
            String key = PREFIX_CART + userInfoTo.getUserId();
            List<CastItem> castItems = getCastItems(key);
//            1更新为最新价格
            //            2只返回选中的Item
            return castItems.stream().filter(CastItem::isCheck).map((obj) -> {
//                    去商品服务查询最新的价格
                obj.setPrice(productFeignService.getPrice(obj.getSkuId()));
                return obj;
            }).collect(Collectors.toList());
        }else{
            return null;
        }
    }

    private BoundHashOperations<String, Object, Object> getOpsByKey(String key) {
        return redisTemplate.boundHashOps(key);
    }

    private List<CastItem> getCastItems(String key) {
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(key);
        List<Object> list = operations.values();
        List<CastItem> castItems = new ArrayList<>();
        for (Object obj : list) {
            CastItem castItem = JSON.parseObject((String) obj, CastItem.class);
            castItems.add(castItem);
        }
        return castItems;
    }

    private BoundHashOperations<String, Object, Object> gerRedisOps() {
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
