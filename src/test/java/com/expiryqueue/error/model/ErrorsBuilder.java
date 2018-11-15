package com.expiryqueue.error.model;

import java.util.List;

public final class ErrorsBuilder {
    private List<RequestError> errors;

    private ErrorsBuilder() {
    }

    public static ErrorsBuilder errorsBuilder() {
        return new ErrorsBuilder();
    }

    public ErrorsBuilder errors(List<RequestError> errors) {
        this.errors = errors;
        return this;
    }

    public Errors build() {
        return new Errors(errors);
    }
}
