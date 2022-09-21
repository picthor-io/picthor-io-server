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

-- // removed batch jobs
-- Migration SQL that makes the change goes here.
DROP TABLE batch_job_item;
DROP TABLE batch_job;


-- //@UNDO
-- SQL to undo the change goes here.
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

