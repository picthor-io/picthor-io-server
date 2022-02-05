--
--    Copyright 2010-2016 the original author or authors.
--
--    Licensed under the Apache License, Version 2.0 (the "License");
--    you may not use this file except in compliance with the License.
--    You may obtain a copy of the License at
--
--       http://www.apache.org/licenses/LICENSE-2.0
--
--    Unless required by applicable law or agreed to in writing, software
--    distributed under the License is distributed on an "AS IS" BASIS,
--    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
--    See the License for the specific language governing permissions and
--    limitations under the License.
--

-- Migration SQL that makes the change goes here.
CREATE TABLE IF NOT EXISTS setting
(
    id         BIGSERIAL    NOT NULL PRIMARY KEY,
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP,
    name       VARCHAR(255) NOT NULL UNIQUE,
    value      VARCHAR(255) NOT NULL,
    type       VARCHAR(50)  NOT NULL
);

CREATE TABLE IF NOT EXISTS directory
(
    id                BIGSERIAL    NOT NULL PRIMARY KEY,
    type              VARCHAR(255) NOT NULL,
    created_at        TIMESTAMP    NOT NULL,
    updated_at        TIMESTAMP,
    last_sync_at      TIMESTAMP,
    parent_id         BIGINT,
    root_directory_id BIGINT,
    full_path         TEXT         NOT NULL UNIQUE,
    name              VARCHAR(255) NOT NULL,
    label             VARCHAR(255) NULL,
    description       VARCHAR(255),
    state             VARCHAR(50)  NOT NULL,
    excludes          TEXT,
    stats             JSONB DEFAULT '{
      "dirs_num": 0,
      "files_num": 0,
      "size_bytes": 0,
      "child_dirs_num": 0,
      "child_files_num": 0,
      "child_size_bytes": 0
    }'::jsonb,
    FOREIGN KEY (parent_id) REFERENCES directory (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS file_data
(
    id                BIGSERIAL    NOT NULL PRIMARY KEY,
    created_at        TIMESTAMP    NOT NULL,
    taken_at          TIMESTAMP,
    updated_at        TIMESTAMP,
    full_path         TEXT         NOT NULL UNIQUE,
    dir_path          TEXT         NOT NULL,
    root_directory_id BIGINT       NOT NULL,
    directory_id      BIGINT       NOT NULL,
    hash              VARCHAR(16),
    type              VARCHAR(255),
    sync_state        VARCHAR(255) NOT NULL,
    file_name         TEXT         NOT NULL,
    base_name         TEXT         NOT NULL,
    extension         VARCHAR(255) NOT NULL,
    size_bytes        BIGINT       NOT NULL,
    index_nanos       BIGINT,
    error             TEXT,
    meta              JSONB,
    FOREIGN KEY (root_directory_id) REFERENCES directory (id) ON DELETE CASCADE,
    FOREIGN KEY (directory_id) REFERENCES directory (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS batch_job
(
    id           BIGSERIAL    NOT NULL PRIMARY KEY,
    created_at   TIMESTAMP,
    name         VARCHAR(255) NOT NULL,
    total_items  INTEGER      NULL,
    state        VARCHAR(50)  NOT NULL,
    type         VARCHAR(50)  NOT NULL,
    payload      JSONB        NULL,
    process_type VARCHAR(255) NOT NULL,
    process_at   TIMESTAMP,
    updated_at   TIMESTAMP
);

CREATE TABLE IF NOT EXISTS batch_job_item
(
    id                 BIGSERIAL    NOT NULL PRIMARY KEY,
    created_at         TIMESTAMP,
    state              VARCHAR(255) NOT NULL,
    relation_type      VARCHAR(255) NULL,
    related_id         BIGINT       NULL,
    batch_job_id       BIGINT       NOT NULL,
    next_item_id       BIGINT       NULL,
    prev_item_id       BIGINT       NULL,
    payload            JSONB        NULL,
    error              TEXT         NULL,
    last_in_queue      BOOLEAN      NOT NULL,
    position_in_queue  INTEGER      NOT NULL,
    internal_total     INTEGER      NOT NULL,
    internal_processed INTEGER      NOT NULL,
    duration           BIGINT       NULL,
    first_in_queue     BOOLEAN      NOT NULL DEFAULT FALSE,
    process_at         TIMESTAMP,
    updated_at         TIMESTAMP,
    FOREIGN KEY (batch_job_id) REFERENCES batch_job (id) ON DELETE CASCADE
);

-- //@UNDO
-- SQL to undo the change goes here.
DROP TABLE IF EXISTS file_data;
DROP TABLE IF EXISTS directory;
DROP TABLE IF EXISTS batch_job_item;
DROP TABLE IF EXISTS batch_job;
DROP TABLE IF EXISTS setting;

