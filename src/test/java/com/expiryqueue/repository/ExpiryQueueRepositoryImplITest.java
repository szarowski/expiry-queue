package com.expiryqueue.repository;

import com.expiryqueue.model.internal.DataInternal;
import com.expiryqueue.util.RepositoryTest;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.jdbc.JdbcTestUtils;

import java.util.List;

import static com.expiryqueue.model.internal.DataInternalBuilder.dataInternalBuilder;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@RepositoryTest(ExpiryQueueRepositoryImpl.class)
public class ExpiryQueueRepositoryImplITest {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private ExpiryQueueRepository repository;

    @After
    public void cleanup() {
        JdbcTestUtils.deleteFromTables(jdbc, "queue_log");
    }

    @Test
    public void shouldInsertDataInternal() {
        DataInternal dataInternal= dataInternalBuilder().build();

        repository.saveDataInternal(dataInternal);

        List<DataInternal> dataInternalList = repository.findAllData();

        assertThat(dataInternalList.size()).isEqualTo(1);
        assertThat(dataInternalList).contains(dataInternal);
    }

    @Test
    public void shouldRetrieveAllDataInternals() {
        DataInternal dataInternal1= dataInternalBuilder().build();
        DataInternal dataInternal2= dataInternalBuilder().build();

        repository.saveDataInternal(dataInternal1);
        repository.saveDataInternal(dataInternal2);

        List<DataInternal> dataInternalList = repository.findAllData();

        assertThat(dataInternalList.size()).isEqualTo(2);
        assertThat(dataInternalList).contains(dataInternal1);
        assertThat(dataInternalList).contains(dataInternal2);
    }

    @Test
    public void shouldRetrieveNothingForEmptyQueue() {
        assertThat(repository.findAllData()).isEmpty();
    }
}