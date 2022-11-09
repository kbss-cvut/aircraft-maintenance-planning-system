#!/bin/bash

#DATASET_FILES="training-dataset"
DATASET_FILES=$1

SCRIPT_PATH=$(dirname "$(readlink -f "$0")")
mkdir -p "$SCRIPT_PATH"/../output/
OUTPUT_FILE_PATH=$SCRIPT_PATH/../output/$DATASET_FILES-output.csv
cd "$SCRIPT_PATH"/../ || return
mvn clean compile exec:java -Dexec.mainClass="cz.cvut.kbss.textanalysis.Main" -Dexec.args="$DATASET_FILES $OUTPUT_FILE_PATH" -q

