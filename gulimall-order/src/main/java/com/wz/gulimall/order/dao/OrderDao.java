package com.wz.gulimall.order.dao;

import com.wz.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author qj
 * @email emailofqj@163.com
 * @date 2022-04-29 13:09:40
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
