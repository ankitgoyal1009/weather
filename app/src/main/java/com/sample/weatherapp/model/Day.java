package com.sample.weatherapp.model;

import com.google.gson.annotations.SerializedName;

public class Day {
    @SerializedName("avgtemp_c")
    private Double avgtempC;

    public Double getAvgtempC() {
        return avgtempC;
    }

}
