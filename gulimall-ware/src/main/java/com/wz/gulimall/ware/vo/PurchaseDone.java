package com.wz.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author: 2014015130
 * @Date: 2022/5/24 13:01
 * @Description:
 */
@Data
public class PurchaseDone {
    Long id;
    List<PurchaseDoneItemVo> items;
}
