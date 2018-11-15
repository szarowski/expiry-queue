package com.expiryqueue.service.transformer;

import com.expiryqueue.model.DataJson;
import com.expiryqueue.model.FullDataJson;
import com.expiryqueue.model.MessageJson;
import com.expiryqueue.model.internal.DataInternal;
import com.expiryqueue.util.Random;
import org.junit.Test;

import java.time.Instant;

import static com.expiryqueue.model.MessageJsonBuilder.messageJsonBuilder;
import static com.expiryqueue.model.internal.DataInternalBuilder.dataInternalBuilder;
import static com.expiryqueue.service.ExpiryQueueServiceImpl.MAX_EXPIRY;
import static com.expiryqueue.service.transformer.ExpiryQueueTransformerImpl.DATE_TIME_FORMATTER;
import static org.assertj.core.api.Assertions.assertThat;

public class ExpiryQueueTransformerImplTest {

    private ExpiryQueueTransformer transformer = new ExpiryQueueTransformerImpl();

    @Test
    public void shouldTransformToInternal() {
        MessageJson messageJson = messageJsonBuilder().build();

        DataInternal internal = transformer.toInternal(
                messageJson, Instant.now(),
                Instant.now().plusSeconds(Random.intVal(MAX_EXPIRY)));

        assertThat(internal.getMessage()).isEqualTo(messageJson.getMessage());
    }

    @Test
    public void shouldTransformToDataJson() {
        DataInternal dataInternal = dataInternalBuilder().build();

        DataJson dataJson = transformer.toJson(dataInternal, dataInternal.getExpiryTime().toInstant());

        assertThat(dataJson.getMessage()).isEqualTo(dataInternal.getMessage());
        assertThat(dataJson.getExpiry()).isEqualTo(0);
    }

    @Test
    public void shouldTransformToFullDataJson() {
        DataInternal dataInternal = dataInternalBuilder().build();

        FullDataJson fullDataJson = transformer.toJson(dataInternal);

        assertThat(fullDataJson.getMessage()).isEqualTo(dataInternal.getMessage());
        assertThat(fullDataJson.getStoreTime()).isEqualTo(dataInternal.getStoreTime().format(DATE_TIME_FORMATTER));
        assertThat(fullDataJson.getExpiryTime()).isEqualTo(dataInternal.getExpiryTime().format(DATE_TIME_FORMATTER));
        assertThat(fullDataJson.getRemainingSeconds()).isEqualTo(dataInternal.getRemainingSeconds() + " seconds");
    }
}