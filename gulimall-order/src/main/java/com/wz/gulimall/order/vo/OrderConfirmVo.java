package com.wz.gulimall.order.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class OrderConfirmVo {
    @Getter @Setter
    List<MemberAddressVo> address; //收货地址列表
    @Getter @Setter
    List<OrderItemVo> items; //购物项
    @Getter @Setter
    Integer integration;
    BigDecimal total; //订单总额
    //防重复提交令牌
    @Getter @Setter
    String orderToken;
    Integer count;

    @Getter @Setter
    Map<Long,Boolean> stocks;

    public Integer getCount(){
        Integer count = 0;
        if(items!=null&&items.size()>0){
            for (OrderItemVo orderItemVo : items) {
                count+=orderItemVo.getCount();
            }
        }
        return count;
    }


    public BigDecimal getTotal() {
        BigDecimal total = BigDecimal.ZERO;
        if(items!=null&&items.size()>0){
            for (OrderItemVo orderItemVo : items) {
                total = total.add(orderItemVo.getPrice().multiply(new BigDecimal(orderItemVo.getCount().toString())));
            }
        }
        return total;
    }

    BigDecimal payPrice; //应付价格

    public BigDecimal getPayPrice() {
        return getTotal();
    }
}
