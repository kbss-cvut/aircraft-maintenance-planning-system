#!/bin/bash
  
DATASET_TYPE_DIR="$(dirname $(dirname $(realpath -s $0)))"

$DATASET_TYPE_DIR/../bin/download-ftp-file.sh Dashboard2_WO-TC-ref.csv ./target/input.tsv
