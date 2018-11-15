package com.expiryqueue.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.StringJoiner;

public final class MessageJson {

    @NotNull
    private final String message;

    @JsonCreator
    public MessageJson(final String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final MessageJson that = (MessageJson) o;
        return Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", MessageJson.class.getSimpleName() + "[", "]")
                .add("message='" + message + "'")
                .toString();
    }
}