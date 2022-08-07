package com.wz.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wz.common.to.SkuHasStockVo;
import com.wz.common.to.mq.StockLockedTo;
import com.wz.common.utils.PageUtils;
import com.wz.gulimall.ware.entity.WareSkuEntity;
import com.wz.gulimall.ware.vo.WareLockVo;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author qj
 * @email emailofqj@163.com
 * @date 2022-04-29 13:15:26
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockVo> hasStock(List<Long> skuIds);

    Boolean lockStock(WareLockVo wareLockVo);

    List<Long> getWareIdsBySkuIdHasStock(Long skuId);

    void unLocked(StockLockedTo stockLockedTo);

    void unLocked(Long skuId, Long wareId, Integer skuNum);
}

