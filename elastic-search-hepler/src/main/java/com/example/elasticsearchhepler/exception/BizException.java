package com.example.elasticsearchhepler.exception;



public class BizException extends Exception {
    private static final long serialVersionUID = 4630227923217642600L;
    private Integer code;
    private String message;

    public BizException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
