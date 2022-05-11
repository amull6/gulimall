package com.wz.gulimall.product.exception;

import com.wz.common.exception.BizCodeEnum;
import com.wz.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: 2014015130
 * @Date: 2022/5/11 14:02
 * @Description:
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.wz.gulimall.product.controller")
public class GulimallExceptionControllerAdvice {
    @ExceptionHandler()
    public R handleValidException(MethodArgumentNotValidException exception) {
        BindingResult result = exception.getBindingResult();
        Map<String, String> map = new HashMap<>();
        result.getFieldErrors().forEach((item) -> {
            map.put(item.getField(), item.getDefaultMessage());
        });
        log.error("数据校验出现问题{}，异常类型{}", exception.getMessage(), exception.getClass());
        return R.error(BizCodeEnum.VALIDE_EXCEPTION.getCode(), BizCodeEnum.VALIDE_EXCEPTION.getMsg()).put("data", map);
    }

    @ExceptionHandler(value = Throwable.class)
    public R handleException(Throwable exception) {
        log.error("未知异常{}，异常类型{}", exception.getMessage(), exception.getClass());
        return R.error(BizCodeEnum.UNKNOW_EXCEPTION.getCode(), BizCodeEnum.UNKNOW_EXCEPTION.getMsg());
    }

}
