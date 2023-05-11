package com.example.pigmy.pflockscreen.security.callbacks;

import com.example.pigmy.pflockscreen.security.PFResult;

public interface PFPinCodeHelperCallback<T> {
    void onResult(PFResult<T> result);
}
