package com.wz.gulimall.seckill.scheduled;

import com.wz.gulimall.seckill.service.SeckillSkuService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class SeckillSkuScheduled {
    @Autowired
    SeckillSkuService seckillSkuService;

    @Autowired
    RedissonClient redisson;

    private final String upload_lock = "seckill:upload:lock";

    @Scheduled(cron = "0 * * * * ?")
    public void seckillSKuUp3Days() {
        RLock rlock = redisson.getLock(upload_lock);
        rlock.lock(10, TimeUnit.SECONDS);
        try {
            seckillSkuService.seckillSKuUp3Days();
        } finally {
            rlock.unlock();
        }
    }


}
