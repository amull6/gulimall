package com.wz.gulimall.order.vo;

import com.wz.gulimall.order.entity.OrderEntity;
import lombok.Data;

@Data
public class SubmitOrderResponseVo {
    private Integer code;
    private OrderEntity orderEntity;
}
