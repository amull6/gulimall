package com.wz.gulimall.coupon.service.impl;

import com.wz.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.wz.gulimall.coupon.service.SeckillSkuRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wz.common.utils.PageUtils;
import com.wz.common.utils.Query;

import com.wz.gulimall.coupon.dao.SeckillSessionDao;
import com.wz.gulimall.coupon.entity.SeckillSessionEntity;
import com.wz.gulimall.coupon.service.SeckillSessionService;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {
    @Autowired
    SeckillSkuRelationService seckillSkuRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    /*
     *获取三天内秒杀任务信息
     */
    @Override
    public List<SeckillSessionEntity> getSeckillSessionWithSkuIn3Days() {
//        获取session信息
        List<SeckillSessionEntity> seckillSessionEntities = this.baseMapper.selectList(new QueryWrapper<SeckillSessionEntity>().between("start_time", getStartTime(), getEndTime()));
//        获取sku关联信息
        if (seckillSessionEntities != null && seckillSessionEntities.size() != 0) {
            return seckillSessionEntities.stream().map((item) -> {
                List<SeckillSkuRelationEntity> seckillSkuRelationEntityList = seckillSkuRelationService.list(new QueryWrapper<SeckillSkuRelationEntity>().eq("promotion_session_id", item.getId()));
                item.setSeckillSkuRelationEntityList(seckillSkuRelationEntityList);
                return item;
            }).collect(Collectors.toList());
        }
        return null;
    }

    public String getStartTime() {
        LocalDateTime start = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        return start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public String getEndTime() {
        LocalDate acquired = LocalDate.now().plusDays(2);
        LocalDateTime end = LocalDateTime.of(acquired, LocalTime.MAX);
        return end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

}