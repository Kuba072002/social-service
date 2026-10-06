#!/usr/bin/env bash

CONTAINER_NAME="scylla_db"
TIMEOUT=120
WARM_UP_TIME=15
START_TIME=$(date +%s)

docker compose up -d

sleep "$WARM_UP_TIME"

echo "Waiting for ${CONTAINER_NAME} to become healthy..."
while true; do
  STATUS=$(docker inspect -f '{{.State.Health.Status}}' scylla_db)

  if [ "$STATUS" = "healthy" ]; then
    echo "${CONTAINER_NAME} is healthy."
    break
  fi

  CURRENT_TIME=$(date +%s)

  if [ $((CURRENT_TIME - START_TIME)) -ge "$TIMEOUT" ]; then
    echo "Timeout: ${CONTAINER_NAME} did not become healthy within ${TIMEOUT} seconds."
    exit 1
  fi

  sleep 2
done

docker exec "$CONTAINER_NAME" cqlsh -f ./init/init_scylla.cql

echo "ScyllaDB setup completed."