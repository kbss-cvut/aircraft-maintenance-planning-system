#!/bin/bash

DATASET_TYPE_DIR="$(dirname $(dirname $(realpath -s $0)))"

$DATASET_TYPE_DIR/../bin/download-ftp-file.sh History/History_time-analysis.tsv ./target/input.tsv
