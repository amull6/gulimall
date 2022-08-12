package com.wz.gulimall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.wz.common.utils.R;
import com.wz.gulimall.seckill.feign.CouponFeignService;
import com.wz.gulimall.seckill.feign.ProductFeignService;
import com.wz.gulimall.seckill.service.SeckillSkuService;
import com.wz.gulimall.seckill.to.SeckillSkuTo;
import com.wz.gulimall.seckill.vo.SeckillSessionVo;
import com.wz.gulimall.seckill.vo.SeckillSkuRelationVo;
import com.wz.gulimall.seckill.vo.SkuInfoVo;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SeckillSkuServiceImpl implements SeckillSkuService {
    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    RedissonClient redisson;

    public static final String SESSION_CACHE_PREFIX = "seckill:session:";
    public static final String SKUS_CACHE_PREFIX = "seckill:skus";
    //    商品随机码
    public static final String SKUSTOCK_SEMAPHONE = "seckill:stock:";


    @Override
    public List<SeckillSkuTo> getCurrentSeckillSkus() {
//        获取当前时间（long）
        Long now = new Date().getTime();
//        获取所有的keys
        Set<String> keys = stringRedisTemplate.keys(SESSION_CACHE_PREFIX+"*");
//        遍历keys 判断是否在指定时间
        if (keys != null && keys.size() > 0) {
            for (String key : keys) {
                String redisTime = key.replace(SESSION_CACHE_PREFIX, "");
                String[] redisTimeArray = redisTime.split("-");
                Long startTime = Long.valueOf(redisTimeArray[0]);
                Long endTime = Long.valueOf(redisTimeArray[1]);
                if (startTime <= now && endTime >= now) {
                    //        取出key下面的所有session List skuIds
                    List<String> sessionSkuIds = stringRedisTemplate.opsForList().range(key, -100, 100);
                    //        批量获取当前session的所有商品信息
                    BoundHashOperations<String, String, String> boundHashOperations = stringRedisTemplate.boundHashOps(SKUS_CACHE_PREFIX);
                    if (sessionSkuIds != null && sessionSkuIds.size() > 0) {
                        List<String> redisTosJson = boundHashOperations.multiGet(sessionSkuIds);
                        //        list遍历
                        return redisTosJson.stream().map((item) -> {
                            SeckillSkuTo seckillSkuTo = new SeckillSkuTo();
                            seckillSkuTo = JSON.parseObject(item, SeckillSkuTo.class);
                            return seckillSkuTo;
                        }).collect(Collectors.toList());
                    }
                }
            }
        }
        return null;
    }

    /*
     *秒杀商品上架，上传redis
     */
    @Override
    public void seckillSKuUp3Days() {
//        TODO 远程获取3天内的秒杀任务信息
        R r = couponFeignService.getSeckillSessionWithSkuIn3Days();
        if (r.getCode() == 0) {
            List<SeckillSessionVo> seckillSessionVos = r.getData(new TypeReference<List<SeckillSessionVo>>() {
            });
//        保存session
            saveSeckillSession(seckillSessionVos);
//        保存sku
            saveSeckillSkuRelation(seckillSessionVos);
        }
    }

    private void saveSeckillSession(List<SeckillSessionVo> seckillSessionVos) {
        if (seckillSessionVos != null && seckillSessionVos.size() > 0) {
            seckillSessionVos.stream().forEach((session) -> {
                long startTime = session.getStartTime().getTime();
                long endTime = session.getEndTime().getTime();
                String key = SESSION_CACHE_PREFIX + startTime + "-" + endTime;
                boolean hasKey = stringRedisTemplate.hasKey(key);
                if (!hasKey) {
//                    取出所有skuId
                    List<String> skuIds = session.getSeckillSkuRelationEntityList().stream().map((item) -> session.getId() + "_" + item.getSkuId().toString()).collect(Collectors.toList());
                    stringRedisTemplate.opsForList().leftPushAll(key, skuIds);
                }
            });
        }
    }

    private void saveSeckillSkuRelation(List<SeckillSessionVo> seckillSessionVos) {
        seckillSessionVos.stream().forEach((session) -> {
//            保存成hash
            BoundHashOperations<String, Object, Object> boundHashOperations = stringRedisTemplate.boundHashOps(SKUS_CACHE_PREFIX);
            List<SeckillSkuRelationVo> skus = session.getSeckillSkuRelationEntityList();
            skus.stream().forEach((sku) -> {
//                幂等性判断
                if (!boundHashOperations.hasKey(sku.getPromotionSessionId() + "_" + sku.getSkuId())) {
                    SeckillSkuTo seckillSkuTo = new SeckillSkuTo();
                    // 远程获取sku信息
                    R r = productFeignService.info(sku.getSkuId());
                    if (r.getCode() == 0) {
                        //生成randomkey
                        String randomCode = UUID.randomUUID().toString().replace("-", "");
                        seckillSkuTo.setRandomCode(randomCode);
                        SkuInfoVo skuInfoVo = r.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                        });
                        seckillSkuTo.setSkuInfoVo(skuInfoVo);
//                        复制secSku信息
                        BeanUtils.copyProperties(sku, seckillSkuTo);
//                        设置起始截止时间
                        seckillSkuTo.setStartTime(session.getStartTime().getTime());
                        seckillSkuTo.setEndTime(session.getEndTime().getTime());
                        String json = JSON.toJSONString(seckillSkuTo);
                        boundHashOperations.put(sku.getPromotionSessionId() + "_" + sku.getSkuId(), json);
                        //                        使用库存作为信号量限流
                        RSemaphore rSemaphore = redisson.getSemaphore(SKUSTOCK_SEMAPHONE + randomCode);
                        rSemaphore.trySetPermits(sku.getSeckillCount().intValue());
                    }
                }
            });
        });
    }
}