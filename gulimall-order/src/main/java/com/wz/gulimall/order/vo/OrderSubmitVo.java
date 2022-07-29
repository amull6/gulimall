package com.wz.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderSubmitVo {
    private Long addrId;
    private Integer payType;
    //无需提交要购买的商品，去购物车再获取一遍
    //优惠、发票
    private String orderToken;
    private BigDecimal payPrice;
    private String remark;
    //用户相关的信息，直接去session中取出即可
}
