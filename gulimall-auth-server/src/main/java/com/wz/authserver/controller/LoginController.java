package com.wz.authserver.controller;

import com.sun.javaws.jnl.IconDesc;
import com.wz.authserver.feign.ThirdPartFeignService;
import com.wz.authserver.vo.UserRegisterVo;
import com.wz.common.constant.AuthServerConstant;
import com.wz.common.exception.BizCodeEnum;
import com.wz.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Controller
@Slf4j
public class LoginController {
    @Autowired
    ThirdPartFeignService thirdPartFeignService;

    @Autowired
    RedisTemplate redisTemplate;

    //    @RequestMapping("/login.html")
//    public String login() {
//        return "login";
//    }
//
//    @RequestMapping("/reg.html")
//    public String reg() {
//        return "reg";
//    }
    @ResponseBody
    @RequestMapping("/sms/sendCode")
    public R sendCode(@RequestParam("phone") String phone) {
//        防刷
        String codeStr = (String) redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX+phone);
        if (codeStr != null) {
//        60秒内禁止再次发送

            if(System.currentTimeMillis()-Long.parseLong(codeStr.split("_")[1]) < 60*1000){
                return R.error(BizCodeEnum.VALIDE_CODE_EXCEPTION.getCode(), BizCodeEnum.VALIDE_CODE_EXCEPTION.getMsg());
            }
        }
        String code = String.valueOf((int)(Math.random()*9+1)*100000);
        redisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX+phone, code +"_"+System.currentTimeMillis() , 10, TimeUnit.MINUTES);
        try {
            return thirdPartFeignService.sendCode(phone, code);
        } catch (Exception e) {
            log.error("远程调用不知名错误");
        }
        return R.ok();
    }

    @RequestMapping("/regist")
    public String regist(@RequestBody UserRegisterVo userRegisterVo, BindResult result) {
        if(result.)

        return "regist";
    }

}
