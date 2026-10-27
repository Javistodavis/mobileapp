package com.example.pianar_event_driven_activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Button btnCustomer = findViewById(R.id.btnCustomer);
        Button btnUserList = findViewById(R.id.btnUserList);
        Button btnLogout = findViewById(R.id.btnLogout);

        // Customer List -> Add Customer + Customer List screen
        btnCustomer.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, CustomerActivity.class)));

        // Users List -> All Registered Users screen
        btnUserList.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, UsersActivity.class)));

        // Log Out -> fresh login page
        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}