package com.wz.common.exception;

/**
 * @Author: 2014015130
 * @Date: 2022/5/11 14:14
 * @Description:
 */
public enum BizCodeEnum {
    UNKNOW_EXCEPTION(10000,"系统未知异常"),
    VALIDE_EXCEPTION(10001,"参数格式校验失败"),

    TOO_MANY_REQUEST_EXCEPTION(10003,"流量过载"),
    PRODUCT_UP_EXCEPTION(11000,"商品上架异常"),

    VALIDE_CODE_EXCEPTION(10002,"短信验证码获取频率太高"),

    USERNAME_EXIST_EXCEPTION(15001,"用户已存在"),

    PHONE_EXIST_EXCEPTION(15002, "手机号码已存在"),

    LOGINACCT_PASSWORD_EXCEPTION(15002, "账号或者密码不正确"),

    NO_STOCK_EXCEPTION(20001, "商品库存不足");


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
