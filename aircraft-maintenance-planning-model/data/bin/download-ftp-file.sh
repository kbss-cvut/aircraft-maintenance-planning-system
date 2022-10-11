#!/bin/bash

INPUT_FILE_PATH=$1
OUTPUT_FILE_PATH=$2

SCRIPT_DIR=$(dirname $(readlink -m $0))
#INPUT_FILE=Dashboard2_time-analysis.csv
#OUTPUT_FILE=./target/input.tsv

INPUT_FILE_NAME=$(echo $INPUT_FILE_PATH | sed 's|.*/||')


$(cat $SCRIPT_DIR/../private/ftp-access-command):$INPUT_FILE_PATH

# create output directory if does not exists
mkdir -p $(dirname $OUTPUT_FILE_PATH)

cat $INPUT_FILE_NAME > $OUTPUT_FILE_PATH

rm $INPUT_FILE_NAME
