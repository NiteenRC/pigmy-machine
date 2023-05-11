package com.example.pigmy.pflockscreen.security;

public class PFSecurityError {

    private final String mMessage;
    private final Integer mCode;

    PFSecurityError(String message, Integer code) {
        mMessage = message;
        mCode = code;
    }

    public String getMessage() {
        return mMessage;
    }

    public Integer getCode() {
        return mCode;
    }
}
