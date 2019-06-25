package com.sample.weatherapp.model;

import com.google.gson.annotations.SerializedName;

public class Current {
    @SerializedName("last_updated_epoch")
    private long lastUpdatedEpoch;

    @SerializedName("last_updated")
    private String lastUpdated;

    @SerializedName("temp_c")
    private Double tempC;

    public long getLastUpdatedEpoch() {
        return lastUpdatedEpoch;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public Double getTempC() {
        return tempC;
    }

}
