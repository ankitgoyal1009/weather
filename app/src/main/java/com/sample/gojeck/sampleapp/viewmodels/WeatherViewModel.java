package com.sample.gojeck.sampleapp.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.sample.gojeck.sampleapp.model.StatusAwareResponse;
import com.sample.gojeck.sampleapp.model.Weather;
import com.sample.gojeck.sampleapp.repo.WeatherRepository;

public class WeatherViewModel extends AndroidViewModel {

    private final WeatherRepository weatherRepository;

    public WeatherViewModel(@NonNull Application application) {
        super(application);
        weatherRepository = WeatherRepository.getInstance();
    }

    public LiveData<StatusAwareResponse<Weather>> getWetherForecast(double latitude, double longitude, int noOfDays) {
        return weatherRepository.getWeatherForecast(latitude, longitude, noOfDays);
    }
}
