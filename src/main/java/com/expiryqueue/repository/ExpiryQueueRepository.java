package com.expiryqueue.repository;

import com.expiryqueue.model.internal.DataInternal;

import java.util.List;

/**
 * Repository providing persistence services for expiry queue.
 */
public interface ExpiryQueueRepository {

    /**
     * Find all DataInternal objects stored int the database.
     *
     * @return the List<DataInternal> object
     */
    List<DataInternal> findAllData();

    /**
     * Save the DataInternal into a database.
     *
     * @param data the DataInternal object to save
     */
    void saveDataInternal(DataInternal data);
}
