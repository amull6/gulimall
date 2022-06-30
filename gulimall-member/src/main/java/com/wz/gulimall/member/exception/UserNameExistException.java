package com.wz.gulimall.member.exception;

public class UserNameExistException extends RuntimeException{
    public UserNameExistException() {
        super("用户名号码已存在");
    }
}
