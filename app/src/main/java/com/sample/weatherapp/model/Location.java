package com.sample.weatherapp.model;

public class Location {
    private String name;
    private String country;
    private Integer localtimeEpoch;
    private String localtime;

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }

    public Integer getLocaltimeEpoch() {
        return localtimeEpoch;
    }

    public String getLocaltime() {
        return localtime;
    }
}
