package com.sample.gojeck.sampleapp.model;

import com.google.gson.annotations.SerializedName;

public class ForecastDay {
    private String date;

    @SerializedName("date_epoch")
    private long dateEpoch;

    private Day day;

    public String getDate() {
        return date;
    }

    public long getDateEpoch() {
        return dateEpoch;
    }

    public Day getDay() {
        return day;
    }
}
