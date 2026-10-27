package com.example.pianar_event_driven_activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Add Customer form + Customer List (Supabase "customer" table). */
public class CustomerActivity extends AppCompatActivity {

    // Possible column names in your Supabase table. The first name in each list is the
    // default; the app also detects the real names from the rows that already exist.
    private static final String[] COL_ID = {"customer_id", "customerid", "customer_no", "id"};
    private static final String[] COL_LAST = {"last_name", "lastname", "surname"};
    private static final String[] COL_FIRST = {"first_name", "firstname"};
    private static final String[] COL_MOBILE = {"mobile_no", "mobileno", "mobile",
            "mobile_number", "contact_no", "phone"};
    private static final String[] COL_ADDRESS = {"address"};

    // Column names actually used (resolved after the list loads)
    private String keyId = COL_ID[0];
    private String keyLast = COL_LAST[0];
    private String keyFirst = COL_FIRST[0];
    private String keyMobile = COL_MOBILE[0];
    private String keyAddress = COL_ADDRESS[0];

    private EditText etCustomerId, etLastName, etFirstName, etMobile, etAddress;
    private Button btnSave;
    private ProgressBar progress;
    private TextView tvMessage;
    private LinearLayout llCustomers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer);

        etCustomerId = findViewById(R.id.etCustomerId);
        etLastName = findViewById(R.id.etLastName);
        etFirstName = findViewById(R.id.etFirstName);
        etMobile = findViewById(R.id.etMobile);
        etAddress = findViewById(R.id.etAddress);
        btnSave = findViewById(R.id.btnSave);
        progress = findViewById(R.id.progress);
        tvMessage = findViewById(R.id.tvMessage);
        llCustomers = findViewById(R.id.llCustomers);
        Button btnBack = findViewById(R.id.btnBack);

        btnSave.setOnClickListener(v -> saveCustomer());
        btnBack.setOnClickListener(v -> finish());

        loadCustomers();
    }

    // ------------------------------------------------------------------ load list

    private void loadCustomers() {
        progress.setVisibility(View.VISIBLE);
        tvMessage.setVisibility(View.GONE);

        SupabaseClient.getApi().getCustomers("*").enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                progress.setVisibility(View.GONE);

                if (!response.isSuccessful() || response.body() == null) {
                    llCustomers.removeAllViews();
                    showMessage("Could not load customers (HTTP " + response.code() + ")\n"
                            + errorText(response));
                    return;
                }

                List<JsonObject> rows = new ArrayList<>();
                for (JsonElement element : response.body()) {
                    if (element.isJsonObject()) rows.add(element.getAsJsonObject());
                }

                if (rows.isEmpty()) {
                    llCustomers.removeAllViews();
                    showMessage("No customers yet.");
                    return;
                }

                detectColumns(rows.get(0));
                sortRows(rows);
                showRows(rows);
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                progress.setVisibility(View.GONE);
                llCustomers.removeAllViews();
                showMessage("Could not load customers: " + t.getLocalizedMessage());
            }
        });
    }

    private void showRows(List<JsonObject> rows) {
        llCustomers.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        int number = 1;

        for (JsonObject row : rows) {
            View item = inflater.inflate(R.layout.item_customer, llCustomers, false);
            TextView tvTitle = item.findViewById(R.id.tvTitle);
            TextView tvSub = item.findViewById(R.id.tvSub);

            String id = str(row, keyId);
            if (id.isEmpty()) id = String.valueOf(number);
            String last = str(row, keyLast);
            String first = str(row, keyFirst);
            String mobile = str(row, keyMobile);
            String address = str(row, keyAddress);

            // "1 • Galleto, Bea"
            tvTitle.setText(id + " \u2022 " + last + ", " + first);

            // "09171234567 · Cebu City"
            String sub = mobile;
            if (!address.isEmpty()) {
                sub = sub.isEmpty() ? address : sub + " \u00B7 " + address;
            }
            tvSub.setText(sub);

            llCustomers.addView(item);
            number++;
        }
    }

    // ------------------------------------------------------------------ save

    private void saveCustomer() {
        String id = etCustomerId.getText().toString().trim();
        String last = etLastName.getText().toString().trim();
        String first = etFirstName.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (TextUtils.isEmpty(id)) {
            etCustomerId.setError("Customer ID is required");
            etCustomerId.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(last)) {
            etLastName.setError("Last name is required");
            etLastName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(first)) {
            etFirstName.setError("First name is required");
            etFirstName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(mobile)) {
            etMobile.setError("Mobile No. is required");
            etMobile.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(address)) {
            etAddress.setError("Address is required");
            etAddress.requestFocus();
            return;
        }

        JsonObject body = new JsonObject();
        body.addProperty(keyId, id);
        body.addProperty(keyLast, last);
        body.addProperty(keyFirst, first);
        body.addProperty(keyMobile, mobile);
        body.addProperty(keyAddress, address);

        btnSave.setEnabled(false);

        SupabaseClient.getApi().addCustomer(body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                btnSave.setEnabled(true);

                if (response.isSuccessful()) {
                    Toast.makeText(CustomerActivity.this, "Customer saved!",
                            Toast.LENGTH_SHORT).show();
                    etCustomerId.setText("");
                    etLastName.setText("");
                    etFirstName.setText("");
                    etMobile.setText("");
                    etAddress.setText("");
                    etCustomerId.requestFocus();
                    loadCustomers();
                } else {
                    showMessage("Could not save customer (HTTP " + response.code() + ")\n"
                            + errorText(response));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                btnSave.setEnabled(true);
                showMessage("Could not save customer: " + t.getLocalizedMessage());
            }
        });
    }

    // ------------------------------------------------------------------ helpers

    /** Finds the real column names from an existing row of the table. */
    private void detectColumns(JsonObject sample) {
        keyId = resolveKey(sample, COL_ID);
        keyLast = resolveKey(sample, COL_LAST);
        keyFirst = resolveKey(sample, COL_FIRST);
        keyMobile = resolveKey(sample, COL_MOBILE);
        keyAddress = resolveKey(sample, COL_ADDRESS);
    }

    private static String resolveKey(JsonObject sample, String[] candidates) {
        for (String candidate : candidates) {
            String wanted = normalize(candidate);
            for (String actual : sample.keySet()) {
                if (normalize(actual).equals(wanted)) return actual;
            }
        }
        return candidates[0];
    }

    private static String normalize(String s) {
        return s.toLowerCase(Locale.US).replaceAll("[^a-z0-9]", "");
    }

    private void sortRows(List<JsonObject> rows) {
        boolean allNumeric = true;
        for (JsonObject row : rows) {
            try {
                Long.parseLong(str(row, keyId).trim());
            } catch (NumberFormatException e) {
                allNumeric = false;
                break;
            }
        }

        final boolean numeric = allNumeric;
        Collections.sort(rows, (a, b) -> {
            String x = str(a, keyId).trim();
            String y = str(b, keyId).trim();
            if (numeric) return Long.compare(Long.parseLong(x), Long.parseLong(y));
            return x.compareToIgnoreCase(y);
        });
    }

    private static String str(JsonObject row, String key) {
        JsonElement element = row.get(key);
        if (element == null || element.isJsonNull()) return "";
        return element.isJsonPrimitive() ? element.getAsString() : element.toString();
    }

    private static String errorText(Response<?> response) {
        try {
            if (response.errorBody() != null) return response.errorBody().string();
        } catch (IOException ignored) {
        }
        return "";
    }

    private void showMessage(String message) {
        tvMessage.setText(message);
        tvMessage.setVisibility(View.VISIBLE);
    }
}