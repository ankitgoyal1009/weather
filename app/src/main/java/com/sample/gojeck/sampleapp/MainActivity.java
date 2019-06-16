package com.sample.gojeck.sampleapp;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.sample.gojeck.sampleapp.enums.Status;
import com.sample.gojeck.sampleapp.model.BaseError;
import com.sample.gojeck.sampleapp.model.Error;
import com.sample.gojeck.sampleapp.model.ForecastDay;
import com.sample.gojeck.sampleapp.model.StatusAwareResponse;
import com.sample.gojeck.sampleapp.model.Weather;
import com.sample.gojeck.sampleapp.view.CustomTextView;
import com.sample.gojeck.sampleapp.viewmodels.WeatherViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String ERROR_CODE_NO_DATA_AT_LOCATION = "1003";
    private static final String ERROR_CODE_DEFAULT_ERROR = "1000";
    private ProgressBar mProgressBar;
    private RelativeLayout mPositiveContainer;
    private RelativeLayout mNegativeContainer;
    private LinearLayout mForecastContainer;
    private CustomTextView tempForToday;
    private CustomTextView place;
    private Observer<StatusAwareResponse<Weather>> mResponseObserver;
    private WeatherViewModel mWeatherViewModel;

    ///////////////////////////////////////////////////////////////////////////
    // Util methods : start
    ///////////////////////////////////////////////////////////////////////////
    public static String dayForDate(long time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
        //to convert epoch seconds to milies multiplying by 1000
        return dateFormat.format(new Date(time * 1000L));
    }

    private static String getErrorMessage(Context context, Error error) {
        String response = context.getString(R.string.error_something_went_wrong);
        if (error != null) {
            if (ERROR_CODE_NO_DATA_AT_LOCATION.equals(error.getError().getCode())) {
                response = context.getString(R.string.error_no_data_at_location);
            }
        }
        return response;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Util methods : end
    ///////////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgressBar = findViewById(R.id.loading);

        mPositiveContainer = findViewById(R.id.positive_view);
        mNegativeContainer = findViewById(R.id.negative_container);
        mForecastContainer = findViewById(R.id.forecast_container);
        tempForToday = mPositiveContainer.findViewById(R.id.today_temp);
        place = mPositiveContainer.findViewById(R.id.city);

        mWeatherViewModel = ViewModelProviders.of(this).get(WeatherViewModel.class);
        mResponseObserver = new Observer<StatusAwareResponse<Weather>>() {
            @Override
            public void onChanged(@Nullable StatusAwareResponse<Weather> weatherStatusAwareResponse) {
                if (weatherStatusAwareResponse == null) {
                    Log.e(TAG, "response is null in main activity");
                    Error error = new Error();
                    BaseError baseError = new BaseError();
                    baseError.setCode(ERROR_CODE_DEFAULT_ERROR);
                    baseError.setMessage(getString(R.string.error_something_went_wrong));
                    error.setError(baseError);
                    updateView(Status.failed, null, error);
                    return;
                }
                updateView(weatherStatusAwareResponse.getStatus(), weatherStatusAwareResponse.getData(), weatherStatusAwareResponse.getError());
            }
        };
        fetchWeatherData();
    }

    private void fetchWeatherData() {
        mWeatherViewModel.getWeatherForecast(12.9870, 77.987, 4)
                .observe(this, mResponseObserver);
    }

    private void updateView(Status status, Weather data, Error error) {
        switch (status) {
            case failed: {
                mPositiveContainer.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.GONE);
                mNegativeContainer.setVisibility(View.VISIBLE);
                CustomTextView errorView = mNegativeContainer.findViewById(R.id.error_msg);
                errorView.setText(getErrorMessage(this, error));
                break;
            }
            case loading: {
                mNegativeContainer.setVisibility(View.GONE);
                mPositiveContainer.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
                break;
            }
            case success: {
                mPositiveContainer.setVisibility(View.VISIBLE);
                mNegativeContainer.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.GONE);
                updatePositiveView(data);
                mForecastContainer.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_up));
                break;
            }
        }
    }

    private void updatePositiveView(Weather data) {
        if (data != null) {
            tempForToday.setText(String.valueOf(data.getCurrent().getTempC()) + (char) 0x00B0);
            place.setText(data.getLocation().getName());
            LayoutInflater inflater = getLayoutInflater();
            for (ForecastDay day : data.getForecast().getForecastday()) {
                View forecastView = inflater.inflate(R.layout.forecast, null);
                CustomTextView dayCtv = forecastView.findViewById(R.id.day);
                dayCtv.setText(dayForDate(day.getDateEpoch()));

                CustomTextView tempCtv = forecastView.findViewById(R.id.temp);
                tempCtv.setText(getString(R.string.temp_unit_suffice, String.valueOf(day.getDay().getAvgtempC())));
                mForecastContainer.addView(forecastView);
            }
        }
    }

    public void retry(View view) {
        fetchWeatherData();
    }
}
