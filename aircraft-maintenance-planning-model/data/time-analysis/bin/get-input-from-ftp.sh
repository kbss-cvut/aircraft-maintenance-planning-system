#!/bin/bash

DATASET_TYPE_DIR="$(dirname $(dirname $(realpath -s $0)))"

$DATASET_TYPE_DIR/../bin/download-ftp-file.sh Dashboard2_time-analysis.csv ./target/input.tsv
