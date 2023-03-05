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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    private final Map<String, String> map = new HashMap<>();
    private final List<String> list = new ArrayList<>();

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
            accountTypeView.setThreshold(1);

            List<String> list = readDataExternal();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                list.forEach(x -> map.put(x.split(",")[5].trim() + " " + x.split(",")[3].trim(), x));
                List<String> accNos = new ArrayList<>(map.keySet());
                final AutoCompleteTextView autoCompleteTextView = findViewById(R.id.autoCompleteTextView1);
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, accNos);
                autoCompleteTextView.setAdapter(arrayAdapter);
                autoCompleteTextView.setThreshold(1);

                AtomicReference<Double> prevAmount = new AtomicReference<>((double) 0);
                autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
                    String selectedField = (String) parent.getItemAtPosition(position);

                    String value = map.get(selectedField);
                    assert value != null;
                    prevAmount.set(Double.parseDouble(value.split(",")[6]));
                    EditText editText = findViewById(R.id.number1_edit_text);
                    editText.setText(String.valueOf(prevAmount.get()));
                    editText.setFocusable(false);

                });

                EditText resultTextView = findViewById(R.id.number2_edit_text);
                resultTextView.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        TextView textView = findViewById(R.id.total_amount);
                        if (isDigitsOnly(s) && s.length() > 0) {
                            double totalAmount = Double.parseDouble(s.toString()) + prevAmount.get();
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
        list.add("");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Files.write(path, list, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        }

        String s = list.get(list.size()-1);
        Intent intent = new Intent(MainActivity.this, CustomerReceipt.class);
        //intent.putExtra("total_amount", s);
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