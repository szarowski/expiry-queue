package com.expiryqueue.service;

import com.expiryqueue.model.DataJson;
import com.expiryqueue.model.FullDataJson;
import com.expiryqueue.model.MessageJson;

import java.util.List;

/**
 * The Service to process requests from the ExpiryQueueController.
 */
public interface ExpiryQueueService {

    /**
     * Write a message into thr queue and store into the database.
     *
     * @param message the MessageJson object
     * @return the DataJson objects representing the message and remaining seconds to expiration
     */
    DataJson writeAndStore(MessageJson message);

    /**
     * Get all remaining messages together with remaining time in seconds
     * from the queue (excluding expired messages).
     *
     * @return the List<DataJson> objects
     */
    List<DataJson> getRemainingMessages();

    /**
     * Get all messages history as a log.
     *
     * @return the List<FullDataJson> objects
     */
    List<FullDataJson> getAllMessages();
}