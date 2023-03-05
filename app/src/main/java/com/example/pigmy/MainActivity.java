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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

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
import java.util.Arrays;
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

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView textView = findViewById(R.id.total_amount);
        EditText resultTextView = findViewById(R.id.number2_edit_text);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            askPermission();
        }
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

            //Need to check permission
            final AutoCompleteTextView accountTypeView = findViewById(R.id.accountType);
            ArrayAdapter<String> accountTypeViewAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, Arrays.asList("PIGMY", "LOAN"));
            accountTypeView.setAdapter(accountTypeViewAdapter);
            accountTypeView.setThreshold(0);

            List<String> list = readDataExternal();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                list.forEach(x -> map.put(x.split(",")[5].trim() + " " + x.split(",")[3].trim(), x));
                List<String> accNos = new ArrayList<>(map.keySet());
                final AutoCompleteTextView autoCompleteTextView = findViewById(R.id.autoCompleteTextView1);
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, accNos);
                autoCompleteTextView.setAdapter(arrayAdapter);
                autoCompleteTextView.setThreshold(0);

                //AtomicReference<Double> prevAmount = new AtomicReference<>((double) 0);
                autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
                    String selectedField = (String) parent.getItemAtPosition(position);

                    selectedRow = map.get(selectedField);
                    assert selectedRow != null;
                    //prevAmount.set(Double.parseDouble(selectedRow.split(",")[6]));
                    prevAmount = Double.parseDouble(selectedRow.split(",")[6]);
                    EditText editText = findViewById(R.id.number1_edit_text);
                    editText.setText(String.valueOf(prevAmount));
                    editText.setFocusable(false);

                    textView.setText("");
                    resultTextView.setText("");
                });

                resultTextView.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (isDigitsOnly(s) && s.length() > 0) {
                            depositAmount = Double.parseDouble(s.toString());
                            totalAmount = depositAmount + prevAmount;
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
        }
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

    public void onAddButtonClick(View view) throws IOException {
        Path path = createFile("receive.txt");
        if (list.isEmpty()) {
            list.add("000,12345,0000000000002900.00,00000000000000000009,0000000006,16.02.23,12345");
        }

        //000,+,PIGMY,0001               ,0000000.00,GANGADAR BADIGER    ,0000100.00,27.12.22,0000000.00@
        //000,PIGMY,0249               ,SRINIVASH MADAG     ,0000100.00,16.02.23,00005
        String accType = selectedRow.split(",")[2];
        String accNo = selectedRow.split(",")[3];
        String customerName = selectedRow.split(",")[5];

        String prevAmountSuffix = String.valueOf(totalAmount);

        String currentAmount = "";
        if (prevAmountSuffix.length() == 9) {
            currentAmount = String.format("%0" + (10 - prevAmountSuffix.length()) + "d%s", 0, prevAmountSuffix);
        } else {
            currentAmount = String.format("%0" + (9 - prevAmountSuffix.length()) + "d%s", 0, prevAmountSuffix);
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
            Files.write(path, list, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
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