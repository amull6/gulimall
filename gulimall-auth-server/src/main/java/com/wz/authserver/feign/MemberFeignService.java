package com.wz.authserver.feign;

import com.wz.authserver.vo.UserRegisterVo;
import com.wz.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient
public interface MemberFeignService {

    @RequestMapping("/register")
    R register(@RequestBody UserRegisterVo userRegisterVo);

}
