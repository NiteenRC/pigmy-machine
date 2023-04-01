package com.example.pigmy;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.pigmy.databinding.FragmentLoginBinding;
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
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btnLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnLogin) {
            checkUserExists(view);
        }
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

                                    SharedPreferences.Editor editor = sharedPreferences.edit();

                                    editor.putString(AppConstants.USER_NAME, userName);
                                    editor.putString(AppConstants.USER_CODE, code);
                                    editor.putString(AppConstants.USER_ROLE, userRole);
                                    editor.putString(AppConstants.USER_SHORT_NAME, shortName);
                                    editor.putBoolean(AppConstants.USER_LOGGED_IN, true);

                                    editor.apply();
                                    Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_homeFragment);
                                }
                            }
                        } else {
                            Toast.makeText(requireActivity(), "Error in connecting to database", Toast.LENGTH_LONG).show();
                        }
                        binding.progressBar.setVisibility(View.INVISIBLE);
                        binding.btnLogin.setEnabled(true);
                    }
                });
    }
}