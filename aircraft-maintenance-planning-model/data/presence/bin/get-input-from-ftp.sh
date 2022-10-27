#!/bin/bash

DATASET_TYPE_DIR="$(dirname $(dirname $(realpath -s $0)))"

cd $DATASET_TYPE_DIR

../bin/download-ftp-file.sh pritomnost.csv ./target/input.csv
