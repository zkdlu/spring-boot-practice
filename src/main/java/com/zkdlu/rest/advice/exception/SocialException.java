package com.zkdlu.rest.advice.exception;

public class SocialException extends RuntimeException {
    public SocialException(String msg, Throwable t) {
        super(msg, t);
    }
    public SocialException(String msg) {
        super(msg);
    }
    public SocialException() {
        super();
    }
}
