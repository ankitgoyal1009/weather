package com.sample.gojeck.sampleapp.repo;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.util.Log;

import com.sample.gojeck.sampleapp.enums.Status;
import com.sample.gojeck.sampleapp.model.Error;
import com.sample.gojeck.sampleapp.model.StatusAwareResponse;
import com.sample.gojeck.sampleapp.model.Weather;
import com.sample.gojeck.sampleapp.network.RetroClient;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.concurrent.Executor;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
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
                StatusAwareResponse<Weather> loadingResponse = new StatusAwareResponse<>();
                loadingResponse.setStatus(Status.loading);
                weatherResponse.postValue(loadingResponse);
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
                if (response.errorBody() != null) {
                    Converter<ResponseBody, Error> errorConverter =
                            RetroClient.getRetrofitInstance().responseBodyConverter(Error.class, new Annotation[0]);
                    Error error;
                    try {
                        error = errorConverter.convert(response.errorBody());
                    } catch (IOException e) {
                        Log.e("WeatherRepository", "IOException while parsing error body");
                        onFailure(call, e);
                        return;
                    }

                    StatusAwareResponse<Weather> failureResponse = new StatusAwareResponse<>();
                    failureResponse.setStatus(Status.failed);
                    failureResponse.setError(error);
                    weatherResponse.postValue(failureResponse);
                    return;
                }

                StatusAwareResponse<Weather> successResponse = new StatusAwareResponse<>();
                successResponse.setStatus(Status.success);
                successResponse.setData(response.body());
                weatherResponse.postValue(successResponse);
            }

            @Override
            public void onFailure(Call<Weather> call, Throwable t) {
                StatusAwareResponse<Weather> failedResponse = new StatusAwareResponse<>();
                failedResponse.setStatus(Status.failed);
                weatherResponse.postValue(failedResponse);
            }
        });
    }

    public LiveData<StatusAwareResponse<Weather>> getWeatherForecast(double latitude, double longitude, int noOfDays) {
        return getWeatherForecast(latitude + "," + longitude, noOfDays);
    }
}