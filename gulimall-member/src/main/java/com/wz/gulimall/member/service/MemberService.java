package com.wz.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wz.common.utils.PageUtils;
import com.wz.gulimall.member.entity.MemberEntity;
import com.wz.gulimall.member.vo.MemberLoginVo;
import com.wz.gulimall.member.vo.MemberRegisterVo;

import java.util.Map;

/**
 * 会员
 *
 * @author qj
 * @email emailofqj@163.com
 * @date 2022-04-29 13:03:32
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void register(MemberRegisterVo memberRegisterVo);

    MemberEntity login(MemberLoginVo memberLoginVo);
}

