package com.sample.gojeck.sampleapp.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetroClient {
    private static final String BASE_URL = "http://api.apixu.com/";
    private static Retrofit retrofit;

    public static Retrofit getRetrofitInstance() {
        if (retrofit != null) {
            return retrofit;
        }
        Gson gson =
                new GsonBuilder()
                        .setLenient()
                        .create();

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        return retrofit;
    }

    public static WeatherApi getWeatherApi() {
        return getRetrofitInstance().create(WeatherApi.class);
    }

}
