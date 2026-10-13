package com.example.pianar_event_driven_activity;

import java.util.List;

public class UsersResponse {
    private boolean success;
    private List<User> data;

    public boolean isSuccess() { return success; }
    public List<User> getData() { return data; }
}
