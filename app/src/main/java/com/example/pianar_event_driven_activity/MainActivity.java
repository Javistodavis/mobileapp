package com.example.pianar_event_driven_activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin, btnNavSignUp;
    private LocalUserStore localStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        localStore = new LocalUserStore(this);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnNavSignUp = findViewById(R.id.btnNavSignUp);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin();
            }
        });

        // Opens the Create Account screen
        btnNavSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SignupActivity.class));
            }
        });
    }

    private void performLogin() {
        // The "username" field is the user's email
        final String username = etUsername.getText().toString().trim();
        final String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Username is required");
            etUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        // 1. Accounts created inside the app
        LocalUserStore.LocalUser localUser = localStore.login(username, password);
        if (localUser != null) {
            openAuthentication(localUser.firstName, localUser.lastName, localUser.email);
            return;
        }

        // 2. Accounts from the API
        btnLogin.setEnabled(false);
        Toast.makeText(this, "Logging in...", Toast.LENGTH_SHORT).show();

        ApiClient.getApi().getUsers().enqueue(new Callback<UsersResponse>() {
            @Override
            public void onResponse(Call<UsersResponse> call, Response<UsersResponse> response) {
                btnLogin.setEnabled(true);

                if (!response.isSuccessful() || response.body() == null
                        || response.body().getData() == null) {
                    Toast.makeText(MainActivity.this,
                            "Server error. Try again.", Toast.LENGTH_SHORT).show();
                    return;
                }

                User matched = null;
                for (User u : response.body().getData()) {
                    if (u.getEmail() != null && u.getPassword() != null
                            && u.getEmail().equalsIgnoreCase(username)
                            && u.getPassword().equals(password)) {
                        matched = u;
                        break;
                    }
                }

                if (matched == null) {
                    Toast.makeText(MainActivity.this,
                            "Invalid username or password", Toast.LENGTH_SHORT).show();
                    return;
                }

                openAuthentication(matched.getFirstName(), matched.getLastName(),
                        matched.getEmail());
            }

            @Override
            public void onFailure(Call<UsersResponse> call, Throwable t) {
                btnLogin.setEnabled(true);
                Toast.makeText(MainActivity.this,
                        "Network error: " + t.getLocalizedMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    // Login OK -> go to the authentication (verification code) screen
    private void openAuthentication(String firstName, String lastName, String email) {
        Intent intent = new Intent(MainActivity.this, AuthenticationActivity.class);
        intent.putExtra("firstName", firstName);
        intent.putExtra("lastName", lastName);
        intent.putExtra("email", email);
        startActivity(intent);
    }
}