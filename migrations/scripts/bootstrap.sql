CREATE TABLE changelog
(
    id          BIGSERIAL NOT NULL PRIMARY KEY,
    applied_at  TEXT      NOT NULL,
    description TEXT      NOT NULL
);