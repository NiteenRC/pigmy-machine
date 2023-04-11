package com.example.pigmy;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.SEND_SMS;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.text.TextUtils.isDigitsOnly;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.navigation.Navigation;

import com.example.pigmy.databinding.FragmentHomeBinding;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    private final Map<String, String> map = new HashMap<>();
    private final List<String> dataList = new ArrayList<>();
    // TODO: Rename and change types of parameters
    private FragmentHomeBinding binding;
    private SharedPreferences sharedPreferences;
    private double totalAmount, prevAmount, depositAmount;
    private int receiptNo;
    private List<Account> accountList;
    private ArrayAdapter<Account> customerArrayAdapter;
    private Account selectedAccount;
    private final ActivityResultLauncher<Intent> activityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        if (Environment.isExternalStorageManager()) {
                            initGUI();
                        }
                    }
                }
            });

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = requireActivity().getSharedPreferences(AppConstants.USER_SHARED_PREFERENCES, Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        askPermission();
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        clearForm();
        askPermission();
    }

    private void askPermission() {
        //checking external storage permission is given or not...
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", requireActivity().getPackageName(), null);
                intent.setData(uri);
                activityResultLauncher.launch(intent);
            } else {
                initGUI();
            }
        } else if (ContextCompat.checkSelfPermission(requireActivity(), WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireActivity(), READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireActivity(), SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE, SEND_SMS}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        } else if (ContextCompat.checkSelfPermission(requireActivity(), WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireActivity(), READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireActivity(), SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            initGUI();
        }
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        initGUI();

        binding.btnSubmit.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ShowToast")
            @Override
            public void onClick(View view) {
                if (Double.parseDouble(binding.tvTotalAmount.getText().toString()) >= 0) {
                    saveRecord(view);
                } else {
                    Toast toast = Toast.makeText(getContext(), "enter valid amount", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }
        });

        binding.btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearForm();
            }
        });

        Navigation.findNavController(view).getCurrentBackStackEntry()
                .getSavedStateHandle().getLiveData("key").observe(requireActivity(), new Observer<Object>() {
                    @Override
                    public void onChanged(Object o) {
                        clearForm();
                        initGUI();
                    }
                });
    }

    private void initGUI() {

        accountList = new ArrayList<>();
        getCustomers();

        binding.tvPrevAmount.setText("");
        binding.tvAccountType.setText("");

       /* dataList = new FileUtils().readDataExternal();
        for (String item : dataList) {
            map.put(item.split(",")[5].trim() + " " + item.split(",")[1].trim() + " " + item.split(",")[3].trim(), item);
        }

        List<String> accNos = new ArrayList<>(map.keySet());*/

        customerArrayAdapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_dropdown_item_1line, accountList);
        binding.avNameAccNo.setAdapter(customerArrayAdapter);
        binding.avNameAccNo.setOnTouchListener((v, event) -> {
            binding.avNameAccNo.showDropDown();
            return false;
        });
        binding.avNameAccNo.requestFocus();
        resetAccTypeAndPrevAmt(binding.avNameAccNo);
        onTextChangedForAccountSelection(binding.avNameAccNo);
        onTextChangedForDepositAmt(binding.tvTotalAmount, binding.tvAmount);

    }

    private void saveRecord(View view) {
        final String customerNameAuto = binding.avNameAccNo.getText().toString();
        final String name = binding.tvAmount.getText().toString();

        if (customerNameAuto.length() == 0) {
            binding.avNameAccNo.requestFocus();
            binding.avNameAccNo.setError("FIELD CANNOT BE EMPTY");
        } else if (name.length() == 0) {
            binding.tvAmount.requestFocus();
            binding.tvAmount.setError("FIELD CANNOT BE EMPTY");
        } else {

            String dateAndTime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH).format(new Date());

//            Bundle bundle = new Bundle();
//            bundle.putString("date_time", dateAndTime);
//            bundle.putString("agent_code", sharedPreferences.getString(AppConstants.USER_CODE, ""));
//            bundle.putString("acc_type", selectedAccount.getType());
//            bundle.putString("customer_name", selectedAccount.getName());
//            bundle.putString("acc_no", selectedAccount.getAccNo());
//            bundle.putString("prev_balance", String.valueOf(prevAmount));
//            bundle.putDouble("deposit_amount", depositAmount);
//            bundle.putDouble("total_amount", totalAmount);
//            bundle.putParcelable("account", selectedAccount);
//
//            Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_receiptFragment, bundle);
            Dialog dialog = new Dialog(getActivity());
            dialog.setContentView(R.layout.fragment_receipt);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

            Button cancel_button = dialog.findViewById(R.id.cancel_button);
            Button confirm_button = dialog.findViewById(R.id.confirm_button);
            cancel_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    clearForm();
                    initGUI();
                }
            });

            TextView date_time = dialog.findViewById(R.id.date_time);
            TextView agent_code = dialog.findViewById(R.id.agent_code);
            TextView acc_type = dialog.findViewById(R.id.acc_type);
            TextView customer_name = dialog.findViewById(R.id.customer_name);
            TextView acc_no = dialog.findViewById(R.id.acc_no);
            TextView prev_balance = dialog.findViewById(R.id.prev_balance);
            TextView deposit_amount = dialog.findViewById(R.id.deposit_amount);
            TextView total_amount = dialog.findViewById(R.id.total_amount);

            date_time.setText("Date  : " + dateAndTime);
            agent_code.setText("Code  : " + sharedPreferences.getString(AppConstants.USER_CODE, ""));
            acc_type.setText("Type  : " + selectedAccount.getType());
            customer_name.setText("Name  : " + selectedAccount.getName());
            acc_no.setText("Acc No  : " + selectedAccount.getAccNo());
            prev_balance.setText("Previous  : " + prevAmount);
            deposit_amount.setText("Deposit  : " + depositAmount);
            total_amount.setText("Total  : " + totalAmount);

            confirm_button.setOnClickListener(view1 -> {
                dialog.dismiss();
                saveTransaction(depositAmount, totalAmount, selectedAccount);
                Toast.makeText(requireActivity(), "Saved Successfully", Toast.LENGTH_LONG).show();
                clearForm();
                initGUI();
//                NavHostFragment.findNavController(this).getPreviousBackStackEntry().getSavedStateHandle().set("key", "clear");
            });

            dialog.show();
        }
    }

    private void saveTransaction(double depositAmount, double totalAmount, Account selectedAccount) {

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        CollectionReference transactionCollectionReference = firebaseFirestore.collection(AppConstants.TRANSACTION_COLLECTION);
        CollectionReference accountCollectionReference = firebaseFirestore.collection(AppConstants.ACCOUNT_COLLECTION);

        String today = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH).format(new Date());

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

        StringBuilder builder = new StringBuilder();
        builder.append("Date: ").append(today);
        builder.append("\nAcc No: ").append(selectedAccount.getAccNo());
        builder.append("\nAcc Type: ").append(selectedAccount.getType());
        builder.append("\nAmount Paid: ").append(depositAmount);
        builder.append("\nPrevious Amount: ").append(selectedAccount.getPrevAmount());
        builder.append("\nTotal Amount: ").append(depositAmount + selectedAccount.getPrevAmount());
        builder.append("\nAgent Name: ").append(sharedPreferences.getString(AppConstants.USER_NAME, ""));

        if (selectedAccount.getPhoneNumber() != null && !selectedAccount.getPhoneNumber().trim().isEmpty()) {
            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(selectedAccount.getPhoneNumber(), null, builder.toString(), null, null);
                Toast.makeText(getActivity(), "Message Sent", Toast.LENGTH_LONG).show();
            } catch (Exception ex) {
                Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_LONG).show();
                ex.printStackTrace();
            }
        }
    }

    /* private void saveRecord(View view) {
        final String customerNameAuto = binding.avNameAccNo.getText().toString();
        final String name = binding.tvAmount.getText().toString();

        if (customerNameAuto.length() == 0) {
            binding.avNameAccNo.requestFocus();
            binding.avNameAccNo.setError("FIELD CANNOT BE EMPTY");
        } else if (name.length() == 0) {
            binding.tvAmount.requestFocus();
            binding.tvAmount.setError("FIELD CANNOT BE EMPTY");
        } else {
            *//*if (dataList.isEmpty()) {
                dataList.add("000,12345,0000000000002900.00,00000000000000000009,0000000006,16.02.23,12345");
            }*//*

            //000,+,PIGMY,0001               ,0000000.00,GANGADAR BADIGER    ,0000100.00,27.12.22,0000000.00@
            //000,PIGMY,0249               ,SRINIVASH MADAG     ,0000100.00,16.02.23,00005
            String creditOrDebit = selectedRow.split(",")[1];
            String accType = selectedRow.split(",")[2];
            String accNo = selectedRow.split(",")[3];
            String customerName = selectedRow.split(",")[5];

            String validateCustomer = customerName.trim() + " " + creditOrDebit + " " + accNo.trim();
            if (!customerNameAuto.equals(validateCustomer)) {
                binding.avNameAccNo.requestFocus();
                binding.avNameAccNo.setError("SELECT VALID CUSTOMER");
                return;
            }

            String depositAmountSuffix = String.valueOf(depositAmount);

            String currentAmount = "";
            if (depositAmountSuffix.length() == 9) {
                currentAmount = String.format("%0" + (10 - depositAmountSuffix.length()) + "d%s", 0, depositAmountSuffix);
            } else {
                currentAmount = String.format("%0" + (9 - depositAmountSuffix.length()) + "d%s", 0, depositAmountSuffix);
                currentAmount = currentAmount.split("\\.")[1].length() == 1 ? currentAmount + "0" : currentAmount;
            }

            if (currentAmount.length() == 9) {
                currentAmount = "0" + currentAmount;
            }

            String date = new SimpleDateFormat("dd.MM.yy").format(new Date());
            String dateAndTime = new SimpleDateFormat("dd.MM.yy hh:mm:ss").format(new Date());

            receiptNo++;
            String receipt = String.format("%0" + (5 - String.valueOf(receiptNo).length()) + "d%s", 0, receiptNo);

            dataList.add("000," + accType + "," + accNo + "," + customerName + "," + currentAmount + "," + date + "," + receipt);

            //writing to file...
            new FileUtils().writeToFile(dataList);

            clearForm();

            String s = dataList.get(dataList.size() - 1);
            Bundle bundle = new Bundle();
            bundle.putString("date_time", dateAndTime);
            bundle.putString("agent_code", "12345");
            bundle.putString("acc_type", accType.trim());
            bundle.putString("customer_name", customerName.trim());
            bundle.putString("acc_no", accNo.trim());
            bundle.putString("prev_balance", String.valueOf(prevAmount));
            bundle.putString("deposit_amount", String.valueOf(depositAmount));
            bundle.putString("total_amount", String.valueOf(totalAmount));

            Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_receiptFragment,bundle);

            Toast.makeText(requireActivity(), "Saved Successfully", Toast.LENGTH_LONG).show();
        }
    }*/

    private void onTextChangedForAccountSelection(AutoCompleteTextView autoCompleteTextView) {
        autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            selectedAccount = (Account) parent.getItemAtPosition(position);
            binding.tvAccountType.setText("" + selectedAccount.getType());
            binding.tvAccountType.setFocusable(false);
            prevAmount = selectedAccount.getPrevAmount();
            binding.tvPrevAmount.setText("" + selectedAccount.getPrevAmount());
            binding.tvPrevAmount.setFocusable(false);
            binding.tvTotalAmount.setText("");
            binding.tvAmount.setText("");
        });
    }

    private void onTextChangedForDepositAmt(TextView textView, EditText resultTextView) {
        resultTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isDigitsOnly(s) && s.length() > 0) {
                    depositAmount = Double.parseDouble(s.toString());
                    if (selectedAccount == null) {
                        binding.avNameAccNo.requestFocus();
                        binding.avNameAccNo.setError("SELECT VALID CUSTOMER");
                        return;
                    }
                    totalAmount = selectedAccount.getPlusMinus().equals("+") ? (prevAmount + depositAmount) : (prevAmount - depositAmount);
                    textView.setText(String.valueOf(totalAmount));
                } else {
                    textView.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                binding.tvAmount.requestFocus();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
        });
    }

    private void resetAccTypeAndPrevAmt(AutoCompleteTextView autoCompleteTextView) {
        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    binding.tvPrevAmount.setText("");
                    binding.tvAccountType.setText("");
                    binding.tvTotalAmount.setText("");
                    binding.tvAmount.setText("");
                    prevAmount = 0;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void clearForm() {
        binding.tvPrevAmount.setText("");
        binding.tvAccountType.setText("");
        binding.tvTotalAmount.setText("");
        binding.tvAmount.setText("");
        binding.avNameAccNo.setText("");
        prevAmount = 0;
        selectedAccount = null;
    }

    private void getCustomers() {

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        CollectionReference collectionReference = firebaseFirestore.collection(AppConstants.ACCOUNT_COLLECTION);

        collectionReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                accountList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Log.d("Testing", "onComplete: " + document.getData());
                    Account account = document.toObject(Account.class);
                    accountList.add(account);
                }
                customerArrayAdapter.notifyDataSetChanged();
            }
        });
    }
}