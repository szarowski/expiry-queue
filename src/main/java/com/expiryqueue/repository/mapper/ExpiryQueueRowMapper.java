package com.expiryqueue.repository.mapper;

import com.expiryqueue.model.internal.DataInternal;
import org.springframework.jdbc.core.RowMapper;

import javax.annotation.Nonnull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;

public class ExpiryQueueRowMapper implements RowMapper<DataInternal> {

    @Override
    public DataInternal mapRow(@Nonnull final ResultSet rs, final int rowNum) throws SQLException {
        return new DataInternal(
                rs.getString("message"),
                rs.getObject("store_time", OffsetDateTime.class),
                rs.getObject("expiry_time", OffsetDateTime.class),
                rs.getLong("remaining_seconds"));
    }
}
