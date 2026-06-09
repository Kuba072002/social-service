CREATE KEYSPACE message_keyspace
WITH replication = {
    'class' : 'SimpleStrategy',
    'replication_factor' : 1
};

USE message_keyspace;

CREATE TABLE IF NOT EXISTS messages (
    chat_id bigint,
    message_id uuid,
    sender_id bigint,
    content text,
    media_content text,
    timestamp timestamp,
    state text,

    PRIMARY KEY ((chat_id), message_id)
)
WITH CLUSTERING ORDER BY (message_id DESC);

CREATE TABLE IF NOT EXISTS message_dedup (
    sender_id bigint,
    client_message_id uuid,

    PRIMARY KEY ((sender_id), client_message_id)
) WITH default_time_to_live = 86400;