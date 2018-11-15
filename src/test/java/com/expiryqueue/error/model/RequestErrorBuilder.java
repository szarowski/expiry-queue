package com.expiryqueue.error.model;

public final class RequestErrorBuilder {
    private String code;
    private String message;
    private String field;
    private String type;

    private RequestErrorBuilder() {
    }

    public static RequestErrorBuilder requestErrorBuilder() {
        return new RequestErrorBuilder();
    }

    public RequestErrorBuilder code(String code) {
        this.code = code;
        return this;
    }

    public RequestErrorBuilder message(String message) {
        this.message = message;
        return this;
    }

    public RequestErrorBuilder field(String field) {
        this.field = field;
        return this;
    }

    public RequestErrorBuilder type(String type) {
        this.type = type;
        return this;
    }

    public RequestError build() {
        return new RequestError(code, message, field, type);
    }
}
