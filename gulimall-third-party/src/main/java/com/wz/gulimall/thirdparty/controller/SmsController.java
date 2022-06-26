package com.wz.gulimall.thirdparty.controller;

import com.wz.common.utils.R;
import com.wz.gulimall.thirdparty.component.SmsComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sms")
public class SmsController {
    @Autowired
    SmsComponent smsComponent;

    @RequestMapping("/sendCode")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code) throws Exception {
        if (!"fail".equals(smsComponent.sendSmsCode(phone, code).split("_")[0])) {
            return R.ok();
        }
        return R.error();
    }
}
