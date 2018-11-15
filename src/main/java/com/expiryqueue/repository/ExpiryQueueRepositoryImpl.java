package com.expiryqueue.repository;

import com.expiryqueue.model.internal.DataInternal;
import com.expiryqueue.repository.mapper.ExpiryQueueRowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import static com.expiryqueue.util.MapBuilder.mapWith;

/**
 * Implementation of the ExpiryQueue Repository providing persistence services for ExpiryQueueService.
 */
@Repository
public class ExpiryQueueRepositoryImpl implements ExpiryQueueRepository {

    private static final Logger LOG = LoggerFactory.getLogger(ExpiryQueueRepositoryImpl.class);

    private final NamedParameterJdbcTemplate jdbc;

    @Autowired
    public ExpiryQueueRepositoryImpl(final NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void saveDataInternal(final DataInternal data) {
        jdbc.update("INSERT INTO queue_log(message, store_time, expiry_time, remaining_seconds) " +
                        "VALUES (:message, :store_time, :expiry_time, :remaining_seconds)",
                mapWith("message", (Object) data.getMessage())
                        .and("store_time", data.getStoreTime())
                        .and("expiry_time", data.getExpiryTime())
                        .and("remaining_seconds", data.getRemainingSeconds()));
        LOG.info("Data stored into the database: " + data.toString());
    }

    @Override
    public List<DataInternal> findAllData() {
        LOG.info("Retrieving data from the database.");
        return new ArrayList<>(jdbc.query("SELECT * FROM queue_log", new ExpiryQueueRowMapper()));
    }
}