package com.expiryqueue.service.transformer;

import com.expiryqueue.model.DataJson;
import com.expiryqueue.model.FullDataJson;
import com.expiryqueue.model.MessageJson;
import com.expiryqueue.model.internal.DataInternal;

import java.time.Instant;

/**
 * External to Internal data representation (and vice versa) transformer.
 */
public interface ExpiryQueueTransformer {

    /**
     * Transform MessageJson into DataInternal.
     *
     * @param message the MessageJson object
     * @param currentTime the current time as Instant object
     * @param expiryTime the expiry time as Instant object
     * @return the DataInternal object
     */
    DataInternal toInternal(MessageJson message, Instant currentTime, Instant expiryTime);

    /**
     * Transform DataInternal into DataJson.
     *
     * @param data the DataInternal object
     * @param currentTime the current time as Instant object
     * @return the DataJson object
     */
    DataJson toJson(DataInternal data, Instant currentTime);

    /**
     * Transform DataInternal into FullDataJson.
     *
     * @param data the DataInternal object
     * @return the FullDataJson object
     */
    FullDataJson toJson(DataInternal data);
}