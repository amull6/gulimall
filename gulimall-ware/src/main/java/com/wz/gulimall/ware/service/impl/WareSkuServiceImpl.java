package com.wz.gulimall.ware.service.impl;

import com.wz.common.to.SkuHasStockVo;
import com.wz.common.utils.R;
import com.wz.common.exception.NoStockException;
import com.wz.gulimall.ware.feign.ProductFeignClient;
import com.wz.gulimall.ware.vo.OrderItemVo;
import com.wz.gulimall.ware.vo.WareLockVo;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wz.common.utils.PageUtils;
import com.wz.common.utils.Query;

import com.wz.gulimall.ware.dao.WareSkuDao;
import com.wz.gulimall.ware.entity.WareSkuEntity;
import com.wz.gulimall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    ProductFeignClient productFeignClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<WareSkuEntity>();
        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            queryWrapper.eq("ware_id", wareId);
        }

        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {
            queryWrapper.eq("sku_id", skuId);
        }
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        List<WareSkuEntity> wareSkuEntities = this.list(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (wareSkuEntities == null || wareSkuEntities.size() == 0) {
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStockLocked(0);
            wareSkuEntity.setStock(skuNum);
            try {
                R r = productFeignClient.info(skuId);
                Map<String, Object> map = (Map<String, Object>) r.get("skuInfo");
                if (r.getCode() == 200) {
                    wareSkuEntity.setSkuName((String) map.get("skuName"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.save(wareSkuEntity);
        } else {
            baseMapper.stock(skuId, wareId, skuNum);
        }
    }

    @Override
    public List<SkuHasStockVo> hasStock(List<Long> skuIds) {
        List<SkuHasStockVo> list = baseMapper.hasStock(skuIds);
        Map<Long, Boolean> map = list.stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock));

        List<SkuHasStockVo> list01 = skuIds.stream().map((obj) -> {
            if (!map.containsKey(obj)) {
                return new SkuHasStockVo(obj, false);
            } else {
                return new SkuHasStockVo(obj, map.get(obj));
            }
        }).collect(Collectors.toList());
        return list01;
    }

    @Override
    @Transactional
    public Boolean lockStock(WareLockVo wareLockVo) {
        List<OrderItemVo> orderItemVos = wareLockVo.getOrderItemVos();
        List<SkuWareHasStock> skuWareHasStocks = orderItemVos.stream().map((item) -> {
            SkuWareHasStock skuWareHasStock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            skuWareHasStock.setSkuId(skuId);
            skuWareHasStock.setNum(item.getCount());
            List<Long> wareIds = this.getWareIdsBySkuIdHasStock(skuId);
            skuWareHasStock.setWareIds(wareIds);
            return skuWareHasStock;
        }).collect(Collectors.toList());
//        锁定库存
        for (SkuWareHasStock skuWareHasStock : skuWareHasStocks) {
            boolean skuStockLocked = false;
            List<Long> wareIds = skuWareHasStock.getWareIds();
            if (wareIds == null || wareIds.size() < 0) {
                throw new NoStockException(skuWareHasStock.getSkuId());
            }
            for (Long wareId : wareIds) {
                Long count = this.baseMapper.lockSkuStock(skuWareHasStock.skuId, wareId, skuWareHasStock.getNum());
                if (count == 1) {
                    skuStockLocked = true;
                    break;
                } else {

                }
            }
            if (!skuStockLocked) {
                throw new NoStockException(skuWareHasStock.getSkuId());
            }
        }
        return true;
    }

    @Override
    public List<Long> getWareIdsBySkuIdHasStock(Long skuId) {
        return this.baseMapper.getWareIdsBySkuIdHasStock(skuId);
    }

    @Data
    private class SkuWareHasStock {
        private Long skuId;
        private Integer num;
        private List<Long> wareIds;
    }
}
