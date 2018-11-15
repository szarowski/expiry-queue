CREATE TABLE IF NOT EXISTS queue_log (
    message             VARCHAR(255)    NOT NULL,
    store_time          TIMESTAMPTZ     NOT NULL,
    expiry_time         TIMESTAMPTZ     NOT NULL,
    remaining_seconds   INTEGER         NOT NULL,

    PRIMARY KEY (store_time)
);
