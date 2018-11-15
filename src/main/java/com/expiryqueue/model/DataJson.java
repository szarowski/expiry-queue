package com.expiryqueue.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Objects;
import java.util.StringJoiner;

public final class DataJson {

    private final String message;

    private final Long expiry;

    @JsonCreator
    public DataJson(final String message, final Long expiry) {
        this.message = message;
        this.expiry = expiry;
    }

    public String getMessage() {
        return message;
    }

    public Long getExpiry() {
        return expiry;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final DataJson dataJson = (DataJson) o;
        return Objects.equals(message, dataJson.message) &&
                Objects.equals(expiry, dataJson.expiry);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, expiry);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", DataJson.class.getSimpleName() + "[", "]")
                .add("message='" + message + "'")
                .add("expiry=" + expiry)
                .toString();
    }
}
