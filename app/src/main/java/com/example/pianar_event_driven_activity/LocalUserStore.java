package com.example.pianar_event_driven_activity;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/** Saves accounts created in the app on the phone (SharedPreferences). */
public class LocalUserStore {

    private static final String PREFS = "local_users_v2";
    private static final String KEY_USERS = "users_json";

    public static class LocalUser {
        public final String firstName;
        public final String lastName;
        public final String email;
        public final String passwordHash;

        public LocalUser(String firstName, String lastName, String email, String passwordHash) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.passwordHash = passwordHash;
        }
    }

    private final SharedPreferences prefs;

    public LocalUserStore(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public List<LocalUser> getAll() {
        List<LocalUser> list = new ArrayList<>();
        JSONArray arr = load();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.optJSONObject(i);
            if (o == null) continue;
            list.add(new LocalUser(
                    o.optString("firstName"),
                    o.optString("lastName"),
                    o.optString("email"),
                    o.optString("passwordHash")));
        }
        return list;
    }

    public boolean exists(String email) {
        for (LocalUser u : getAll()) {
            if (u.email.equalsIgnoreCase(email.trim())) return true;
        }
        return false;
    }

    public void save(String firstName, String lastName, String email, String password) {
        JSONArray arr = load();
        try {
            JSONObject o = new JSONObject();
            o.put("firstName", firstName);
            o.put("lastName", lastName);
            o.put("email", email.trim());
            o.put("passwordHash", hash(password));
            arr.put(o);
        } catch (JSONException e) {
            return;
        }
        prefs.edit().putString(KEY_USERS, arr.toString()).apply();
    }

    /** Returns the user if email + password match, otherwise null. */
    public LocalUser login(String email, String password) {
        String h = hash(password);
        for (LocalUser u : getAll()) {
            if (u.email.equalsIgnoreCase(email.trim()) && u.passwordHash.equals(h)) {
                return u;
            }
        }
        return null;
    }

    private JSONArray load() {
        try {
            return new JSONArray(prefs.getString(KEY_USERS, "[]"));
        } catch (JSONException e) {
            return new JSONArray();
        }
    }

    private String hash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}