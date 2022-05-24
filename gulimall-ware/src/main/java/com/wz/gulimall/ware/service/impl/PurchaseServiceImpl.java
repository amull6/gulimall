package com.wz.gulimall.ware.service.impl;

import com.sun.xml.bind.v2.TODO;
import com.wz.common.constant.WareConstant;
import com.wz.common.constant.WareConstant.PurchaseEntityStatus;
import com.wz.gulimall.ware.entity.PurchaseDetailEntity;
import com.wz.gulimall.ware.service.PurchaseDetailService;
import com.wz.gulimall.ware.service.WareSkuService;
import com.wz.gulimall.ware.vo.MergeVo;
import com.wz.gulimall.ware.vo.PurchaseDone;
import com.wz.gulimall.ware.vo.PurchaseDoneItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wz.common.utils.PageUtils;
import com.wz.common.utils.Query;

import com.wz.gulimall.ware.dao.PurchaseDao;
import com.wz.gulimall.ware.entity.PurchaseEntity;
import com.wz.gulimall.ware.service.PurchaseService;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {
    @Autowired
    PurchaseDetailService purchaseDetaiService;
    @Autowired
    WareSkuService wareSkuService;

    @Override

    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryUnreceivePage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>().eq("status", 0).or().eq("status", 1)
        );

        return new PageUtils(page);
    }

    @Override
    public void merge(MergeVo mergeVo) {
//        查找采购单
        Long purchaseId = mergeVo.getPurchaseId();
//        没有采购单新建
        if (purchaseId == null) {
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            purchaseEntity.setStatus(PurchaseEntityStatus.CREATED.getCode());
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }
//        有采购单用查询出来的采购单
//        遍历采购项ID
//        TODO 确认订单状态是 0 1
        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> purchaseDetailEntities = mergeVo.getItems().stream().map((item) -> {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            purchaseDetailEntity.setPurchaseId(finalPurchaseId);
            purchaseDetailEntity.setId(item);
            purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatus.ASSIGNED.getCode());
            return purchaseDetailEntity;
        }).collect(Collectors.toList());
//        修改采购项上采购单ID/更新时间
        purchaseDetaiService.updateBatchById(purchaseDetailEntities);

        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }

    @Override
    public void receive(List<Long> ids) {
//        判断是否领取
        List<PurchaseEntity> purchaseEntities = ids.stream().map((obj) -> {
            PurchaseEntity purchaseEntity = this.getById(obj);
            return purchaseEntity;
        }).filter((obj) -> {
            if (obj.getStatus() == PurchaseEntityStatus.CREATED.getCode() || obj.getStatus() == PurchaseEntityStatus.ASSIGNED.getCode()) {
                return true;
            } else {
                return false;
            }
        }).map((obj) -> {
            obj.setStatus(PurchaseEntityStatus.RECEIVE.getCode());
            obj.setUpdateTime(new Date());
            return obj;
        }).collect(Collectors.toList());
        this.updateBatchById(purchaseEntities);
//        修改采购项状态
        for (PurchaseEntity item : purchaseEntities) {

            List<PurchaseDetailEntity> purchaseDetailEntities = purchaseDetaiService.ListByPurchaseEntityId(item.getId());
            List<PurchaseDetailEntity> purchaseDetailEntities1 = purchaseDetailEntities.stream().map((obj) -> {
                PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
                purchaseDetailEntity.setId(obj.getId());
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatus.RECEIVE.getCode());
                return purchaseDetailEntity;
            }).collect(Collectors.toList());
            purchaseDetaiService.updateBatchById(purchaseDetailEntities1);
        }
    }

    @Override
    public void done(PurchaseDone purchaseDone) {

        boolean flag = false;
        List<PurchaseDetailEntity> purchaseDetailEntities = new ArrayList<>();
        for (PurchaseDoneItemVo purchaseDoneItemVo : purchaseDone.getItems()) {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            if (purchaseDoneItemVo.getStatus() == WareConstant.PurchaseDetailStatus.FINISH.getCode()) {
                purchaseDetailEntity.setStatus(purchaseDoneItemVo.getStatus());
//                修改库存
                PurchaseDetailEntity purchaseDetailEntity1 = purchaseDetaiService.getById(purchaseDoneItemVo.getItemId());
                wareSkuService.addStock(purchaseDetailEntity1.getSkuId(), purchaseDetailEntity1.getWareId(), purchaseDetailEntity1.getSkuNum());
            } else {
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatus.HASERROR.getCode());
                flag = true;
            }
            purchaseDetailEntity.setId(purchaseDoneItemVo.getItemId());
            purchaseDetailEntities.add(purchaseDetailEntity);
        }
        purchaseDetaiService.updateBatchById(purchaseDetailEntities);
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseDone.getId());
        purchaseEntity.setStatus(flag?WareConstant.PurchaseEntityStatus.HASERROR.getCode():WareConstant.PurchaseEntityStatus.FINISH.getCode());
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }

}