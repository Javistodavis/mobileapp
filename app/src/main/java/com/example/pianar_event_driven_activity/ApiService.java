package com.example.pianar_event_driven_activity;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET("api/users")
    Call<UsersResponse> getUsers();
}
