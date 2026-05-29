-- V1: Initial schema
-- Domain tables will be added in subsequent migrations

CREATE TABLE IF NOT EXISTS schema_version_log (
    id         BIGSERIAL PRIMARY KEY,
    applied_at TIMESTAMP NOT NULL DEFAULT NOW(),
    note       VARCHAR(255)
);

INSERT INTO schema_version_log (note) VALUES ('Schema initialized');