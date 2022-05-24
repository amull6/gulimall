package com.wz.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wz.common.utils.PageUtils;
import com.wz.gulimall.ware.entity.PurchaseEntity;
import com.wz.gulimall.ware.vo.MergeVo;
import com.wz.gulimall.ware.vo.PurchaseDone;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author qj
 * @email emailofqj@163.com
 * @date 2022-04-29 13:15:26
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryUnreceivePage(Map<String, Object> params);

    void merge(MergeVo mergeVo);

    void receive(List<Long> ids);

    void done(PurchaseDone purchaseDone);
}

