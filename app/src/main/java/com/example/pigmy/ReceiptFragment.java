package com.example.pigmy;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.pigmy.databinding.FragmentReceiptBinding;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class ReceiptFragment extends DialogFragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    private SharedPreferences sharedPreferences;
    private FragmentReceiptBinding binding;

    public ReceiptFragment() {
        // Required empty public constructor
    }

    public static ReceiptFragment newInstance(/*String param1, String param2*/) {
        ReceiptFragment fragment = new ReceiptFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        sharedPreferences = requireActivity().getSharedPreferences(AppConstants.USER_SHARED_PREFERENCES, Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentReceiptBinding.inflate(inflater, container, false);
        // Set transparent background and no title
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        return binding.getRoot();
    }

    private void extracted(String key, int textType, String label) {
        key = getArguments().getString(key);
        TextView accTypeTextView = binding.getRoot().findViewById(textType);
        accTypeTextView.setText(String.format(label, key));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        extracted("date_time", R.id.date_time, "Date  : %s");
        extracted("agent_code", R.id.agent_code, "Code  : %s");
        extracted("acc_type", R.id.acc_type, "Type  : %s");
        extracted("customer_name", R.id.customer_name, "Name : %s");
        extracted("acc_no", R.id.acc_no, "Acc No : %s");
        extracted("prev_balance", R.id.prev_balance, "Previous: %s");
      /*  extracted( "deposit_amount", R.id.deposit_amount, "Dept Amt    : %s");
        extracted( "total_amount", R.id.total_amount, "Total Amt   : %s");*/
        double depositAmount = getArguments().getDouble("deposit_amount");
        double totalAmount = getArguments().getDouble("total_amount");

        binding.depositAmount.setText("Deposit : " + depositAmount);
        binding.totalAmount.setText("Total     : " + totalAmount);

        binding.confirmButton.setOnClickListener(view1 -> {
            dismiss();
            Account account = getArguments().getParcelable("account");
            double depositAmount1 = getArguments().getDouble("deposit_amount");
            double totalAmount1 = getArguments().getDouble("total_amount");
            saveTransaction(depositAmount1, totalAmount1, account);
            Toast.makeText(requireActivity(), "Saved Successfully", Toast.LENGTH_LONG).show();

            NavHostFragment.findNavController(this).getPreviousBackStackEntry().getSavedStateHandle().set("key", "clear");
        });

        binding.cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    private void saveTransaction(double depositAmount, double totalAmount, Account selectedAccount) {

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        CollectionReference transactionCollectionReference = firebaseFirestore.collection(AppConstants.TRANSACTION_COLLECTION);
        CollectionReference accountCollectionReference = firebaseFirestore.collection(AppConstants.ACCOUNT_COLLECTION);

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(new Date());

        HashMap<String, Object> transactionMap = new HashMap<>();
        transactionMap.put("accNo", selectedAccount.getAccNo());
        transactionMap.put("accType", selectedAccount.getType());
        transactionMap.put("code", sharedPreferences.getString(AppConstants.USER_CODE, ""));
        transactionMap.put("agentName", sharedPreferences.getString(AppConstants.USER_NAME, ""));
        transactionMap.put("date", today);
        transactionMap.put("depAmount", depositAmount);
        transactionMap.put("name", selectedAccount.getName());
        transactionMap.put("prevAmount", selectedAccount.getPrevAmount());

        transactionCollectionReference.add(transactionMap);

        HashMap<String, Object> accountMap = new HashMap<>();
        accountMap.put("prevAmount", totalAmount);
        accountCollectionReference.document(String.valueOf(selectedAccount.getId())).update(accountMap);
    }
}