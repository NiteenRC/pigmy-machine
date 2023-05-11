package com.example.pigmy.pflockscreen.fragments;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.core.hardware.fingerprint.FingerprintManagerCompat;
import androidx.fragment.app.DialogFragment;

import com.example.pigmy.R;

@RequiresApi(api = Build.VERSION_CODES.M)
public class PFFingerprintAuthDialogFragment extends DialogFragment {

    private TextView mCancelButton;
    private View mFingerprintContent;

    private Stage mStage = Stage.FINGERPRINT;

    private FingerprintManagerCompat.CryptoObject mCryptoObject;

    private PFFingerprintUIHelper mFingerprintCallback;

    private Context mContext;

    private PFFingerprintAuthListener mAuthListener;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.view_pf_fingerprint_dialog_container, container, false);
        mCancelButton = v.findViewById(R.id.cancel_button);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        mFingerprintContent = v.findViewById(R.id.fingerprint_container);

        FingerprintManagerCompat manager = FingerprintManagerCompat.from(getContext());
        mFingerprintCallback = new PFFingerprintUIHelper(manager,
                (ImageView) v.findViewById(R.id.fingerprint_icon),
                (TextView) v.findViewById(R.id.fingerprint_status),
                mAuthListener);
        updateStage();
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mStage == Stage.FINGERPRINT) {
            mFingerprintCallback.startListening(mCryptoObject);
        }
    }

    public void setStage(Stage stage) {
        mStage = stage;
    }

    @Override
    public void onPause() {
        super.onPause();
        mFingerprintCallback.stopListening();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }
    /*public void setCryptoObject(FingerprintManagerCompat.CryptoObject cryptoObject) {
        mCryptoObject = cryptoObject;
    }*/


    private void updateStage() {
        switch (mStage) {
            case FINGERPRINT:
                mCancelButton.setText(R.string.cancel_pf);
                mFingerprintContent.setVisibility(View.VISIBLE);
                break;
        }
    }


    public void setAuthListener(PFFingerprintAuthListener authListener) {
        mAuthListener = authListener;
    }

    public enum Stage {
        FINGERPRINT
    }
}
