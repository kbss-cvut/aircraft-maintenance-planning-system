#!/bin/bash

DATASET_TYPE_DIR="$(dirname $(dirname $(realpath -s $0)))"

cd $DATASET_TYPE_DIR

../bin/download-ftp-file.sh pritomnost.csv ./target/input.tsv

#sed -i '1iEmployee No;status_1;status_2;date;time' ./target/input.tsv 
sed -i '1s/^/Employee No;status_1;status_2;date;time\n/' ./target/input.tsv
