package com.expiryqueue.model.internal;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.StringJoiner;

public final class DataInternal implements Serializable {

    private final String message;

    private final OffsetDateTime storeTime;

    private final OffsetDateTime expiryTime;

    private final Long remainingSeconds;

    public DataInternal(final String message, final OffsetDateTime storeTime,
                        final OffsetDateTime expiryTime, final Long remainingSeconds) {
        this.message = message;
        this.storeTime = storeTime;
        this.expiryTime = expiryTime;
        this.remainingSeconds = remainingSeconds;
    }

    public String getMessage() {
        return message;
    }

    public OffsetDateTime getStoreTime() {
        return storeTime;
    }

    public OffsetDateTime getExpiryTime() {
        return expiryTime;
    }

    public Long getRemainingSeconds() {
        return remainingSeconds;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final DataInternal that = (DataInternal) o;
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
        return new StringJoiner(", ", DataInternal.class.getSimpleName() + "[", "]")
                .add("message='" + message + "'")
                .add("storeTime=" + storeTime)
                .add("expiryTime=" + expiryTime)
                .add("remainingSeconds=" + remainingSeconds)
                .toString();
    }
}