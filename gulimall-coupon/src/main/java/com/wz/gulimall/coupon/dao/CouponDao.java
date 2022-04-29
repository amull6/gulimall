package com.wz.gulimall.coupon.dao;

import com.wz.gulimall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author qj
 * @email emailofqj@163.com
 * @date 2022-04-29 12:51:56
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
