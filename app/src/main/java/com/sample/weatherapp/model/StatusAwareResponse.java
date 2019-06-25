package com.sample.weatherapp.model;

import com.sample.weatherapp.enums.Status;

public class StatusAwareResponse<T> {
    private Status status;
    private T data;
    private Error error;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }
}
