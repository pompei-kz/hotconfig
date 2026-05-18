#!/usr/bin/env bash

cd "$(dirname "$0")" || exit 113

docker compose down
docker compose build
docker compose up -d
docker compose logs -f
