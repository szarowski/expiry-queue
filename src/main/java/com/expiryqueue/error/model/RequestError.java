package com.expiryqueue.error.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * RequestError for com.payments.model
 */
@JsonInclude(Include.NON_NULL)
public final class RequestError {
    private final String code;
    private final String message;
    private final String field;
    private final String type;

    @JsonCreator
    public RequestError(final String code, final String message, final String field, final String type) {
        this.code = code;
        this.message = message;
        this.field = field;
        this.type = type;
    }

    public RequestError(final String message) {
        this(null, message, null, null);
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getField() {
        return field;
    }

    public String getType() {
        return type;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final RequestError that = (RequestError) o;
        return Objects.equals(code, that.code) &&
                Objects.equals(message, that.message) &&
                Objects.equals(field, that.field) &&
                Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, message, field, type);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", RequestError.class.getSimpleName() + "[", "]")
                .add("code='" + code + "'")
                .add("message='" + message + "'")
                .add("field='" + field + "'")
                .add("type='" + type + "'")
                .toString();
    }
}