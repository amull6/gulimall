package com.wz.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author: 2014015130
 * @Date: 2022/5/24 10:55
 * @Description:
 */
@Data
public class MergeVo {
    Long purchaseId;
    List<Long> items;
}
