package com.example.pigmy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class CustomerReceipt extends AppCompatActivity {
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_receipt);

        textView = findViewById(R.id.total_amount);
        Intent intent = getIntent();
        String str = intent.getStringExtra("total_amount");
        textView.setText(str);
    }

    public void onPrintClick(View view) {

    }
}
