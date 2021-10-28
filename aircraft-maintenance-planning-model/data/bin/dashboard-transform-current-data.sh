#!/bin/bash -x

DIR="$(dirname $(realpath -s $0))"

cd $DIR/..

DATASET_LIST="
time-analysis
wo-tc-ref
time-estimate
presence
"

for DATASET in $DATASET_LIST; do
	$DATASET/bin/get-input-from-ftp.sh
	$DATASET/bin/retrieve-data.sh
done
