package com.sample.weatherapp.weatherapp;

import org.junit.Test;

public class MainActivityTest {
    @Test
    public void check_day_isCorrect() {
        // This will check if day returned by dayForDate() is correct.
    }

    @Test
    public void check_error_message_isCorrect() {
        // This will check if error message returned by getErrorMessage() is correct
    }

    @Test
    public void check_live_data_is_updating_state() {
        // This method will call fetchWeatherData() function and checks if live data is changing the state from none->loading.
    }
}
