package com.expiryqueue.service;

import com.expiryqueue.model.DataJson;
import com.expiryqueue.model.FullDataJson;
import com.expiryqueue.model.MessageJson;
import com.expiryqueue.model.internal.DataInternal;
import com.expiryqueue.repository.ExpiryQueueRepository;
import com.expiryqueue.service.transformer.ExpiryQueueTransformer;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toList;

@Service
public class ExpiryQueueServiceImpl implements ExpiryQueueService {

    public static final String EXPIRY_MAP = "expiry-map";

    public static final int MIN_EXPIRY = 10;
    public static final int MAX_EXPIRY = 60;

    private static final Logger LOG = LoggerFactory.getLogger(ExpiryQueueServiceImpl.class);

    private final ExpiryQueueTransformer transformer;
    private final ExpiryQueueRepository repository;

    private IMap<Long, DataInternal> expiryMap;

    @Autowired
    ExpiryQueueServiceImpl(final ExpiryQueueTransformer transformer,
                                  final ExpiryQueueRepository repository,
                                  final HazelcastInstance hazelcast) {
        this.transformer = transformer;
        this.repository = repository;
        expiryMap = hazelcast.getMap(EXPIRY_MAP);
    }

    @Override
    public DataJson writeAndStore(final MessageJson message) {
        final Instant currentTime = Instant.now();
        final long expiryInSeconds = generateRandomExpiry();
        final Instant expiryTime = currentTime.plusSeconds(expiryInSeconds);
        final DataInternal dataInternal = transformer.toInternal(message, currentTime, expiryTime);
        expiryMap.put(currentTime.toEpochMilli(), dataInternal, expiryInSeconds, TimeUnit.SECONDS);
        LOG.info("DataInternal store into expiry queue: " + dataInternal.toString());
        repository.saveDataInternal(dataInternal);
        LOG.info("DataInternal store into database: " + dataInternal.toString());
        return new DataJson(message.getMessage(), expiryInSeconds);
    }

    @Override
    public List<DataJson> getRemainingMessages() {
        final Map<Long, DataJson> remainingMap = new TreeMap<>(Comparator.reverseOrder());
        final Instant currentTime = Instant.now();
        for (Map.Entry<Long, DataInternal> entry : expiryMap.entrySet()) {
            final DataJson data = transformer.toJson(entry.getValue(), currentTime);
            remainingMap.put(entry.getKey(), data);
        }
        LOG.info("Valid messages retrieved from the queue: " + remainingMap.values().toString());
        return new ArrayList<>(remainingMap.values());
    }

    @Override
    public List<FullDataJson> getAllMessages() {
        LOG.info("Retrieving of the queue log.");
        return repository.findAllData().stream().sorted(
                Comparator.<DataInternal>comparingLong(di -> di.getStoreTime().toInstant().toEpochMilli()).reversed())
                .map(transformer::toJson)
                .collect(toList());
    }

    protected long generateRandomExpiry() {
        int randomExpiry = new Random().nextInt(MAX_EXPIRY - MIN_EXPIRY) + MIN_EXPIRY;
        LOG.info("Random expiry time in seconds (between 10s and 60s) generated: " + randomExpiry);
        return randomExpiry;
    }
}