package com.nhb.exception;

/**
 * 登录失败
 */
public class RegisterFailedException extends BaseRuntimeException{
    public RegisterFailedException(String msg){
        super(msg);
    }
}
