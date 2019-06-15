package com.sample.gojeck.sampleapp.network;

import com.sample.gojeck.sampleapp.model.Weather;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApi {
    @GET("/v1/forecast.json")
    Call<Weather> getWeatherForecast(@Query("q") String query, @Query("days") int days, @Query("key") String key);

}
