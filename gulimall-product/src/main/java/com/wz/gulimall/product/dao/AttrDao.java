package com.wz.gulimall.product.dao;

import com.wz.gulimall.product.entity.AttrEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品属性
 * 
 * @author qj
 * @email emailofqj@163.com
 * @date 2022-04-29 10:51:50
 */
@Mapper
public interface AttrDao extends BaseMapper<AttrEntity> {

    List<Long> searchAttIds(@Param("attrIds") List<Long> attrIds);
}
