package com.expiryqueue.controller;

import com.expiryqueue.config.TestRestConfig;
import com.expiryqueue.error.model.Errors;
import com.expiryqueue.error.model.RequestError;
import com.expiryqueue.model.DataJson;
import com.expiryqueue.model.FullDataJson;
import com.expiryqueue.model.MessageJson;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.expiryqueue.error.model.RequestErrorBuilder.requestErrorBuilder;
import static com.expiryqueue.model.DataJsonBuilder.dataJsonBuilder;
import static com.expiryqueue.model.MessageJsonBuilder.messageJsonBuilder;
import static com.expiryqueue.service.ExpiryQueueServiceImpl.EXPIRY_MAP;
import static com.expiryqueue.util.IntegrationTestHelper.apiUrl;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.RequestEntity.get;
import static org.springframework.http.RequestEntity.post;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = TestRestConfig.class)
public class ExpiryQueueControllerITest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private HazelcastInstance hazelcastInstance;

    @After
    public void tearDown( ) {
        jdbcTemplate.update("DELETE FROM queue_log");
        hazelcastInstance.getMap(EXPIRY_MAP).clear();
    }

    @AfterClass
    public static void destroy( ) {
        Hazelcast.shutdownAll();
    }

    @Test
    public void shouldStoreAndRetrieveMessageFromDatabase() {
        MessageJson messageJson = messageJsonBuilder().build();
        DataJson dataJson = dataJsonBuilder().message(messageJson.getMessage()).build();

        ResponseEntity<DataJson> response = restTemplate.exchange(
                post(apiUrl("/v1/expiry-queue/write", port)).header(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                        .body(messageJson), DataJson.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo(dataJson.getMessage());

        List<Long> remainingSeconds = jdbcTemplate.queryForList("SELECT remaining_seconds FROM queue_log", Long.class);

        ResponseEntity<List<FullDataJson>> responseList = restTemplate.exchange(
                get(apiUrl("/v1/expiry-queue/log", port))
                        .header(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE).build(),
                new ParameterizedTypeReference<List<FullDataJson>>() {});

        assertThat(responseList.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseList.getBody()).extracting("remainingSeconds")
                .containsExactly(remainingSeconds.get(0) + " seconds");

        int dataCount  = jdbcTemplate.queryForObject("SELECT count(*) FROM queue_log", int.class);
        assertThat(dataCount).isEqualTo(1);
    }

    @Test
    public void shouldStoreAndRetrieveMessageFromQueue() {
        MessageJson messageJson = messageJsonBuilder().build();
        DataJson dataJson = dataJsonBuilder().message(messageJson.getMessage()).build();

        ResponseEntity<DataJson> response = restTemplate.exchange(
                post(apiUrl("/v1/expiry-queue/write", port)).header(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                        .body(messageJson), DataJson.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo(dataJson.getMessage());

        List<Long> remainingSeconds = jdbcTemplate.queryForList("SELECT remaining_seconds FROM queue_log", Long.class);

        ResponseEntity<List<DataJson>> responseList = restTemplate.exchange(
                get(apiUrl("/v1/expiry-queue/read", port))
                        .header(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE).build(),
                new ParameterizedTypeReference<List<DataJson>>() {});

        assertThat(responseList.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseList.getBody()).extracting("expiry")
                .containsExactlyInAnyOrder(remainingSeconds.get(0));

        int dataCount  = jdbcTemplate.queryForObject("SELECT count(*) FROM queue_log", int.class);
        assertThat(dataCount).isEqualTo(1);
    }

    @Test
    public void shouldStoreExpireAndNotRetrieveMessageFromQueue() throws InterruptedException {
        MessageJson messageJson = messageJsonBuilder().build();
        DataJson dataJson = dataJsonBuilder().message(messageJson.getMessage()).build();

        ResponseEntity<DataJson> response = restTemplate.exchange(
                post(apiUrl("/v1/expiry-queue/write", port)).header(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                        .body(messageJson), DataJson.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo(dataJson.getMessage());

        List<Long> remainingSeconds = jdbcTemplate.queryForList("SELECT remaining_seconds FROM queue_log", Long.class);

        TimeUnit.SECONDS.sleep(remainingSeconds.get(0));

        ResponseEntity<List<DataJson>> responseList = restTemplate.exchange(
                get(apiUrl("/v1/expiry-queue/read", port))
                        .header(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE).build(),
                new ParameterizedTypeReference<List<DataJson>>() {});

        assertThat(responseList.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseList.getBody()).isNotNull();
        assertThat(responseList.getBody().size()).isEqualTo(0);

        int dataCount  = jdbcTemplate.queryForObject("SELECT count(*) FROM queue_log", int.class);
        assertThat(dataCount).isEqualTo(1);
    }

    @Test
    public void shouldReturn422OnInvalidInput() {
        RequestError requestError = requestErrorBuilder().build();

        ResponseEntity<Errors> response = restTemplate.exchange(
                post(apiUrl("/v1/expiry-queue/write", port)).header(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                        .body(requestError), Errors.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().getErrors())
                .hasSize(1)
                .contains(new RequestError("NotNull", "message must not be null", "message", null));

        MessageJson request = messageJsonBuilder().message(null).build();

        ResponseEntity<Errors> response2 = restTemplate.exchange(
                post(apiUrl("/v1/expiry-queue/write", port)).header(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                        .body(request), Errors.class);

        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response2.getBody().getErrors())
                .hasSize(1)
                .contains(new RequestError("NotNull", "message must not be null", "message", null));
    }
}