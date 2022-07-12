package com.wz.gulimall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wz.common.utils.HttpUtils;
import com.wz.gulimall.member.dao.MemberLevelDao;
import com.wz.gulimall.member.entity.MemberLevelEntity;
import com.wz.gulimall.member.exception.PhoneExistException;
import com.wz.gulimall.member.exception.UserNameExistException;
import com.wz.gulimall.member.vo.MemberLoginVo;
import com.wz.gulimall.member.vo.MemberRegisterVo;
import com.wz.gulimall.member.vo.SocialUser;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
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
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = encoder.encode(memberRegisterVo.getPassword());
        entity.setUsername(memberRegisterVo.getUserName());
        entity.setPassword(password);
        entity.setCreateTime(new Date());
        entity.setStatus(0);
        entity.setBirth(new Date());
        entity.setGender(1);
        this.baseMapper.insert(entity);
    }

    @Override
    public MemberEntity login(MemberLoginVo memberLoginVo) {
        MemberEntity memberEntity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("username", memberLoginVo.getLoginacct()));
        if (memberEntity == null) {
            return null;
        } else {
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            boolean b = bCryptPasswordEncoder.matches(memberLoginVo.getPassword(), memberEntity.getPassword());
            if (b) {
                return memberEntity;
            } else {
                return null;
            }

        }
    }

    @Override
    public MemberEntity login(SocialUser socialUser) throws Exception {
//        根据Uid查询有无MemberEntity
        MemberEntity memberEntity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("uid", socialUser.getUid()));
//        如果有
        if (memberEntity != null) {
            //        更新token等并返回
            MemberEntity newMemberEntity = new MemberEntity();
            newMemberEntity.setAccessToken(socialUser.getAccess_token());
            newMemberEntity.setExpiresIn(socialUser.getExpires_in());
            newMemberEntity.setId(memberEntity.getId());
            this.baseMapper.updateById(memberEntity);

            memberEntity.setAccessToken(socialUser.getAccess_token());
            memberEntity.setExpiresIn(socialUser.getExpires_in());
            return memberEntity;
        } else {
            //        如果没有
            //        注册新的memberEntity并返回
            MemberEntity newMemberEntity = new MemberEntity();
            newMemberEntity.setAccessToken(socialUser.getAccess_token());
            newMemberEntity.setExpiresIn(socialUser.getExpires_in());
            newMemberEntity.setUid(socialUser.getUid());
            newMemberEntity.setStatus(0);
            newMemberEntity.setCreateTime(new Date());
            newMemberEntity.setBirth(new Date());
            newMemberEntity.setLevelId(1L);
//            根据token信息调用微博API获取用户信息
            // 2. 没有查到当前社交用户对应的记录 我们就需要注册一个
            HashMap<String, String> map = new HashMap<>();
            map.put("access_token", socialUser.getAccess_token());
            map.put("uid", socialUser.getUid());
            try {
                HttpResponse httpResponse = HttpUtils.doGet("https://api.weibo.com", "/2/users/show.json", new HashMap<>(), map);
                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    String json = EntityUtils.toString(httpResponse.getEntity());
                    JSONObject jsonObject = JSON.parseObject(json);
                    newMemberEntity.setNickname(jsonObject.getString("name"));
                    newMemberEntity.setUsername(jsonObject.getString("name"));
                    newMemberEntity.setGender("m".equals(jsonObject.getString("gender")) ? 1 : 0);
                    memberEntity.setCity(jsonObject.getString("location"));
                    memberEntity.setJob("自媒体");
                    memberEntity.setEmail(jsonObject.getString("email"));
                }
            } catch (Exception e) {
                log.warn("社交登录时，调用微博远程API获取用户信息出错【尝试修复】");
            }
            this.baseMapper.insert(newMemberEntity);
            return newMemberEntity;
        }
    }

    private void checkUserName(String userName) {
        if (this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", userName)) > 0) {
            throw new UserNameExistException();
        }
    }

    private void checkPhone(String phone) {
        if (this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone)) > 0) {
            throw new PhoneExistException();
        }
    }

}