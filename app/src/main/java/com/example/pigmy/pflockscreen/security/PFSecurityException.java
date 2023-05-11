package com.example.pigmy.pflockscreen.security;

public class PFSecurityException extends Exception {

    private final Integer mCode;

    public PFSecurityException(String message, Integer code) {
        super(message);
        mCode = code;
    }

    public Integer getCode() {
        return mCode;
    }

    public PFSecurityError getError() {
        return new PFSecurityError(getMessage(), getCode());
    }
}
