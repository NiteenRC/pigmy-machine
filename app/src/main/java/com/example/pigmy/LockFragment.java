package com.example.pigmy;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.navigation.Navigation;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.pigmy.databinding.FragmentLockBinding;
import com.example.pigmy.pflockscreen.PFFLockScreenConfiguration;
import com.example.pigmy.pflockscreen.SharedPref;
import com.example.pigmy.pflockscreen.fragments.PFLockScreenFragment;
import com.example.pigmy.pflockscreen.security.PFResult;
import com.example.pigmy.pflockscreen.viewmodels.PFPinCodeViewModel;

public class LockFragment extends Fragment {

    FragmentLockBinding fragmentLockBinding;
    private SharedPref sharedPre;

    public LockFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentLockBinding = FragmentLockBinding.inflate(inflater, container, false);
        sharedPre = new SharedPref(getActivity());
        showLockScreenFragment();
        return fragmentLockBinding.getRoot();
    }

    private final PFLockScreenFragment.OnPFLockScreenCodeCreateListener mCodeCreateListener = new PFLockScreenFragment.OnPFLockScreenCodeCreateListener() {
        @Override
        public void onCodeCreated(String encodedCode) {
//            methods.showSnackBar("Code created","success");
            sharedPre.saveToPref(encodedCode, true);
//            Setting.in_code = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
//                    getActivity().onBackPressed();
                    Navigation.findNavController(fragmentLockBinding.getRoot()).navigate(R.id.action_pinFragment_to_homeFragment);
                }
            },500);
        }

        @Override
        public void onNewCodeValidationFailed() {
//            methods.showSnackBar("Code validation error","error");
        }
    };

    private final PFLockScreenFragment.OnPFLockScreenLoginListener mLoginListener = new PFLockScreenFragment.OnPFLockScreenLoginListener() {

        @Override
        public void onCodeInputSuccessful() {
//            methods.showSnackBar("Code successfull","success");
            showMainFragment();
        }

        @Override
        public void onFingerprintSuccessful() {
//            methods.showSnackBar("Fingerprint successfull","success");
            showMainFragment();
        }

        @Override
        public void onPinLoginFailed() {
//            methods.showSnackBar("Pin failed","error");
        }

        @Override
        public void onFingerprintLoginFailed() {
//            methods.showSnackBar("Fingerprint failed","error");
        }
    };

    private void showMainFragment() {
        Navigation.findNavController(fragmentLockBinding.getRoot()).navigate(R.id.action_pinFragment_to_homeFragment);
    }

    private void showLockScreenFragment() {
        new PFPinCodeViewModel().isPinCodeEncryptionKeyExist().observe(
                getActivity(),
                new Observer<PFResult<Boolean>>() {
                    @Override
                    public void onChanged(@Nullable PFResult<Boolean> result) {
                        if (result == null) {
                            return;
                        }
                        if (result.getError() != null) {
//                            methods.showSnackBar("Can not get pin code info","error");
                            return;
                        }
//                        showLockScreenFragment(result.getResult());
                        showLockScreenFragment(true);
                    }
                }
        );
    }

    private void showLockScreenFragment(boolean isPinExist) {
        final PFFLockScreenConfiguration.Builder builder = new PFFLockScreenConfiguration.Builder(getActivity())
                .setTitle(isPinExist ? "Unlock with your pin code or fingerprint" : "Create Code")
                .setCodeLength(4)
                .setLeftButton("Can't remeber")
                .setNewCodeValidation(true)
                .setNewCodeValidationTitle("Please input code again")
                .setUseFingerprint(true);
        final PFLockScreenFragment fragment = new PFLockScreenFragment();

        fragment.setOnLeftButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                methods.showSnackBar("Left button pressed","success");
            }
        });

        builder.setMode(isPinExist ? PFFLockScreenConfiguration.MODE_AUTH : PFFLockScreenConfiguration.MODE_CREATE);
        if (isPinExist) {
            fragment.setEncodedPinCode(sharedPre.getCode());
            fragment.setLoginListener(mLoginListener);
        }

        fragment.setConfiguration(builder.build());
        fragment.setCodeCreateListener(mCodeCreateListener);
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container_view, fragment).commit();

    }
}