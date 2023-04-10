package com.example.pigmy;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavGraph;
import androidx.navigation.NavInflater;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.pigmy.databinding.ActivityMainBinding;
import com.example.pigmy.databinding.LayoutProfileDetailsNewBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    private final Map<String, String> map = new HashMap<>();
    private String selectedRow;
    private double totalAmount, prevAmount, depositAmount;
    private int receiptNo;
    private AutoCompleteTextView autoCompleteTextView;

    private TextView textView, accountTypeView, editText;
    private EditText resultTextView;

    private List<String> dataList = new ArrayList<>();

    /*private ActivityResultLauncher<Intent> activityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        if (Environment.isExternalStorageManager())
                        {
                            initGUI();
                        }
                    }
                }
            });*/

    private ActivityMainBinding binding;
    private SharedPreferences sharedPreferences;

    private NavController navController;

    private PopupWindow popupWindow;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPreferences = getSharedPreferences(AppConstants.USER_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        Toolbar toolbar = findViewById(R.id.toolBar);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        NavInflater navInflater = navHostFragment.getNavController().getNavInflater();
        NavGraph navGraph = navInflater.inflate(R.navigation.nav_graph);
        if (sharedPreferences.getBoolean(AppConstants.USER_LOGGED_IN, false)) {
            navGraph.setStartDestination(R.id.homeFragment);
        } else {
            navGraph.setStartDestination(R.id.loginFragment);
        }

        navController = navHostFragment.getNavController();
        navController.setGraph(navGraph);

        NavigationView navView = findViewById(R.id.nav_view);
//        AppBarConfiguration appBarConfiguration = new AppBarConfiguration
//                .Builder(R.id.homeFragment)
//                .setOpenableLayout(binding.drawerLayout)
//                .build();
//
//        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        navView.setNavigationItemSelectedListener(this);

        View headerView = binding.navView.getHeaderView(0);
        TextView tvPersonName = headerView.findViewById(R.id.tvPersonName);

        binding.tvPersonShortName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final PopupWindow popupWindow = new PopupWindow(MainActivity.this);

                LayoutProfileDetailsNewBinding binding = LayoutProfileDetailsNewBinding.inflate(getLayoutInflater());

                binding.tvPersonName.setText(sharedPreferences.getString(AppConstants.USER_NAME, ""));
                binding.tvPersonCode.setText("Code: " + sharedPreferences.getString(AppConstants.USER_CODE, ""));

                popupWindow.setFocusable(true);
                popupWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
                popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
                popupWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                popupWindow.setContentView(binding.getRoot());

                binding.tvSignOut.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        logOut();
                        popupWindow.dismiss();
                    }
                });

                binding.transactionHistory.setOnClickListener(v -> {
                    navigateToTransactionHistory();
                    popupWindow.dismiss();
                });

                popupWindow.showAsDropDown(view, -40, 28);

            }
        });

        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController navController, @NonNull NavDestination navDestination, @Nullable Bundle bundle) {
                if (navDestination.getId() == R.id.transactionHistoryFragment) {
                    binding.title.setText(getString(R.string.transaction_history_fragment_title));
                }
                if (navDestination.getId() == R.id.loginFragment) {
                    toolbar.setVisibility(View.GONE);
                    binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                } else {
                    toolbar.setVisibility(View.VISIBLE);
                    binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                }

                tvPersonName.setText("Hey, " + sharedPreferences.getString(AppConstants.USER_NAME, ""));
                binding.tvPersonShortName.setText(sharedPreferences.getString(AppConstants.USER_SHORT_NAME, ""));
            }
        });
    }

    private void showProfileDetailsDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        LayoutProfileDetailsNewBinding binding = LayoutProfileDetailsNewBinding.inflate(getLayoutInflater());
        builder.setView(binding.getRoot());

        binding.tvPersonName.setText(sharedPreferences.getString(AppConstants.USER_NAME, ""));
        binding.tvPersonCode.setText("Code: " + sharedPreferences.getString(AppConstants.USER_CODE, ""));

        AlertDialog alertDialog = builder.create();
        binding.tvSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logOut();
                alertDialog.dismiss();
            }
        });

        builder.setPositiveButton("OK !", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        WindowManager.LayoutParams wmlp = alertDialog.getWindow().getAttributes();

        wmlp.gravity = Gravity.TOP;
        wmlp.x = 100;   //x position
        wmlp.y = 100;

        alertDialog.show();
    }

   /* @SuppressLint("SetTextI18n")
    private void onTextChangedForAccountSelection(TextView textView, EditText resultTextView, TextView editText, TextView accountTypeView, AutoCompleteTextView autoCompleteTextView) {
        autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedField = (String) parent.getItemAtPosition(position);

            selectedRow = map.get(selectedField);
            assert selectedRow != null;
            accountTypeView.setText("ACC TYPE: " + selectedRow.split(",")[2]);
            assert selectedRow != null;
            prevAmount = Double.parseDouble(selectedRow.split(",")[6]);

            editText.setText("PREV AMT: " + prevAmount);
            editText.setFocusable(false);

            textView.setText("");
            resultTextView.setText("");
        });
    }*/

    /*private void onTextChangedForDepositAmt(TextView textView, EditText resultTextView) {
        resultTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isDigitsOnly(s) && s.length() > 0) {
                    depositAmount = Double.parseDouble(s.toString());

                    if (selectedRow == null) {
                        autoCompleteTextView.requestFocus();
                        autoCompleteTextView.setError("SELECT VALID CUSTOMER");
                        return;
                    }
                    String creditOrDebit = selectedRow.split(",")[1];
                    totalAmount = creditOrDebit.equals("+") ? (prevAmount + depositAmount) : (prevAmount - depositAmount);

                    textView.setText(String.valueOf(totalAmount));
                } else {
                    textView.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
        });
    }

    private void resetAccTypeAndPrevAmt(TextView editText, TextView accountTypeView, AutoCompleteTextView autoCompleteTextView, TextView textView, EditText resultTextView) {
        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    editText.setText("PREV AMT: ");
                    accountTypeView.setText("ACC TYPE: ");
                    textView.setText("");
                    resultTextView.setText("");
                    prevAmount = 0;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted
                initGUI();
            } else {
                // Permission denied
            }
        }
    }*/

    /*private void askPermission() {
        //checking external storage permission is given or not...
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                activityResultLauncher.launch(intent);
            } else {
                initGUI();
            }
        } else if (ContextCompat.checkSelfPermission(MainActivity.this,
                WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{WRITE_EXTERNAL_STORAGE,
                            READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        } else if (ContextCompat.checkSelfPermission(MainActivity.this,
                WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            initGUI();
        }
    }*/

/*
    private void initGUI() {
        textView = findViewById(R.id.total_amount);
        resultTextView = findViewById(R.id.number2_edit_text);

        editText = findViewById(R.id.number1_edit_text);
        editText.setText("PREV AMT: ");

        accountTypeView = findViewById(R.id.accountType);
        accountTypeView.setText("ACC TYPE: ");

        dataList = new FileUtils().readDataExternal();
        for (String item : dataList) {
            map.put(item.split(",")[5].trim() + " " + item.split(",")[1].trim() + " " + item.split(",")[3].trim(), item);
        }

        List<String> accNos = new ArrayList<>(map.keySet());
        autoCompleteTextView = findViewById(R.id.autoCompleteTextView1);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, accNos);
        autoCompleteTextView.setAdapter(arrayAdapter);
        autoCompleteTextView.setThreshold(0);

        resetAccTypeAndPrevAmt(editText, accountTypeView, autoCompleteTextView, textView, resultTextView);
        onTextChangedForAccountSelection(textView, resultTextView, editText, accountTypeView, autoCompleteTextView);
        onTextChangedForDepositAmt(textView, resultTextView);

    }
*/

    @Override
    public void onClick(View view) {
       /* switch (view.getId()){
            case R.id.clear_button:
                clearForm();
                break;
            case R.id.add_button:
                saveRecord();
                break;
        }*/
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.profile) {
            binding.drawerLayout.close();
            showProfileDetailsDialog();
        } else if (item.getItemId() == R.id.logout) {
            binding.drawerLayout.close();
            logOut();
        }
        return false;
    }

    private void logOut() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Log Out");
        builder.setMessage("Are you sure ?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                sharedPreferences.edit().clear().apply();
                navController.navigate(R.id.action_homeFragment_to_loginFragment);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.create().show();
    }

    private void navigateToTransactionHistory() {
        navController.navigate(R.id.action_homeFragment_to_transactionHistoryFragment);
    }

    /*private void saveRecord() {
        final String customerNameAuto = autoCompleteTextView.getText().toString();
        final String name = resultTextView.getText().toString();

        if (customerNameAuto.length() == 0) {
            autoCompleteTextView.requestFocus();
            autoCompleteTextView.setError("FIELD CANNOT BE EMPTY");
        } else if (name.length() == 0) {
            resultTextView.requestFocus();
            resultTextView.setError("FIELD CANNOT BE EMPTY");
        } else {
            if (dataList.isEmpty()) {
                dataList.add("000,12345,0000000000002900.00,00000000000000000009,0000000006,16.02.23,12345");
            }

            //000,+,PIGMY,0001               ,0000000.00,GANGADAR BADIGER    ,0000100.00,27.12.22,0000000.00@
            //000,PIGMY,0249               ,SRINIVASH MADAG     ,0000100.00,16.02.23,00005
            String creditOrDebit = selectedRow.split(",")[1];
            String accType = selectedRow.split(",")[2];
            String accNo = selectedRow.split(",")[3];
            String customerName = selectedRow.split(",")[5];

            String validateCustomer = customerName.trim() + " " + creditOrDebit + " " + accNo.trim();
            if (!customerNameAuto.equals(validateCustomer)) {
                autoCompleteTextView.requestFocus();
                autoCompleteTextView.setError("SELECT VALID CUSTOMER");
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

            @SuppressLint("SimpleDateFormat") String date = new SimpleDateFormat("dd.MM.yy").format(new Date());
            @SuppressLint("SimpleDateFormat") String dateAndTime = new SimpleDateFormat("dd.MM.yy hh:mm:ss").format(new Date());

            receiptNo++;
            String receipt = String.format("%0" + (5 - String.valueOf(receiptNo).length()) + "d%s", 0, receiptNo);

            dataList.add("000," + accType + "," + accNo + "," + customerName + "," + currentAmount + "," + date + "," + receipt);

            //writing to file...
            new FileUtils().writeToFile(dataList);

            clearForm();

            String s = dataList.get(dataList.size() - 1);
            Intent intent = new Intent(MainActivity.this, CustomerReceipt.class);
            intent.putExtra("date_time", dateAndTime);
            intent.putExtra("agent_code", "12345");
            intent.putExtra("acc_type", accType.trim());
            intent.putExtra("customer_name", customerName.trim());
            intent.putExtra("acc_no", accNo.trim());
            intent.putExtra("prev_balance", String.valueOf(prevAmount));
            intent.putExtra("deposit_amount", String.valueOf(depositAmount));
            intent.putExtra("total_amount", String.valueOf(totalAmount));
            startActivity(intent);

            Toast.makeText(MainActivity.this, "Saved Successfully", Toast.LENGTH_LONG).show();
        }
    }

    private void clearForm() {
        editText.setText("PREV AMT: ");
        accountTypeView.setText("ACC TYPE: ");
        textView.setText("");
        resultTextView.setText("");
        autoCompleteTextView.setText("");
        prevAmount = 0;
    }*/
}