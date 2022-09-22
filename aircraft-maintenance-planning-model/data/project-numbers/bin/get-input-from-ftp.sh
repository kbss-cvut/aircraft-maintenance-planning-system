#!/bin/bash

DATASET_TYPE_DIR="$(dirname $(dirname $(realpath -s $0)))"

cd $DATASET_TYPE_DIR

../bin/download-ftp-file.sh Dashboard2_project-numbers.csv ./target/input.tsv
