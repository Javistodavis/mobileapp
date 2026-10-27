package com.example.pianar_event_driven_activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UsersActivity extends AppCompatActivity {

    private TextView tvLocal, tvCloud;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        tvLocal = findViewById(R.id.tvLocalUsers);
        tvCloud = findViewById(R.id.tvCloudUsers);
        Button btnBackDashboard = findViewById(R.id.btnBackDashboard);
        Button btnLogout = findViewById(R.id.btnLogout);

        showLocalUsers();
        loadCloudUsers();

        // Back to Dashboard
        btnBackDashboard.setOnClickListener(v -> {
            Intent intent = new Intent(UsersActivity.this, DashboardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        // Back to a fresh login page
        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(UsersActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void showLocalUsers() {
        List<LocalUserStore.LocalUser> users = new LocalUserStore(this).getAll();

        if (users.isEmpty()) {
            tvLocal.setText("No local users yet.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (LocalUserStore.LocalUser u : users) {
            sb.append("Name: ").append(u.firstName).append(" ").append(u.lastName).append("\n");
            sb.append("Email: ").append(u.email).append("\n");
            sb.append("----------------\n");
        }
        tvLocal.setText(sb.toString());
    }

    private void loadCloudUsers() {
        tvCloud.setText("Loading...");

        ApiClient.getApi().getUsers().enqueue(new Callback<UsersResponse>() {
            @Override
            public void onResponse(Call<UsersResponse> call, Response<UsersResponse> response) {
                if (!response.isSuccessful() || response.body() == null
                        || response.body().getData() == null) {
                    tvCloud.setText("Could not load cloud users.");
                    return;
                }

                StringBuilder sb = new StringBuilder();
                for (User u : response.body().getData()) {
                    sb.append("Name: ").append(u.getFirstName()).append(" ")
                            .append(u.getLastName()).append("\n");
                    sb.append("Email: ").append(u.getEmail()).append("\n");
                    sb.append("----------------\n");
                }
                tvCloud.setText(sb.length() == 0 ? "No cloud users." : sb.toString());
            }

            @Override
            public void onFailure(Call<UsersResponse> call, Throwable t) {
                tvCloud.setText("Could not load cloud users: " + t.getLocalizedMessage());
            }
        });
    }
}