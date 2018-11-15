package com.expiryqueue.service.transformer;

import com.expiryqueue.model.DataJson;
import com.expiryqueue.model.FullDataJson;
import com.expiryqueue.model.MessageJson;
import com.expiryqueue.model.internal.DataInternal;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class ExpiryQueueTransformerImpl implements ExpiryQueueTransformer {

    static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public DataInternal toInternal(final MessageJson message, final Instant currentTime, final Instant expiryTime) {
        final Long expiryTimeInSeconds = expiryTime.minusSeconds(currentTime.getEpochSecond()).getEpochSecond();
        return new DataInternal(message.getMessage(),
                OffsetDateTime.ofInstant(currentTime, ZoneId.systemDefault()),
                OffsetDateTime.ofInstant(expiryTime, ZoneId.systemDefault()),
                expiryTimeInSeconds);
    }

    @Override
    public DataJson toJson(final DataInternal data, final Instant currentTime) {
        return new DataJson(data.getMessage(),
                data.getExpiryTime().toEpochSecond() - currentTime.getEpochSecond());
    }

    @Override
    public FullDataJson toJson(final DataInternal data) {
        return new FullDataJson(data.getMessage(),
                data.getStoreTime().format(DATE_TIME_FORMATTER),
                data.getExpiryTime().format(DATE_TIME_FORMATTER),
                data.getRemainingSeconds() + " seconds");
    }
}
