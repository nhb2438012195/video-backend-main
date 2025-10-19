package com.nhb.exception;

/**
 * 业务异常
 */
public class BaseRuntimeException extends RuntimeException {

    public BaseRuntimeException() {
    }

    public BaseRuntimeException(String msg) {
        super(msg);
    }

}
