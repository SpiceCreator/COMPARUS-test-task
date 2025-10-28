#!/bin/sh
set -e

# 1) Ensure base dir exists
mkdir -p /data/h2

# 2) Start H2 TCP server + web console in the background
java -cp /opt/h2/h2.jar org.h2.tools.Server \
     -tcp -tcpAllowOthers -tcpPort 9092 \
     -web -webAllowOthers -webPort 81 \
     -baseDir /data/h2 &

# 3) Wait until TCP port is ready
echo "Waiting for H2 TCP server to start..."
while ! nc -z localhost 9092; do
  sleep 1
done
echo "H2 TCP server is up"

[ -f /data/h2/testdb.mv.db ] && rm /data/h2/testdb.mv.db
touch /data/h2/testdb.mv.db

# 4) Run init.sql against the server
java -cp /opt/h2/h2.jar org.h2.tools.RunScript \
     -url jdbc:h2:tcp://localhost:9092/testdb \
     -user sa \
     -script /docker-entrypoint-initdb.d/init.sql

# 5) Keep the TCP server in foreground so container stays alive
wait
