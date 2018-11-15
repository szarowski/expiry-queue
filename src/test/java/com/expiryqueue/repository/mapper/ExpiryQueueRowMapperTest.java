package com.expiryqueue.repository.mapper;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class ExpiryQueueRowMapperTest {

    private static final String COL_MESSAGE = "message";
    private static final String COL_TIME_STORE = "store_time";
    private static final String COL_TIME_EXPIRY = "expiry_time";
    private static final String COL_REMAINING_SECONDS = "remaining_seconds";

    private ResultSet rs;
    private static final int ROW_NUM = 88;

    @Before
    public void setupMocks() {
        rs = mock(ResultSet.class);
    }

    @Test
    public void testInteractions() throws SQLException {
        when(rs.getObject(anyString(), eq(OffsetDateTime.class))).thenReturn(OffsetDateTime.now());

        final ExpiryQueueRowMapper rowMapper = new ExpiryQueueRowMapper();
        rowMapper.mapRow(rs, ROW_NUM);
        verify(rs, Mockito.times(1)).getString(COL_MESSAGE);
        verify(rs, Mockito.times(1)).getObject(COL_TIME_STORE, OffsetDateTime.class);
        verify(rs, Mockito.times(1)).getObject(COL_TIME_EXPIRY, OffsetDateTime.class);
        verify(rs, Mockito.times(1)).getLong(COL_REMAINING_SECONDS);

        verifyNoMoreInteractions(rs);
    }
}