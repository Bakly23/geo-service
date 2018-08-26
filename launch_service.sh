#!/bin/bash

java -jar target/geo-service-1.0-SNAPSHOT.jar \
    --server.port=8080 \
    --users.table.path=src/main/resources/users.csv \
    --geo.cells.table.path=src/main/resources/geo_cells.csv