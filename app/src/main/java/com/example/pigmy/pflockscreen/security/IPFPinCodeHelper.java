package com.example.pigmy.pflockscreen.security;

import android.content.Context;

import com.example.pigmy.pflockscreen.security.callbacks.PFPinCodeHelperCallback;

public interface IPFPinCodeHelper {

    void encodePin(Context context, String pin, PFPinCodeHelperCallback<String> callBack);

    void checkPin(Context context, String encodedPin, String pin, PFPinCodeHelperCallback<Boolean> callback);

    void delete(PFPinCodeHelperCallback<Boolean> callback);

    void isPinCodeEncryptionKeyExist(PFPinCodeHelperCallback<Boolean> callback);

}
