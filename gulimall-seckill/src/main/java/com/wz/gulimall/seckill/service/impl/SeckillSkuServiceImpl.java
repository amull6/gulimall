package com.wz.gulimall.seckill.service.impl;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.wz.common.to.mq.SecKillTo;
import com.wz.common.utils.R;
import com.wz.common.vo.MemberResVo;
import com.wz.gulimall.seckill.feign.CouponFeignService;
import com.wz.gulimall.seckill.feign.ProductFeignService;
import com.wz.gulimall.seckill.interceptor.LoginUserInterceptor;
import com.wz.gulimall.seckill.service.SeckillSkuService;
import com.wz.gulimall.seckill.to.SeckillSkuTo;
import com.wz.gulimall.seckill.vo.SeckillSessionVo;
import com.wz.gulimall.seckill.vo.SeckillSkuRelationVo;
import com.wz.gulimall.seckill.vo.SkuInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
@Slf4j
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

    @Autowired
    RabbitTemplate rabbitTemplate;

    public static final String SESSION_CACHE_PREFIX = "seckill:session:";
    public static final String SKUS_CACHE_PREFIX = "seckill:skus";
    //    商品随机码
    public static final String SKUSTOCK_SEMAPHONE = "seckill:stock:";

    @Override
    public String seckill(String killId, String key, Integer num) {
        MemberResVo memberResVo = LoginUserInterceptor.loginUser.get();
//        查找SeckillSkuTo
        BoundHashOperations<String, String, String> boundHashOperations = stringRedisTemplate.boundHashOps(SKUS_CACHE_PREFIX);
        String json = boundHashOperations.get(killId);
        SeckillSkuTo seckillSkuTo = JSON.parseObject(json, SeckillSkuTo.class);
//        2、校验合法性
//        2.1、校验时间的合法性
        Long now = new Date().getTime();
        Long start = seckillSkuTo.getStartTime();
        Long end = seckillSkuTo.getEndTime();
        Long exp = end - start;
        if (now >= seckillSkuTo.getStartTime() && now <= seckillSkuTo.getEndTime()) {
//        2.2、校验随机码 和 商品id 是否正确
            String skuId = seckillSkuTo.getPromotionSessionId() + "_" + seckillSkuTo.getSkuId();
            if (key.equals(seckillSkuTo.getRandomCode()) && skuId.equals(killId)) {
//        2.3、验证购物车数量是否合理
                if (num <= seckillSkuTo.getSeckillLimit().intValue()) {
                    //        2.4、验证这个人是否购买过。幂等性：如果只要秒杀成功，就去占位。 userId_SessionId_skuId
                    String rediskey = memberResVo.getId() + skuId;
                    Boolean isFirstTime = stringRedisTemplate.opsForValue().setIfAbsent(rediskey, num.toString(), exp, TimeUnit.MILLISECONDS);
                    if (isFirstTime) {
                        RSemaphore semaphore = redisson.getSemaphore(SKUSTOCK_SEMAPHONE + key);
                        try {
                            boolean tryAcquire = semaphore.tryAcquire(num, 100, TimeUnit.MILLISECONDS);
                            SecKillTo secKillTo = new SecKillTo();
                            secKillTo.setOrderSn(IdWorker.getTimeId());
                            secKillTo.setMemberId(memberResVo.getId());
                            secKillTo.setSeckillPrice(seckillSkuTo.getSeckillPrice());
                            secKillTo.setNum(num);
                            secKillTo.setPromotionSessionId(seckillSkuTo.getPromotionSessionId());
                            secKillTo.setSkuId(seckillSkuTo.getSkuId());
                            rabbitTemplate.convertAndSend("order.event.exchange", "order.seckill.order", secKillTo);
                            return secKillTo.getOrderSn();
                        } catch (InterruptedException e) {
                            return null;
                        }
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public List<SeckillSkuTo> handlerException(BlockException e) {
        log.error(e.getMessage());
        return null;
    }

    @Override
    @SentinelResource(value = "seckillSkusByRes",blockHandler = "handlerException")
    public List<SeckillSkuTo> getCurrentSeckillSkus() {
//        获取当前时间（long）
        Long now = new Date().getTime();
//        获取所有的keys
        Set<String> keys = stringRedisTemplate.keys(SESSION_CACHE_PREFIX + "*");
//        遍历keys 判断是否在指定时间
        try (Entry entry = SphU.entry("seckillSkus")){
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
        } catch (BlockException e) {
            log.error("资源被限流{}", e.getMessage());
        }
        return null;
    }

    @Override
    public SeckillSkuTo getSeckillBySkuId(Long skuId) {
        BoundHashOperations<String, String, String> boundHashOperations = stringRedisTemplate.boundHashOps(SKUS_CACHE_PREFIX);
        Set<String> keys = boundHashOperations.keys();
        if (keys != null && keys.size() > 0) {
            for (String key : keys) {
                String pattern = "\\d_" + skuId;
                if (Pattern.matches(pattern, key)) {
                    String json = boundHashOperations.get(key);
                    SeckillSkuTo seckillSkuTo = JSON.parseObject(json, SeckillSkuTo.class);
//                    判断秒杀时间
                    Long now = new Date().getTime();
                    Long startTime = seckillSkuTo.getStartTime();
                    Long endTime = seckillSkuTo.getEndTime();
                    if (now >= startTime && now <= endTime) {
                        // 在秒杀活动时
                    } else {
                        // 不在秒杀活动时不应该传递随机码
                        seckillSkuTo.setRandomCode("");
                    }
                    return seckillSkuTo;
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
