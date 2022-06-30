package com.wz.authserver.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
@Data
public class UserRegisterVo {
    @NotEmpty(message = "用户名必须提交")
    @Length(min = 6,max = 20,message = "用户名长度在6-20之间")
    private String userName;
    @NotEmpty(message = "密码必须提交")
    @Length(min = 6,max = 20,message = "密码长度在6-20之间")
    private String password;
    @NotEmpty(message = "手机号不能为空")
    @Pattern(regexp = "^[1]([1-9])[0-9]]{9}$",message = "手机号格式不正确")
    private String phone;
    @NotEmpty(message = "验证码必须填写")
    private String code;
}
