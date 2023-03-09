package com.example.pigmy;

import static android.text.TextUtils.isDigitsOnly;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    private final Map<String, String> map = new HashMap<>();
    private final List<String> list = new ArrayList<>();
    private String selectedRow;
    private double totalAmount;
    private int receiptNo;
    private double prevAmount;
    private double depositAmount;
    private AutoCompleteTextView autoCompleteTextView;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView textView = findViewById(R.id.total_amount);
        EditText resultTextView = findViewById(R.id.number2_edit_text);

        TextView editText = findViewById(R.id.number1_edit_text);
        editText.setText("Prev Amt: ");

        final TextView accountTypeView = findViewById(R.id.accountType);
        accountTypeView.setText("Acc Type: ");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            askPermission();
        }
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

            //Need to check permission
            List<String> list = readDataExternal();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                list.forEach(x -> map.put(x.split(",")[5].trim() + " " + x.split(",")[1].trim() + " " + x.split(",")[3].trim(), x));
                List<String> accNos = new ArrayList<>(map.keySet());
                autoCompleteTextView = findViewById(R.id.autoCompleteTextView1);
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, accNos);
                autoCompleteTextView.setAdapter(arrayAdapter);
                autoCompleteTextView.setThreshold(0);

                resetAccTypeAndPrevAmt(editText, accountTypeView, autoCompleteTextView, textView, resultTextView);
                onTextChangedForAccountSelection(textView, resultTextView, editText, accountTypeView, autoCompleteTextView);
                onTextChangedForDepositAmt(textView, resultTextView);

                Button btn = findViewById(R.id.clear_button);
                btn.setOnClickListener(v -> {
                    editText.setText("Prev Amt: ");
                    accountTypeView.setText("Acc Type: ");
                    textView.setText("");
                    resultTextView.setText("");
                    autoCompleteTextView.setText("");
                    prevAmount = 0;
                });

                Button addButton = findViewById(R.id.add_button);
                addButton.setOnClickListener(view -> {
                    final String customerNameAuto = autoCompleteTextView.getText().toString();
                    final String name = resultTextView.getText().toString();

                    if (customerNameAuto.length() == 0) {
                        autoCompleteTextView.requestFocus();
                        autoCompleteTextView.setError("FIELD CANNOT BE EMPTY");
                    } else if (name.length() == 0) {
                        resultTextView.requestFocus();
                        resultTextView.setError("FIELD CANNOT BE EMPTY");
                    } else {
                        Path path;
                        try {
                            path = createFile("receive.txt");
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        if (list.isEmpty()) {
                            list.add("000,12345,0000000000002900.00,00000000000000000009,0000000006,16.02.23,12345");
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
                            autoCompleteTextView.setError("Select valid customer");
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

                        list.add("000," + accType + "," + accNo + "," + customerName + "," + currentAmount + "," + date + "," + receipt);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            try {
                                Files.write(path, list, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }

                        String s = list.get(list.size() - 1);
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
                        Toast.makeText(MainActivity.this, "Saved Successful", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void onTextChangedForAccountSelection(TextView textView, EditText resultTextView, TextView editText, TextView accountTypeView, AutoCompleteTextView autoCompleteTextView) {
        autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedField = (String) parent.getItemAtPosition(position);

            selectedRow = map.get(selectedField);
            assert selectedRow != null;
            accountTypeView.setText("Acc Type: " + selectedRow.split(",")[2]);
            assert selectedRow != null;
            prevAmount = Double.parseDouble(selectedRow.split(",")[6]);

            editText.setText("Prev Amt: " + prevAmount);
            editText.setFocusable(false);

            textView.setText("");
            resultTextView.setText("");
        });
    }

    private void onTextChangedForDepositAmt(TextView textView, EditText resultTextView) {
        resultTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isDigitsOnly(s) && s.length() > 0) {
                    depositAmount = Double.parseDouble(s.toString());

                    if (selectedRow == null) {
                        autoCompleteTextView.requestFocus();
                        autoCompleteTextView.setError("Select valid customer");
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
                    editText.setText("Prev Amt: ");
                    accountTypeView.setText("Acc Type: ");
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

    private List<String> readDataExternal() {
        try {
            File downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                Path path = Paths.get(downloadPath + "/send.txt");
                return Files.readAllLines(path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted
                readDataExternal();
            } else {
                // Permission denied
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void askPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
    }

    private Path createFile(String fileName) throws IOException {
        Path path = null;
        File downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            path = Paths.get(downloadPath + "/" + fileName);
            Files.deleteIfExists(path);

            if (!Files.exists(path)) {
                try {
                    Files.createFile(path);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return path;
    }
}