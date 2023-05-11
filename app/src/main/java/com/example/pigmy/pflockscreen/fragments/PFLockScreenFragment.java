package com.example.pigmy.pflockscreen.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.hardware.fingerprint.FingerprintManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import com.example.pigmy.AppConstants;
import com.example.pigmy.R;
import com.example.pigmy.pflockscreen.PFFLockScreenConfiguration;
import com.example.pigmy.pflockscreen.security.PFResult;
import com.example.pigmy.pflockscreen.viewmodels.PFPinCodeViewModel;
import com.example.pigmy.pflockscreen.views.PFCodeView;

public class PFLockScreenFragment extends Fragment {

    private static final String TAG = PFLockScreenFragment.class.getName();

    private static final String FINGERPRINT_DIALOG_FRAGMENT_TAG = "FingerprintDialogFragment";

    private static final String INSTANCE_STATE_CONFIG = "com.example.pigmy.pflockscreen.instance_state_config";

    private View mFingerprintButton;
    private View mDeleteButton;
    private TextView mLeftButton;
    private Button mNextButton;
    private PFCodeView mCodeView;
    private TextView titleView, user_text_view;

    private boolean mUseFingerPrint = true;
    private boolean mFingerprintHardwareDetected = false;
    private boolean mIsCreateMode = false;

    private OnPFLockScreenCodeCreateListener mCodeCreateListener;
    private OnPFLockScreenLoginListener mLoginListener;

    private String mCode = "";
    private String mCodeValidation = "";
    private String mEncodedPinCode = "";

    private PFFLockScreenConfiguration mConfiguration;
    private View mRootView;

    private final PFPinCodeViewModel mPFPinCodeViewModel = new PFPinCodeViewModel();

    private View.OnClickListener mOnLeftButtonClickListener = null;

    private SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(INSTANCE_STATE_CONFIG, mConfiguration);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_lock_screen_pf, container, false);

        mFingerprintButton = view.findViewById(R.id.button_finger_print);
        mDeleteButton = view.findViewById(R.id.button_delete);
        sharedPreferences = requireActivity().getSharedPreferences(AppConstants.USER_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        if (mConfiguration == null) {
            mConfiguration = (PFFLockScreenConfiguration) savedInstanceState.getSerializable(INSTANCE_STATE_CONFIG);
        }
        if (sharedPreferences.getBoolean(AppConstants.IS_FIRST_TIME, true)){
            mFingerprintButton.setVisibility(View.GONE);
            mDeleteButton.setVisibility(View.VISIBLE);
        } else {
            mFingerprintButton.setVisibility(View.VISIBLE);
            mDeleteButton.setVisibility(View.GONE);
        }

        mLeftButton = view.findViewById(R.id.button_left);
        mNextButton = view.findViewById(R.id.button_next);

        mDeleteButton.setOnClickListener(mOnDeleteButtonClickListener);
        mDeleteButton.setOnLongClickListener(mOnDeleteButtonOnLongClickListener);
        mFingerprintButton.setOnClickListener(mOnFingerprintClickListener);

        mCodeView = view.findViewById(R.id.code_view);
        initKeyViews(view);

        mCodeView.setListener(mCodeListener);

        if (!mUseFingerPrint) {
            mFingerprintButton.setVisibility(View.GONE);
        }

        mFingerprintHardwareDetected = isFingerprintApiAvailable(getContext());

        mRootView = view;
        applyConfiguration(mConfiguration);

        return view;
    }

    @Override
    public void onStart() {
        if (!mIsCreateMode && mUseFingerPrint && mConfiguration.isAutoShowFingerprint() && isFingerprintApiAvailable(getActivity()) && isFingerprintsExists(getActivity())) {
            mOnFingerprintClickListener.onClick(mFingerprintButton);
        }
        super.onStart();
    }

    public void setConfiguration(PFFLockScreenConfiguration configuration) {
        this.mConfiguration = configuration;
        applyConfiguration(configuration);
    }

    private void applyConfiguration(PFFLockScreenConfiguration configuration) {
        if (mRootView == null || configuration == null) {
            return;
        }
        titleView = mRootView.findViewById(R.id.title_text_view);
        user_text_view = mRootView.findViewById(R.id.user_text_view);
        user_text_view.setText(sharedPreferences.getString(AppConstants.USER_NAME, ""));
        titleView.setText(configuration.getTitle());
        if (TextUtils.isEmpty(configuration.getLeftButton())) {
            mLeftButton.setVisibility(View.GONE);
        } else {
            mLeftButton.setText(configuration.getLeftButton());
            mLeftButton.setOnClickListener(mOnLeftButtonClickListener);
        }

        if (!TextUtils.isEmpty(configuration.getNextButton())) {
            mNextButton.setText(configuration.getNextButton());
        }

        mUseFingerPrint = configuration.isUseFingerprint();
        if (!mUseFingerPrint) {
            mFingerprintButton.setVisibility(View.GONE);
            mDeleteButton.setVisibility(View.VISIBLE);
        }
        mIsCreateMode = mConfiguration.getMode() == PFFLockScreenConfiguration.MODE_CREATE;

        if (mIsCreateMode) {
            mLeftButton.setVisibility(View.GONE);
            mFingerprintButton.setVisibility(View.GONE);
        }

        if (mIsCreateMode) {
            mNextButton.setOnClickListener(mOnNextButtonClickListener);
        } else {
            mNextButton.setOnClickListener(null);
        }

        mNextButton.setVisibility(View.INVISIBLE);
        mCodeView.setCodeLength(mConfiguration.getCodeLength());
    }

    private void initKeyViews(View parent) {
        parent.findViewById(R.id.button_0).setOnClickListener(mOnKeyClickListener);
        parent.findViewById(R.id.button_1).setOnClickListener(mOnKeyClickListener);
        parent.findViewById(R.id.button_2).setOnClickListener(mOnKeyClickListener);
        parent.findViewById(R.id.button_3).setOnClickListener(mOnKeyClickListener);
        parent.findViewById(R.id.button_4).setOnClickListener(mOnKeyClickListener);
        parent.findViewById(R.id.button_5).setOnClickListener(mOnKeyClickListener);
        parent.findViewById(R.id.button_6).setOnClickListener(mOnKeyClickListener);
        parent.findViewById(R.id.button_7).setOnClickListener(mOnKeyClickListener);
        parent.findViewById(R.id.button_8).setOnClickListener(mOnKeyClickListener);
        parent.findViewById(R.id.button_9).setOnClickListener(mOnKeyClickListener);
    }

    private final View.OnClickListener mOnKeyClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v instanceof TextView) {
                final String string = ((TextView) v).getText().toString();
                if (string.length() != 1) {
                    return;
                }
                final int codeLength = mCodeView.input(string);
                configureRightButton(codeLength);
            }
        }
    };

    private final View.OnClickListener mOnDeleteButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final int codeLength = mCodeView.delete();
            configureRightButton(codeLength);
        }
    };

    private final View.OnLongClickListener mOnDeleteButtonOnLongClickListener
            = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            mCodeView.clearCode();
            configureRightButton(0);
            return true;
        }
    };

    private final View.OnClickListener mOnFingerprintClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                    !isFingerprintApiAvailable(getActivity())) {
                return;
            }


            if (!isFingerprintsExists(getActivity())) {
                showNoFingerprintDialog();
                return;
            }

            final PFFingerprintAuthDialogFragment fragment = new PFFingerprintAuthDialogFragment();
            fragment.show(getFragmentManager(), FINGERPRINT_DIALOG_FRAGMENT_TAG);
            fragment.setAuthListener(new PFFingerprintAuthListener() {
                @Override
                public void onAuthenticated() {
                    if (mLoginListener != null) {
                        mLoginListener.onFingerprintSuccessful();
                    }
                    fragment.dismiss();
                }

                @Override
                public void onError() {
                    if (mLoginListener != null) {
                        mLoginListener.onFingerprintLoginFailed();
                    }
                }
            });
        }
    };

    private void configureRightButton(int codeLength) {
        if (mIsCreateMode) {
            if (codeLength > 0) {
                mDeleteButton.setVisibility(View.VISIBLE);
            } else {
                mDeleteButton.setVisibility(View.GONE);
            }
            return;
        }

        if (codeLength > 0) {
            mFingerprintButton.setVisibility(View.GONE);
            mDeleteButton.setVisibility(View.VISIBLE);
            mDeleteButton.setEnabled(true);
            return;
        }

        if (sharedPreferences.getBoolean(AppConstants.IS_FIRST_TIME, true)) {
            mFingerprintButton.setVisibility(View.GONE);
            mDeleteButton.setVisibility(View.VISIBLE);
        } else {
            if (mUseFingerPrint && mFingerprintHardwareDetected) {
                mFingerprintButton.setVisibility(View.VISIBLE);
                mDeleteButton.setVisibility(View.GONE);
            } else {
                mFingerprintButton.setVisibility(View.GONE);
                mDeleteButton.setVisibility(View.VISIBLE);
            }
        }

        mDeleteButton.setEnabled(false);

    }


    private boolean isFingerprintApiAvailable(Context context) {
        return FingerprintManagerCompat.from(context).isHardwareDetected();
    }


    private boolean isFingerprintsExists(Context context) {
        return FingerprintManagerCompat.from(context).hasEnrolledFingerprints();
    }


    private void showNoFingerprintDialog() {
        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.no_fingerprint);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));

        TextView cancel = dialog.findViewById(R.id.cancel);
        TextView setting_dialog = dialog.findViewById(R.id.setting_dialog);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        setting_dialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                startActivity(new Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS));
            }
        });

        dialog.show();

//            new AlertDialog.Builder(new ContextThemeWrapper(getContext(), R.style.AlertDialogCustom))
//                    .setTitle(R.string.no_fingerprints_title_pf)
//                    .setMessage(R.string.no_fingerprints_message_pf)
//                    .setCancelable(true)
//                    .setNegativeButton(R.string.cancel_pf, null)
//                    .setPositiveButton(R.string.settings_pf, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            startActivity(new Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS));
//                        }
//                    }).create().show();
    }


    private final PFCodeView.OnPFCodeListener mCodeListener = new PFCodeView.OnPFCodeListener() {
        @Override
        public void onCodeCompleted(String code) {
            if (mIsCreateMode) {
                mNextButton.setVisibility(View.VISIBLE);
                mCode = code;
                return;
            }
            mCode = code;
            mPFPinCodeViewModel.checkPin(getContext(), mEncodedPinCode, mCode).observe(
                    PFLockScreenFragment.this,
                    new Observer<PFResult<Boolean>>() {
                        @Override
                        public void onChanged(@Nullable PFResult<Boolean> result) {
                            if (result == null) {
                                return;
                            }
                            if (result.getError() != null) {
                                return;
                            }
                            final boolean isCorrect = result.getResult();
                            if (mLoginListener != null) {
                                if (isCorrect) {
                                    editor.putBoolean(AppConstants.IS_FIRST_TIME, false);
                                    editor.putBoolean(AppConstants.USER_LOGGED_PIN_IN, true);
                                    editor.apply();
                                    mLoginListener.onCodeInputSuccessful();
                                } else {
                                    mLoginListener.onPinLoginFailed();
                                    errorAction();
                                }
                            }
                            if (!isCorrect && mConfiguration.isClearCodeOnError()) {
                                mCodeView.clearCode();
                            }
                        }
                    });

        }

        @Override
        public void onCodeNotCompleted(String code) {
            if (mIsCreateMode) {
                mNextButton.setVisibility(View.INVISIBLE);
                return;
            }
        }
    };


    private final View.OnClickListener mOnNextButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mConfiguration.isNewCodeValidation() && TextUtils.isEmpty(mCodeValidation)) {
                mCodeValidation = mCode;
                cleanCode();
                titleView.setText(mConfiguration.getNewCodeValidationTitle());
                return;
            }
            if (mConfiguration.isNewCodeValidation() && !TextUtils.isEmpty(mCodeValidation) && !mCode.equals(mCodeValidation)) {
                mCodeCreateListener.onNewCodeValidationFailed();
                titleView.setText(mConfiguration.getTitle());
                cleanCode();
                return;
            }
            mCodeValidation = "";
            mPFPinCodeViewModel.encodePin(getContext(), mCode).observe(PFLockScreenFragment.this, new Observer<PFResult<String>>() {
                        @Override
                        public void onChanged(@Nullable PFResult<String> result) {
                            if (result == null) {
                                return;
                            }
                            if (result.getError() != null) {
                                Log.d(TAG, "Can not encode pin code");
                                deleteEncodeKey();
                                return;
                            }
                            final String encodedCode = result.getResult();
                            if (mCodeCreateListener != null) {
                                mCodeCreateListener.onCodeCreated(encodedCode);
                            }
                        }
                    }
            );
        }
    };

    private void cleanCode() {
        mCode = "";
        mCodeView.clearCode();
    }


    private void deleteEncodeKey() {
        mPFPinCodeViewModel.delete().observe(
                this,
                new Observer<PFResult<Boolean>>() {
                    @Override
                    public void onChanged(@Nullable PFResult<Boolean> result) {
                        if (result == null) {
                            return;
                        }
                        if (result.getError() != null) {
                            Log.d(TAG, "Can not delete the alias");
                            return;
                        }

                    }
                }
        );
    }

    private void errorAction() {
        if (mConfiguration.isErrorVibration()) {
            final Vibrator v = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null) {
                v.vibrate(400);
            }
        }

        if (mConfiguration.isErrorAnimation()) {
            final Animation animShake = AnimationUtils.loadAnimation(getContext(), R.anim.shake_pf);
            mCodeView.startAnimation(animShake);
        }
    }

    public void setOnLeftButtonClickListener(View.OnClickListener onLeftButtonClickListener) {
        this.mOnLeftButtonClickListener = onLeftButtonClickListener;
    }

    public void setCodeCreateListener(OnPFLockScreenCodeCreateListener listener) {
        mCodeCreateListener = listener;
    }

    public void setLoginListener(OnPFLockScreenLoginListener listener) {
        mLoginListener = listener;
    }

    public void setEncodedPinCode(String encodedPinCode) {
        mEncodedPinCode = encodedPinCode;
    }

    public interface OnPFLockScreenCodeCreateListener {

        void onCodeCreated(String encodedCode);

        void onNewCodeValidationFailed();

    }

    public interface OnPFLockScreenLoginListener {

        void onCodeInputSuccessful();

        void onFingerprintSuccessful();

        void onPinLoginFailed();

        void onFingerprintLoginFailed();
    }
}