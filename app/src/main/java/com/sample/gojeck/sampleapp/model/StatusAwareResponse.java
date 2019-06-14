package com.sample.gojeck.sampleapp.model;

import com.sample.gojeck.sampleapp.enums.Status;

public class StatusAwareResponse<T> {
    Status status;
    T data;

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
}
