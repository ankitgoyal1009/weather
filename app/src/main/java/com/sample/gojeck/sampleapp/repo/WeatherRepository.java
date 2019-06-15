package com.sample.gojeck.sampleapp.repo;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import com.sample.gojeck.sampleapp.enums.Status;
import com.sample.gojeck.sampleapp.model.StatusAwareResponse;
import com.sample.gojeck.sampleapp.model.Weather;
import com.sample.gojeck.sampleapp.network.RetroClient;

import java.util.concurrent.Executor;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherRepository {
    private static WeatherRepository instance;

    private WeatherRepository() {
    }

    public static WeatherRepository getInstance() {
        if (instance == null) {
            instance = new WeatherRepository();
        }
        return instance;
    }

    public LiveData<StatusAwareResponse<Weather>> getWeatherForecast(final String query, final int forecastDays) {
        final MutableLiveData<StatusAwareResponse<Weather>> weatherResponse = new MutableLiveData<>();

        new Executor() {
            @Override
            public void execute(@NonNull Runnable runnable) {
                runnable.run();
            }
        }.execute(new Runnable() {
            @Override
            public void run() {
                StatusAwareResponse<Weather> loadinResponse = new StatusAwareResponse<>();
                loadinResponse.setStatus(Status.loading);
                weatherResponse.postValue(loadinResponse);
                fetchForecastFromWebService(query, forecastDays, weatherResponse);
            }
        });
        return weatherResponse;
    }

    private void fetchForecastFromWebService(String query, int forecastDays, final MutableLiveData<StatusAwareResponse<Weather>> weatherResponse) {
        Call<Weather> weatherForecast = RetroClient.getWeatherApi().getWeatherForecast(query, forecastDays, "f3f6483879154b8f98c174328191306");
        weatherForecast.enqueue(new Callback<Weather>() {
            @Override
            public void onResponse(Call<Weather> call, Response<Weather> response) {
                //TODO http status and handle error case
                StatusAwareResponse<Weather> succesResponse = new StatusAwareResponse<>();
                succesResponse.setStatus(Status.success);
                succesResponse.setData(response.body());
                weatherResponse.postValue(succesResponse);

            }

            @Override
            public void onFailure(Call<Weather> call, Throwable t) {
                StatusAwareResponse<Weather> failedResponse = new StatusAwareResponse<>();
                failedResponse.setStatus(Status.failed);
                weatherResponse.postValue(failedResponse);
            }
        });
    }

    public LiveData<StatusAwareResponse<Weather>> getWeatherForecast(double lattitude, double longitude, int noOfDays) {
        return getWeatherForecast(lattitude + "," + longitude, noOfDays);
    }
}
                                