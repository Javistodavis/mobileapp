package com.example.pianar_event_driven_activity;

import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static ApiService api;

    public static ApiService getApi() {
        if (api == null) {
            // Render free servers sleep, so the first request can take up to ~60s
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build();

            api = new Retrofit.Builder()
                    .baseUrl("https://user-api-pianar.onrender.com/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(ApiService.class);
        }
        return api;
    }
}
