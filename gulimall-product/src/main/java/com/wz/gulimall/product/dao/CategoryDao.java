package com.wz.gulimall.product.dao;

import com.wz.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author qj
 * @email emailofqj@163.com
 * @date 2022-04-29 10:51:50
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
