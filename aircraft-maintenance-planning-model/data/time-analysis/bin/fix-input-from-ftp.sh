#!/bin/bash

###
### Fixes badly quoted TSV file taken from FTP (i.e. quote (") occurs in the middle of TSV field but is not escaped (by ""))
###

DATASET_TYPE_DIR="$(dirname $(dirname $(realpath -s $0)))"

cd $DATASET_TYPE_DIR

./bin/get-input-from-ftp.sh
sed -i 's/\([^"\t]\)"\([^"\t]\)/\1""\2/g' target/input.csv

if [[ $# -eq 1 && "$1" == "--deploy" ]]; then
     # fix also in FTP 
     ../bin/push-ftp-file.sh ./target/input.csv Dashboard2_time-analysis.csv 
fi

