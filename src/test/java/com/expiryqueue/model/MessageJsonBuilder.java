package com.expiryqueue.model;

import com.expiryqueue.util.Random;

public final class MessageJsonBuilder {
    private String message = Random.alphaNumeric();

    private MessageJsonBuilder() {
    }

    public static MessageJsonBuilder messageJsonBuilder() {
        return new MessageJsonBuilder();
    }

    public MessageJsonBuilder message(String message) {
        this.message = message;
        return this;
    }

    public MessageJson build() {
        return new MessageJson(message);
    }
}
