package com.sample.gojeck.sampleapp.model;

public class Weather {
private Forecast forecast;
private Current current;
private Location location;

    public Forecast getForecast() {
        return forecast;
    }

    public Current getCurrent() {
        return current;
    }

    public Location getLocation() {
        return location;
    }
}
