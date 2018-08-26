#!/bin/bash

function launch_puts {
    FROM=$1
    TO=$2
    for i in $(seq $FROM $TO); do
        LON=$((i % 20))
        LAT=$((i % 10))
        curl --silent -X PUT localhost:8080/user_label/${i} -H "Content-Type: application/json" -d '{"lon": '$LON', "lat": '$LAT'}';
    done
}

STEP=200
FORK_NUMBER=4
START_TIME=$SECONDS
NUMBER_OF_CONCURRENT_WRITES_BY_KEY=2
echo "Number of queries to execute $((FORK_NUMBER * STEP * NUMBER_OF_CONCURRENT_WRITES_BY_KEY))"
for i in $(seq 0 $((FORK_NUMBER - 1))); do
    for j in $(seq 1 $NUMBER_OF_CONCURRENT_WRITES_BY_KEY); do
        START=$((i * STEP + 1))
        END=$((START + STEP - 1))
        echo "launching puts from $START to $END"
        launch_puts $START $END &
    done
done

wait
ELAPSED_TIME=$(($SECONDS - $START_TIME))
echo "$FORK_NUMBER forks with $STEP steps and $NUMBER_OF_CONCURRENT_WRITES_BY_KEY concurrent writes by key finished for $ELAPSED_TIME"