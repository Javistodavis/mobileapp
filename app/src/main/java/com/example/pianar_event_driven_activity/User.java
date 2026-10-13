package com.example.pianar_event_driven_activity;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("FirstName") private String firstName;
    @SerializedName("LastName") private String lastName;
    @SerializedName("Email") private String email;
    @SerializedName("Password") private String password;

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
}