#!/usr/bin/env bash
sudo -i -u postgres psql -c "CREATE USER expiry_queue WITH PASSWORD 'expiry_queue';"
sudo -u postgres createdb -O expiry_queue expiry_queue
