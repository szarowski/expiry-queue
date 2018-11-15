package com.expiryqueue.model.internal;

import com.expiryqueue.util.Random;

import java.time.OffsetDateTime;

import static com.expiryqueue.service.ExpiryQueueServiceImpl.MAX_EXPIRY;
import static com.expiryqueue.service.ExpiryQueueServiceImpl.MIN_EXPIRY;

public final class DataInternalBuilder {
    private String message = Random.alphaNumeric();
    private OffsetDateTime storeTime = Random.pastOffsetDateTime(MIN_EXPIRY);
    private OffsetDateTime expiryTime = Random.futureOffsetDateTime(MAX_EXPIRY);
    private Long remainingSeconds = (long) Random.intVal(MAX_EXPIRY);

    private DataInternalBuilder() {
    }

    public static DataInternalBuilder dataInternalBuilder() {
        return new DataInternalBuilder();
    }

    public DataInternalBuilder message(String message) {
        this.message = message;
        return this;
    }

    public DataInternalBuilder storeTime(OffsetDateTime storeTime) {
        this.storeTime = storeTime;
        return this;
    }

    public DataInternalBuilder expiryTime(OffsetDateTime expiryTime) {
        this.expiryTime = expiryTime;
        return this;
    }

    public DataInternalBuilder remainingSeconds(Long remainingSeconds) {
        this.remainingSeconds = remainingSeconds;
        return this;
    }

    public DataInternal build() {
        return new DataInternal(message, storeTime, expiryTime, remainingSeconds);
    }
}
