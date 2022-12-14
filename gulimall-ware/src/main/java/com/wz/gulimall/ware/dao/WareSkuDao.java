package com.wz.gulimall.ware.dao;

import com.wz.common.to.SkuHasStockVo;
import com.wz.gulimall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * εεεΊε­
 *
 * @author qj
 * @email emailofqj@163.com
 * @date 2022-04-29 13:15:26
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    void stock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum") Integer skuNum);

    List<SkuHasStockVo> hasStock(@Param("skuIds") List<Long> skuIds);

    List<Long> getWareIdsBySkuIdHasStock(Long skuId);

    Long lockSkuStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("num") Integer num);

    void unLocked(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("num") Integer skuNum);
}
