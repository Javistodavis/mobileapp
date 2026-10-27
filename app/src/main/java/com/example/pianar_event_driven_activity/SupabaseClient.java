package com.example.pianar_event_driven_activity;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SupabaseClient {

    public static final String BASE_URL = "https://nobheeqnozkwzzqiqaft.supabase.co/";

    public static final String API_KEY = "sb_publishable_hpPVsy7BD5JKZ3TayG3hDA_HzCnLNvr";

    private static SupabaseApi api;

    public static SupabaseApi getApi() {
        if (api == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(chain -> {
                        Request.Builder builder = chain.request().newBuilder()
                                .header("apikey", API_KEY)
                                .header("Accept", "application/json");
                        // Old-style JWT keys (start with "eyJ") also go in Authorization.
                        // New sb_publishable_ keys only use the apikey header.
                        if (API_KEY.startsWith("eyJ")) {
                            builder.header("Authorization", "Bearer " + API_KEY);
                        }
                        return chain.proceed(builder.build());
                    })
                    .build();

            api = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(SupabaseApi.class);
        }
        return api;
    }
}