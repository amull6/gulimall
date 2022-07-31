package com.wz.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

@Data
public class WareLockVo {
    private String orderSn;
    private List<OrderItemVo> orderItemVos;
}
