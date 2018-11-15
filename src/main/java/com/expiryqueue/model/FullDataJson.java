package com.expiryqueue.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.StringJoiner;

public final class FullDataJson {

    private final String message;

    private final String storeTime;

    private final String expiryTime;

    private final String remainingSeconds;

    @JsonCreator
    public FullDataJson(final String message, final String storeTime,
                        final String expiryTime, final String remainingSeconds) {
        this.message = message;
        this.storeTime = storeTime;
        this.expiryTime = expiryTime;
        this.remainingSeconds = remainingSeconds;
    }

    public String getMessage() {
        return message;
    }

    public String getStoreTime() {
        return storeTime;
    }

    public String getExpiryTime() {
        return expiryTime;
    }

    public String getRemainingSeconds() {
        return remainingSeconds;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final FullDataJson that = (FullDataJson) o;
        return Objects.equals(message, that.message) &&
                Objects.equals(storeTime, that.storeTime) &&
                Objects.equals(expiryTime, that.expiryTime) &&
                Objects.equals(remainingSeconds, that.remainingSeconds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, storeTime, expiryTime, remainingSeconds);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", FullDataJson.class.getSimpleName() + "[", "]")
                .add("message='" + message + "'")
                .add("storeTime='" + storeTime + "'")
                .add("expiryTime='" + expiryTime + "'")
                .add("remainingSeconds='" + remainingSeconds + "'")
                .toString();
    }
}