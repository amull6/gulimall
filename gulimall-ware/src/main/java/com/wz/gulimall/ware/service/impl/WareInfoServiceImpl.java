package com.wz.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.wz.common.utils.R;
import com.wz.gulimall.ware.feign.MemberFeignService;
import com.wz.gulimall.ware.vo.MemberReceiveAddressVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wz.common.utils.PageUtils;
import com.wz.common.utils.Query;

import com.wz.gulimall.ware.dao.WareInfoDao;
import com.wz.gulimall.ware.entity.WareInfoEntity;
import com.wz.gulimall.ware.service.WareInfoService;
import org.springframework.util.StringUtils;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Autowired
    MemberFeignService memberFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String key = (String) params.get("key");
        QueryWrapper<WareInfoEntity> queryWrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and((obj) -> {
                obj.eq("id", key).or().eq("name", key);
            });
        }
        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public BigDecimal getFare(Long addrId) {
//        根据addrId远程获取地址信息
        R r = memberFeignService.info(addrId);
        MemberReceiveAddressVo memberReceiveAddressVo = r.getData("memberReceiveAddress", new TypeReference<MemberReceiveAddressVo>() {
        });
        String fare = memberReceiveAddressVo.getPhone().substring(memberReceiveAddressVo.getPhone().length() - 1);
        return new BigDecimal(fare);
    }

}