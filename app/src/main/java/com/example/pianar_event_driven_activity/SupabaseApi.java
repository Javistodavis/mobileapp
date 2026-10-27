package com.example.pianar_event_driven_activity;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface SupabaseApi {

    // https://nobheeqnozkwzzqiqaft.supabase.co/rest/v1/customer?select=*
    @GET("rest/v1/customer")
    Call<JsonArray> getCustomers(@Query("select") String select);

    // Insert a new customer row
    @Headers("Prefer: return=minimal")
    @POST("rest/v1/customer")
    Call<Void> addCustomer(@Body JsonObject customer);
}