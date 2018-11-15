package com.expiryqueue.service;

import com.expiryqueue.config.HazelcastConfig;
import com.expiryqueue.model.DataJson;
import com.expiryqueue.model.FullDataJson;
import com.expiryqueue.model.MessageJson;
import com.expiryqueue.model.internal.DataInternal;
import com.expiryqueue.repository.ExpiryQueueRepository;
import com.expiryqueue.service.transformer.ExpiryQueueTransformer;
import com.google.common.collect.ImmutableList;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.expiryqueue.model.DataJsonBuilder.dataJsonBuilder;
import static com.expiryqueue.model.FullDataJsonBuilder.fullDataJsonBuilder;
import static com.expiryqueue.model.MessageJsonBuilder.messageJsonBuilder;
import static com.expiryqueue.model.internal.DataInternalBuilder.dataInternalBuilder;
import static com.expiryqueue.service.ExpiryQueueServiceImpl.EXPIRY_MAP;
import static com.expiryqueue.service.ExpiryQueueServiceImpl.MAX_EXPIRY;
import static com.expiryqueue.service.ExpiryQueueServiceImpl.MIN_EXPIRY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;

@RunWith(MockitoJUnitRunner.class)
public class ExpiryQueueServiceImplTest {

    @Mock
    private ExpiryQueueTransformer transformer;

    @Mock
    private ExpiryQueueRepository repository;

    private HazelcastInstance hazelcast;

    private ExpiryQueueServiceImpl service;

    @Before
    public void setUp() {
        hazelcast = Hazelcast.getOrCreateHazelcastInstance(
                new HazelcastConfig().hazelcast());
    }

    @After
    public void tearDown( ) {
        Hazelcast.shutdownAll();
    }

    @Test
    public void shouldCreateDataInternalAndNotExpire() {
        service = new ExpiryQueueServiceStub(transformer, repository, hazelcast, MAX_EXPIRY);

        MessageJson messageJson = messageJsonBuilder().build();
        DataInternal dataInternal= dataInternalBuilder().message(messageJson.getMessage()).build();
        DataJson dataJson = dataJsonBuilder().message(dataInternal.getMessage()).build();

        doNothing().when(repository).saveDataInternal(dataInternal);
        given(transformer.toInternal(eq(messageJson), any(Instant.class), any(Instant.class)))
                .willReturn(dataInternal);

        DataJson storedData = service.writeAndStore(messageJson);

        assertThat(storedData).isNotNull();
        assertThat(storedData.getMessage()).isEqualTo(dataJson.getMessage());

        assertThat(hazelcast.getMap(EXPIRY_MAP).size()).isEqualTo(1);
        assertThat(((DataInternal) hazelcast.getMap(EXPIRY_MAP)
                .entrySet().iterator().next().getValue()).getMessage())
                .isEqualTo(messageJson.getMessage());
    }

    @Test
    public void shouldCreateDataInternalButExpire() throws InterruptedException {
        service = new ExpiryQueueServiceStub(transformer, repository, hazelcast, MIN_EXPIRY);

        MessageJson messageJson = messageJsonBuilder().build();
        DataInternal dataInternal= dataInternalBuilder().message(messageJson.getMessage()).build();
        DataJson dataJson = dataJsonBuilder().message(dataInternal.getMessage()).build();

        doNothing().when(repository).saveDataInternal(dataInternal);
        given(transformer.toInternal(eq(messageJson), any(Instant.class), any(Instant.class)))
                .willReturn(dataInternal);

        DataJson storedData = service.writeAndStore(messageJson);

        TimeUnit.SECONDS.sleep(MIN_EXPIRY);

        assertThat(storedData).isNotNull();
        assertThat(storedData.getMessage()).isEqualTo(dataJson.getMessage());

        assertThat(hazelcast.getMap(EXPIRY_MAP).size()).isEqualTo(0);
    }

    @Test
    public void shouldCreateTwoDataInternalsAndRetrieveFromQueueInOrder() {
        service = new ExpiryQueueServiceStub(transformer, repository, hazelcast, MAX_EXPIRY);

        MessageJson messageJson1 = messageJsonBuilder().build();
        MessageJson messageJson2 = messageJsonBuilder().build();
        DataInternal dataInternal1 = dataInternalBuilder().message(messageJson1.getMessage()).storeTime(OffsetDateTime.now()).build();
        DataInternal dataInternal2 = dataInternalBuilder().message(messageJson2.getMessage()).storeTime(OffsetDateTime.now().plusSeconds(1L)).build();
        DataJson dataJson1 = dataJsonBuilder().message(dataInternal1.getMessage()).build();
        DataJson dataJson2 = dataJsonBuilder().message(dataInternal2.getMessage()).build();

        doNothing().when(repository).saveDataInternal(dataInternal1);
        given(transformer.toInternal(eq(messageJson1), any(Instant.class), any(Instant.class)))
                .willReturn(dataInternal1);
        given(transformer.toInternal(eq(messageJson2), any(Instant.class), any(Instant.class)))
                .willReturn(dataInternal2);
        given(transformer.toJson(eq(dataInternal1), any(Instant.class)))
                .willReturn(dataJson1);
        given(transformer.toJson(eq(dataInternal2), any(Instant.class)))
                .willReturn(dataJson2);

        service.writeAndStore(messageJson1);
        service.writeAndStore(messageJson2);

        List<DataJson> dataJsons = service.getRemainingMessages();

        assertThat(dataJsons.size()).isEqualTo(2);
        assertThat(dataJsons.get(0).getMessage()).isEqualTo(messageJson2.getMessage());
        assertThat(dataJsons.get(1).getMessage()).isEqualTo(messageJson1.getMessage());
    }

    @Test
    public void shouldCreateTwoDataInternalsAndRetrieveFromDatabaseInOrder() {
        service = new ExpiryQueueServiceStub(transformer, repository, hazelcast, MAX_EXPIRY);

        DataInternal dataInternal1 = dataInternalBuilder().storeTime(OffsetDateTime.now()).build();
        DataInternal dataInternal2 = dataInternalBuilder().storeTime(OffsetDateTime.now().plusSeconds(1L)).build();
        FullDataJson fullDataJson1 = fullDataJsonBuilder().message(dataInternal1.getMessage()).build();
        FullDataJson fullDataJson2 = fullDataJsonBuilder().message(dataInternal2.getMessage()).build();

        given(repository.findAllData()).willReturn(ImmutableList.of(dataInternal1, dataInternal2));
        given(transformer.toJson(dataInternal1)).willReturn(fullDataJson1);
        given(transformer.toJson(dataInternal2)).willReturn(fullDataJson2);

        List<FullDataJson> responses = service.getAllMessages();

        assertThat(responses.size()).isEqualTo(2);
        assertThat(responses.get(0).getMessage()).isEqualTo(dataInternal2.getMessage());
        assertThat(responses.get(1).getMessage()).isEqualTo(dataInternal1.getMessage());
    }

    @Test
    public void shouldRetrieveEmptyList() {
        service = new ExpiryQueueServiceStub(transformer, repository, hazelcast, MAX_EXPIRY);

        given(repository.findAllData()).willReturn(Lists.emptyList());

        List<FullDataJson> responses = service.getAllMessages();

        assertThat(responses.size()).isEqualTo(0);
    }

    private final class ExpiryQueueServiceStub extends ExpiryQueueServiceImpl {

        private final long maxExpiry;

        ExpiryQueueServiceStub(ExpiryQueueTransformer transformer, ExpiryQueueRepository repository,
                              HazelcastInstance hazelcast, long maxExpiry) {
            super(transformer, repository, hazelcast);
            this.maxExpiry = maxExpiry;
        }

        @Override
        protected long generateRandomExpiry() {
            return maxExpiry;
        }
    }
}