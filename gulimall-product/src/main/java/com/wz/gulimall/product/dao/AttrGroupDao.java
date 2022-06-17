package com.wz.gulimall.product.dao;

import com.wz.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.wz.gulimall.product.entity.AttrGroupEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wz.gulimall.product.vo.SpuItemGroupAttrVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性分组
 *
 * @author qj
 * @email emailofqj@163.com
 * @date 2022-04-29 10:51:50
 */
@Mapper
public interface AttrGroupDao extends BaseMapper<AttrGroupEntity> {

    void removeRelations(@Param("attrAttrgroupRelationEntities") List<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntities);

    List<SpuItemGroupAttrVo> getSpuItemGroupAttrVo(@Param("spuId") Long spuId, @Param("catalogId") Long catalogId);
}
