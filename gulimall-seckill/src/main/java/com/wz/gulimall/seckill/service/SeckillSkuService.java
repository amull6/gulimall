package com.wz.gulimall.seckill.service;


import com.wz.gulimall.seckill.to.SeckillSkuTo;

import java.util.List;

public interface SeckillSkuService {
    void seckillSKuUp3Days();

    List<SeckillSkuTo> getCurrentSeckillSkus();

    SeckillSkuTo getSeckillBySkuId(Long skuId);
}
