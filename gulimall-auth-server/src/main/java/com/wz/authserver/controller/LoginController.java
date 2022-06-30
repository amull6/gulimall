package com.wz.authserver.controller;

import com.wz.authserver.feign.MemberFeignService;
import com.wz.authserver.feign.ThirdPartFeignService;
import com.wz.authserver.vo.UserRegisterVo;
import com.wz.common.constant.AuthServerConstant;
import com.wz.common.exception.BizCodeEnum;
import com.wz.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
@Slf4j
public class LoginController {
    @Autowired
    ThirdPartFeignService thirdPartFeignService;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    MemberFeignService memberFeignService;

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
        String codeStr = (String) redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (codeStr != null) {
//        60秒内禁止再次发送

            if (System.currentTimeMillis() - Long.parseLong(codeStr.split("_")[1]) < 60 * 1000) {
                return R.error(BizCodeEnum.VALIDE_CODE_EXCEPTION.getCode(), BizCodeEnum.VALIDE_CODE_EXCEPTION.getMsg());
            }
        }
        String code = String.valueOf((int) (Math.random() * 9 + 1) * 100000);
        redisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone, code + "_" + System.currentTimeMillis(), 10, TimeUnit.MINUTES);
        try {
            return thirdPartFeignService.sendCode(phone, code);
        } catch (Exception e) {
            log.error("远程调用不知名错误");
        }
        return R.ok();
    }

    @RequestMapping("/regist")
    public String regist(@Valid UserRegisterVo userRegisterVo, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
//        数据校验
        if (bindingResult.hasErrors()) {
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            Map<String, String> errorMap = fieldErrors.stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            redirectAttributes.addFlashAttribute("error", errorMap);
            return "redirect:http://auth.gulimall.com/reg.html";
        }
//        验证Code
        Map<String, String> errorMap = new HashMap<>();
        String codeStr = (String) redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + userRegisterVo.getPhone());
        if (!StringUtils.isEmpty(codeStr)) {
//            对比
            String[] codeArray = codeStr.split("_");
            if (codeArray[0].equals(userRegisterVo.getCode())) {
//                调用member服务注册用户
                try {
                    R r = memberFeignService.register(userRegisterVo);
                    if (r.getCode() == 0) {
                        return "redirect:http://auth.gulimall.com/login.html";
                    } else {
                        errorMap.put("msg", (String) r.get("msg"));
                        redirectAttributes.addFlashAttribute("error", errorMap);
                        return "redirect:http://auth.gulimall.com/reg.html";
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                errorMap.put("code", "请输入正确的验证码");
                redirectAttributes.addFlashAttribute("error", errorMap);
                return "redirect:http://auth.gulimall.com/reg.html";
            }
        } else {
            errorMap.put("code", "请发送验证码");
            redirectAttributes.addFlashAttribute("error", errorMap);
            return "redirect:http://auth.gulimall.com/reg.html";
        }
    }

}
