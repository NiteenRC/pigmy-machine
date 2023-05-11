package com.example.pigmy;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.SEND_SMS;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.navigation.Navigation;

import com.example.pigmy.databinding.FragmentLoginBinding;
import com.example.pigmy.pflockscreen.SharedPref;
import com.example.pigmy.pflockscreen.fragments.PFLockScreenFragment;
import com.example.pigmy.pflockscreen.security.PFResult;
import com.example.pigmy.pflockscreen.viewmodels.PFPinCodeViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


public class LoginFragment extends Fragment implements View.OnClickListener {

    private FragmentLoginBinding binding;
    private SharedPreferences sharedPreferences;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    private SharedPref sharedPre;
    private final PFPinCodeViewModel mPFPinCodeViewModel = new PFPinCodeViewModel();
    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = requireActivity().getSharedPreferences(AppConstants.USER_SHARED_PREFERENCES, Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        sharedPre = new SharedPref(getActivity());
        askPermission();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btnLogin.setOnClickListener(this);
        if (!sharedPreferences.getString(AppConstants.USER_CODE, "").isEmpty()){
            binding.etCode.setEnabled(false);
            binding.etCode.setText(sharedPreferences.getString(AppConstants.USER_CODE, ""));
            binding.etPassword.setEnabled(false);
            binding.etPassword.setText(sharedPreferences.getString(AppConstants.USER_PWD, ""));
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnLogin) {
            checkUserExists(view);
        }
    }

    private void askPermission() {
        //checking external storage permission is given or not...
        if (ContextCompat.checkSelfPermission(requireActivity(), WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireActivity(), READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireActivity(), SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE, SEND_SMS}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!Environment.isExternalStorageManager()) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    Uri uri = Uri.fromParts("package", requireActivity().getPackageName(), null);
                    intent.setData(uri);
                    getActivity().startActivity(intent);
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        askPermission();
    }

    private boolean validateLogin() {
        List<Boolean> isValid = new ArrayList<>();
        String code = binding.etCode.getText().toString().trim();
        String pwd = binding.etPassword.getText().toString().trim();
        if (code.isEmpty()) {
            binding.tlCode.setError("Please enter agent code");
            binding.tlCode.setErrorEnabled(true);
            isValid.add(false);
        } else {
            isValid.add(true);
            binding.tlCode.setError(null);
            binding.tlCode.setErrorEnabled(false);
        }
        if (pwd.isEmpty()) {
            binding.tlPassword.setError("Please enter your password");
            binding.tlPassword.setErrorEnabled(true);
            isValid.add(false);
        } else {
            isValid.add(true);
            binding.tlPassword.setError(null);
            binding.tlPassword.setErrorEnabled(false);
        }
        return !isValid.contains(false);
    }

    private void checkUserExists(View view) {
        if (!validateLogin()) {
            return;
        }
        String code = binding.etCode.getText().toString().trim();
        String pwd = binding.etPassword.getText().toString().trim();

        binding.btnLogin.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        CollectionReference collectionReference = firebaseFirestore.collection(AppConstants.USERS_COLLECTION);
        collectionReference.whereEqualTo("code", code).whereEqualTo("pwd", pwd)
                .limit(1)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().isEmpty()) {
                                Toast.makeText(requireActivity(), "Invalid Code and Password", Toast.LENGTH_LONG).show();
                            } else {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    System.out.println(document.getId() + " => " + document.getData());
                                    String userName = document.getString("name");
                                    String shortName = document.getString("shortname");
                                    String userRole = document.getString("role");
                                    String pwd = document.getString("pwd");
                                    long pin = document.getLong("pin");

                                    SharedPreferences.Editor editor = sharedPreferences.edit();

                                    editor.putString(AppConstants.USER_NAME, userName);
                                    editor.putString(AppConstants.USER_CODE, code);
                                    editor.putString(AppConstants.USER_ROLE, userRole);
                                    editor.putString(AppConstants.USER_SHORT_NAME, shortName);
                                    editor.putLong(AppConstants.USER_PIN_IN, pin);
                                    editor.putString(AppConstants.USER_PWD, pwd);
                                    editor.putBoolean(AppConstants.USER_LOGGED_IN, true);
                                    editor.apply();

                                    mPFPinCodeViewModel.encodePin(getContext(), String.valueOf(pin)).observe(getActivity(), new Observer<PFResult<String>>() {
                                                @Override
                                                public void onChanged(@Nullable PFResult<String> result) {
                                                    if (result == null) {
                                                        return;
                                                    }
//                                                    if (result.getError() != null) {
//                                                        Log.d(TAG, "Can not encode pin code");
//                                                        deleteEncodeKey();
//                                                        return;
//                                                    }
                                                    final String encodedCode = result.getResult();
                                                    Log.e("TAG", "onChanged: "+pin);
//                                                    if (mCodeCreateListener != null) {
                                                    final PFLockScreenFragment fragment = new PFLockScreenFragment();
                                                    fragment.setEncodedPinCode(encodedCode);
                                                        sharedPre.saveToPref(encodedCode, true);
//                                                        mCodeCreateListener.onCodeCreated(encodedCode);
//                                                    }
                                                }
                                            }
                                    );

                                    Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_pinFragment);
                                }
                            }
                        } else {
                            Toast.makeText(requireActivity(), "Error in connecting to database", Toast.LENGTH_LONG).show();
                        }
                        Log.e("TAG", "onComplete: " + task.toString());
                        binding.progressBar.setVisibility(View.INVISIBLE);
                        binding.btnLogin.setEnabled(true);
                    }
                });
    }
}