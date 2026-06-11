--liquibase formatted sql
--changeset kuba:1
CREATE SCHEMA IF NOT EXISTS chat_schema;

--changeset kuba:2
CREATE TABLE IF NOT EXISTS chat_schema.chats
(
    id               BIGSERIAL PRIMARY KEY,
    name             VARCHAR(255),
    image_url        VARCHAR(2048),
    chat_type        VARCHAR(10) NOT NULL,
    last_message_at  TIMESTAMPTZ,
    private_pair_key VARCHAR(40),
    created_at       TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uq_chats_private_pair_key
    ON chat_schema.chats (private_pair_key)
    WHERE private_pair_key IS NOT NULL;

--changeset kuba:3

CREATE TABLE IF NOT EXISTS chat_schema.chat_participants
(
    id           BIGSERIAL PRIMARY KEY,
    chat_id      BIGINT      NOT NULL,
    user_id      BIGINT,
    role         VARCHAR(10) NOT NULL,
    last_read_at TIMESTAMPTZ,
    joined_at    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_chat_participants_chat
        FOREIGN KEY (chat_id)
            REFERENCES chat_schema.chats (id),

    CONSTRAINT uq_chat_participants_chat_user
        UNIQUE (chat_id, user_id)
);

CREATE INDEX idx_chat_participants_user_chat_last_read
    ON chat_schema.chat_participants (user_id, chat_id, last_read_at);

--changeset kuba:4
CREATE TABLE IF NOT EXISTS chat_event_outbox
(
    id         BIGSERIAL PRIMARY KEY,
    payload    JSONB,
    created_at BIGINT
);

