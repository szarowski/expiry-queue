package com.expiryqueue.model;

import com.expiryqueue.util.Random;

import static com.expiryqueue.service.ExpiryQueueServiceImpl.MAX_EXPIRY;

public final class DataJsonBuilder {

    private String message = Random.alphaNumeric();
    private Long expiry = (long) Random.intVal(MAX_EXPIRY);

    private DataJsonBuilder() {
    }

    public static DataJsonBuilder dataJsonBuilder() {
        return new DataJsonBuilder();
    }

    public DataJsonBuilder message(String message) {
        this.message = message;
        return this;
    }

    public DataJsonBuilder expiry(Long expiry) {
        this.expiry = expiry;
        return this;
    }

    public DataJson build() {
        return new DataJson(message, expiry);
    }
}
