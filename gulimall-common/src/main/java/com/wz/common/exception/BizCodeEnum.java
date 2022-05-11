package com.wz.common.exception;

/**
 * @Author: 2014015130
 * @Date: 2022/5/11 14:14
 * @Description:
 */
public enum BizCodeEnum {
    UNKNOW_EXCEPTION(10000,"系统未知异常"),
    VALIDE_EXCEPTION(10001,"参数格式校验失败");
    private Integer code;
    private String msg;

    BizCodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

}