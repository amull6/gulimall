package com.wz.gulimall.ware.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FareVo {
    private MemberReceiveAddressVo addressVo;
    private BigDecimal fare;
}
