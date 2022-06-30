package com.wz.gulimall.member.controller;

import java.util.Arrays;
import java.util.Map;

import com.wz.gulimall.member.feign.CouponFeignSerice;
import com.wz.gulimall.member.vo.MemberRegisterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wz.gulimall.member.entity.MemberEntity;
import com.wz.gulimall.member.service.MemberService;
import com.wz.common.utils.PageUtils;
import com.wz.common.utils.R;


/**
 * 会员
 *
 * @author qj
 * @email emailofqj@163.com
 * @date 2022-04-29 13:03:32
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    CouponFeignSerice couponFeignSerice;

    @RequestMapping("/register")
    public R register(@RequestBody MemberRegisterVo memberRegisterVo) {
        memberService.register(memberRegisterVo);
        return R.ok();
    }

    @RequestMapping("/coupons")
    public R test() {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("张三");
        R c = couponFeignSerice.memberCoupon();
        return R.ok().put("member", memberEntity).put("coupon", c);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {

        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody MemberEntity member) {
        memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody MemberEntity member) {
        memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
