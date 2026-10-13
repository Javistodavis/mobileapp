package com.example.pianar_event_driven_activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progress = findViewById(R.id.progress);

        btnLogin.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        // 1. Local validation
        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            return;
        }
        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            return;
        }

        // 2. Validate against the API
        setLoading(true);
        ApiClient.getApi().getUsers().enqueue(new Callback<UsersResponse>() {
            @Override
            public void onResponse(Call<UsersResponse> call, Response<UsersResponse> response) {
                setLoading(false);

                if (!response.isSuccessful() || response.body() == null
                        || response.body().getData() == null) {
                    Toast.makeText(LoginActivity.this,
                            "Server error. Try again.", Toast.LENGTH_SHORT).show();
                    return;
                }

                User matched = null;
                for (User u : response.body().getData()) {
                    if (u.getEmail().equalsIgnoreCase(email)
                            && u.getPassword().equals(password)) {
                        matched = u;
                        break;
                    }
                }

                if (matched != null) {
                    Toast.makeText(LoginActivity.this,
                            "Welcome " + matched.getFirstName() + " " + matched.getLastName() + "!",
                            Toast.LENGTH_SHORT).show();

                    // TODO: change MainActivity to the screen you want after login
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("firstName", matched.getFirstName());
                    intent.putExtra("lastName", matched.getLastName());
                    intent.putExtra("email", matched.getEmail());
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this,
                            "Invalid email or password", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UsersResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(LoginActivity.this,
                        "Network error: " + t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
    }
}
