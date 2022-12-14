package com.wz.authserver.feign;

import com.wz.authserver.vo.SocialUser;
import com.wz.authserver.vo.UserLoginVo;
import com.wz.authserver.vo.UserRegisterVo;
import com.wz.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("gulimall-member")
public interface MemberFeignService {

    @RequestMapping("/member/member/register")
    R register(@RequestBody UserRegisterVo userRegisterVo);

    @RequestMapping("/member/member/login")
    R login(@RequestBody UserLoginVo userLoginVo);

    @RequestMapping("/member/member/oauth2login")
    R login(@RequestBody SocialUser socialUser);


}
