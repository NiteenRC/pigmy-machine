package com.example.pigmy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class CustomerReceipt extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_receipt);

        Intent intent = getIntent();
        extracted(intent, "date_time", R.id.date_time, "Date & Time: %s");
        extracted(intent, "agent_code", R.id.agent_code, "Agent Code : %s");
        extracted(intent, "acc_type", R.id.acc_type, "Acc Type     : %s");
        extracted(intent, "customer_name", R.id.customer_name, "Cust name  : %s");
        extracted(intent, "acc_no", R.id.acc_no, "Acc No        : %s");
        extracted(intent, "prev_balance", R.id.prev_balance, "Prev Amt    : %s");
        extracted(intent, "deposit_amount", R.id.deposit_amount, "Dept Amt    : %s");
        extracted(intent, "total_amount", R.id.total_amount, "Total Amt   : %s");
    }

    private void extracted(Intent intent, String key, int textType, String label) {
        key = intent.getStringExtra(key);
        TextView accTypeTextView = findViewById(textType);
        accTypeTextView.setText(String.format(label, key));
    }

    public void onPrintClick(View view) {

    }
}
