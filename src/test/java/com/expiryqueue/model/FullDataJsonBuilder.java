package com.expiryqueue.model;

import com.expiryqueue.util.Random;

import java.time.format.DateTimeFormatter;

public final class FullDataJsonBuilder {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private String message = Random.alphaNumeric();
    private String storeTime = Random.pastOffsetDateTime(10).format(DATE_TIME_FORMATTER);
    private String expiryTime = Random.futureOffsetDateTime(10).format(DATE_TIME_FORMATTER);
    private String remainingSeconds = String.valueOf(Random.intVal());

    private FullDataJsonBuilder() {
    }

    public static FullDataJsonBuilder fullDataJsonBuilder() {
        return new FullDataJsonBuilder();
    }

    public FullDataJsonBuilder message(String message) {
        this.message = message;
        return this;
    }

    public FullDataJsonBuilder storeTime(String storeTime) {
        this.storeTime = storeTime;
        return this;
    }

    public FullDataJsonBuilder expiryTime(String expiryTime) {
        this.expiryTime = expiryTime;
        return this;
    }

    public FullDataJsonBuilder remainingSeconds(String remainingSeconds) {
        this.remainingSeconds = remainingSeconds;
        return this;
    }

    public FullDataJson build() {
        return new FullDataJson(message, storeTime, expiryTime, remainingSeconds);
    }
}
