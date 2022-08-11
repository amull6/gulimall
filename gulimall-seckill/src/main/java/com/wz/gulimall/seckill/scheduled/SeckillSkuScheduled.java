package com.wz.gulimall.seckill.scheduled;

import com.wz.gulimall.seckill.service.SeckillSkuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SeckillSkuScheduled {
    @Autowired
    SeckillSkuService seckillSkuService;

    @Scheduled(cron = "* * /3 * * ?")
    public void seckillSKuUp3Days(){
        seckillSkuService.seckillSKuUp3Days();
    }


}
