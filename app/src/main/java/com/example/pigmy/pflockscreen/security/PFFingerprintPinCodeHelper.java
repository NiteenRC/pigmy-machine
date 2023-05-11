package com.example.pigmy.pflockscreen.security;

import android.content.Context;

import androidx.core.hardware.fingerprint.FingerprintManagerCompat;

import com.example.pigmy.pflockscreen.security.callbacks.PFPinCodeHelperCallback;

public class PFFingerprintPinCodeHelper implements IPFPinCodeHelper {


    private static final String FINGERPRINT_ALIAS = "fp_fingerprint_lock_screen_key_store";
    private static final String PIN_ALIAS = "fp_pin_lock_screen_key_store";

    private static final PFFingerprintPinCodeHelper ourInstance = new PFFingerprintPinCodeHelper();

    public static PFFingerprintPinCodeHelper getInstance() {
        return ourInstance;
    }

    private final IPFSecurityUtils pfSecurityUtils
            = PFSecurityUtilsFactory.getPFSecurityUtilsInstance();

    private PFFingerprintPinCodeHelper() {

    }

    @Override
    public void encodePin(Context context, String pin, PFPinCodeHelperCallback<String> callback) {
        try {
            final String encoded = pfSecurityUtils.encode(context, PIN_ALIAS, pin, false);
            if (callback != null) {
                callback.onResult(new PFResult(encoded));
            }
        } catch (PFSecurityException e) {
            if (callback != null) {
                callback.onResult(new PFResult(e.getError()));
            }
        }
    }

    @Override
    public void checkPin(Context context, String encodedPin, String pin, PFPinCodeHelperCallback<Boolean> callback) {
        try {
            final String pinCode = pfSecurityUtils.decode(PIN_ALIAS, encodedPin);
            if (callback != null) {
                callback.onResult(new PFResult(pinCode.equals(pin)));
            }
        } catch (PFSecurityException e) {
            if (callback != null) {
                callback.onResult(new PFResult(e.getError()));
            }
        }
    }


    private boolean isFingerPrintAvailable(Context context) {
        return FingerprintManagerCompat.from(context).isHardwareDetected();
    }

    private boolean isFingerPrintReady(Context context) {
        return FingerprintManagerCompat.from(context).hasEnrolledFingerprints();
    }

    @Override
    public void delete(PFPinCodeHelperCallback<Boolean> callback) {
        try {
            pfSecurityUtils.deleteKey(PIN_ALIAS);
            if (callback != null) {
                callback.onResult(new PFResult(true));
            }
        } catch (PFSecurityException e) {
            if (callback != null) {
                callback.onResult(new PFResult(e.getError()));
            }
        }
    }

    @Override
    public void isPinCodeEncryptionKeyExist(PFPinCodeHelperCallback<Boolean> callback) {
        try {
            final boolean isExist = pfSecurityUtils.isKeystoreContainAlias(PIN_ALIAS);
            if (callback != null) {
                callback.onResult(new PFResult(isExist));
            }
        } catch (PFSecurityException e) {
            if (callback != null) {
                callback.onResult(new PFResult(e.getError()));
            }
        }
    }
}
