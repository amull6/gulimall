package com.wz.gulimall.member.service.impl;

import com.wz.gulimall.member.dao.MemberLevelDao;
import com.wz.gulimall.member.entity.MemberLevelEntity;
import com.wz.gulimall.member.exception.PhoneExistException;
import com.wz.gulimall.member.exception.UserNameExistException;
import com.wz.gulimall.member.vo.MemberRegisterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wz.common.utils.PageUtils;
import com.wz.common.utils.Query;

import com.wz.gulimall.member.dao.MemberDao;
import com.wz.gulimall.member.entity.MemberEntity;
import com.wz.gulimall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void register(MemberRegisterVo memberRegisterVo) {
//        设置等级
        MemberEntity entity = new MemberEntity();
        MemberLevelEntity memberLevelEntity = memberLevelDao.getDefaultLevel();
        entity.setLevelId(memberLevelEntity.getId());
//        判断有无相同账户名 手机号账户
        // 检查手机号 用户名是否唯一
        checkPhone(memberRegisterVo.getPhone());
        checkUserName(memberRegisterVo.getUserName());
//        密码加密存储


    }

    private void checkUserName(String userName) {
        if (this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("user_name", userName)) > 0) {
            throw new UserNameExistException();
        }
    }

    private void checkPhone(String phone) {
        if (this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone)) > 0) {
            throw new PhoneExistException();
        }
    }

}