#!/bin/bash

INPUT_ROOT_DIR=./private/3-ttl-data


find $INPUT_ROOT_DIR -name '*.ttl' | while read FILE; do
    if [ ! -s "$FILE" ]; then
        echo "INFO: Removing empty file: $FILE"
        rm "$FILE"
    fi
done
