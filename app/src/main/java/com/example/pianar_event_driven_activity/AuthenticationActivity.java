package com.example.pianar_event_driven_activity;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.CycleInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.security.SecureRandom;
import java.util.Locale;

public class AuthenticationActivity extends AppCompatActivity {

    private static final long CODE_VALIDITY_MS = 30_000L;
    private static final long GO_NEXT_DELAY_MS = 1500L;

    private static final String KEY_CODE = "code";
    private static final String KEY_EXPIRY = "expiry";
    private static final String KEY_VERIFIED = "verified";

    private TextView tvCode, tvTimer, tvStatus;
    private EditText etCode;
    private Button btnVerify, btnResend;

    private final SecureRandom random = new SecureRandom();
    private CountDownTimer timer;
    private final Handler handler = new Handler(Looper.getMainLooper());

    // After success: open the "All Registered Users" screen
    private final Runnable goToUsers = () -> {
        startActivity(new Intent(AuthenticationActivity.this, UsersActivity.class));
        finish();
    };

    private String currentCode;
    private long expiryTimeMs;
    private boolean verified = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        tvCode = findViewById(R.id.tvCode);
        tvTimer = findViewById(R.id.tvTimer);
        tvStatus = findViewById(R.id.tvStatus);
        etCode = findViewById(R.id.etCode);
        btnVerify = findViewById(R.id.btnVerify);
        btnResend = findViewById(R.id.btnResend);

        btnVerify.setOnClickListener(v -> verifyCode());
        btnResend.setOnClickListener(v -> generateNewCode());

        if (savedInstanceState != null && savedInstanceState.getString(KEY_CODE) != null) {
            // Screen rotated: keep same code and remaining time
            currentCode = savedInstanceState.getString(KEY_CODE);
            expiryTimeMs = savedInstanceState.getLong(KEY_EXPIRY);
            verified = savedInstanceState.getBoolean(KEY_VERIFIED);
            tvCode.setText(currentCode);

            if (verified) {
                showSuccessState();
                handler.postDelayed(goToUsers, GO_NEXT_DELAY_MS);
            } else {
                startTimer();
            }
        } else {
            generateNewCode();
        }
    }

    // ---------------- code ----------------

    private void generateNewCode() {
        currentCode = String.format(Locale.US, "%06d", random.nextInt(1_000_000));
        expiryTimeMs = System.currentTimeMillis() + CODE_VALIDITY_MS;

        tvCode.setText(currentCode);
        tvCode.setAlpha(1f);
        etCode.setText("");
        etCode.setEnabled(true);
        hideStatus();
        startTimer();
    }

    private void verifyCode() {
        if (verified) return;

        String entered = etCode.getText().toString().trim();

        if (entered.length() != 6) {
            showError(getString(R.string.auth_enter_six));
            return;
        }

        if (System.currentTimeMillis() >= expiryTimeMs) {
            showError(getString(R.string.auth_code_expired_msg));
            return;
        }

        if (entered.equals(currentCode)) {
            verified = true;
            if (timer != null) timer.cancel();
            showSuccessState();
            Toast.makeText(this, R.string.auth_success, Toast.LENGTH_SHORT).show();
            handler.postDelayed(goToUsers, GO_NEXT_DELAY_MS);
        } else {
            showError(getString(R.string.auth_wrong));
            etCode.setText("");
        }
    }

    // ---------------- timer ----------------

    private void startTimer() {
        if (timer != null) timer.cancel();

        long remaining = expiryTimeMs - System.currentTimeMillis();
        if (remaining <= 0) {
            onCodeExpired();
            return;
        }

        btnVerify.setEnabled(true);
        btnResend.setEnabled(false);   // Resend only after the 30 seconds
        tvCode.setAlpha(1f);

        timer = new CountDownTimer(remaining, 250) {
            @Override
            public void onTick(long msLeft) {
                int secs = (int) Math.ceil(msLeft / 1000.0);
                tvTimer.setText(getString(R.string.auth_expires_in, secs));
                btnResend.setText(getString(R.string.auth_resend_in, secs));
            }

            @Override
            public void onFinish() {
                onCodeExpired();
            }
        }.start();
    }

    private void onCodeExpired() {
        tvTimer.setText(R.string.auth_expired);
        tvCode.setAlpha(0.4f);
        btnVerify.setEnabled(false);
        btnResend.setText(R.string.auth_resend);
        btnResend.setEnabled(true);
    }

    // ---------------- UI ----------------

    private void showError(String message) {
        tvStatus.setTextColor(getColor(R.color.auth_error));
        tvStatus.setText(message);
        tvStatus.setVisibility(View.VISIBLE);

        ObjectAnimator shake = ObjectAnimator.ofFloat(etCode, "translationX", 0f, 20f);
        shake.setInterpolator(new CycleInterpolator(3));
        shake.setDuration(400);
        shake.start();
    }

    private void showSuccessState() {
        tvStatus.setTextColor(getColor(R.color.auth_success));
        tvStatus.setText(R.string.auth_success);
        tvStatus.setVisibility(View.VISIBLE);
        tvTimer.setText("");
        etCode.setEnabled(false);
        btnVerify.setEnabled(false);
        btnResend.setEnabled(false);
        btnResend.setText(R.string.auth_resend);
    }

    private void hideStatus() {
        tvStatus.setVisibility(View.INVISIBLE);
    }

    // ---------------- lifecycle ----------------

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_CODE, currentCode);
        outState.putLong(KEY_EXPIRY, expiryTimeMs);
        outState.putBoolean(KEY_VERIFIED, verified);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) timer.cancel();
        handler.removeCallbacks(goToUsers);
    }
}