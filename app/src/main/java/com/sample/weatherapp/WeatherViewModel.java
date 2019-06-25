package com.sample.weatherapp;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.sample.weatherapp.model.StatusAwareResponse;
import com.sample.weatherapp.model.Weather;
import com.sample.weatherapp.repo.WeatherRepository;

public class WeatherViewModel extends AndroidViewModel {

    private final WeatherRepository weatherRepository;

    public WeatherViewModel(@NonNull Application application) {
        super(application);
        weatherRepository = WeatherRepository.getInstance();
    }

    public LiveData<StatusAwareResponse<Weather>> getWeatherForecast(double latitude, double longitude, int noOfDays) {
        return weatherRepository.getWeatherForecast(latitude, longitude, noOfDays);
    }
}
