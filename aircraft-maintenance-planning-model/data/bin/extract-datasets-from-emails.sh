#!/bin/bash

DIR="$(dirname $(realpath -s $0))"

cd $DIR/..


INPUT_EML_DATA_DIR=./private/input-eml-data
OUTPUT_DATA_DIR=./private/eml-data-output

# Input date and time values
DATE_STR='2023-01-16'
TIME_STR='0504'

# Create ISO 8601 timestamp

echo ${iso_timestamp}


mkdir -p $OUTPUT_DATA_DIR

ls $INPUT_EML_DATA_DIR/* | sort | while read EML_FILE; do
	EML_FILE_NAME=$(basename "$EML_FILE")
	DATASET_NAME=$(echo $EML_FILE_NAME | sed 's/ - .csatechnics.*//' | sed 's/Dashboard2_//')
	DATE_STR=$(echo $EML_FILE_NAME | sed 's/.*csatechnics.com..-.//' | sed 's/ .*//')
	TIME_STR=$(echo $EML_FILE_NAME | sed 's/.*csatechnics.com..-.[^ ]* //' | sed 's/.eml//')
	ISO_TIMESTAMP=$(date -u -d "${DATE_STR} ${TIME_STR}" +"%Y-%m-%dT%H:%M:%S.%N")'Z'

	TIMESTAMP_DIR=$OUTPUT_DATA_DIR/$ISO_TIMESTAMP
	echo "INFO: Uploading into $ISO_TIMESTAMP -- dataset $DATASET_NAME"
	mkdir -p $TIMESTAMP_DIR
	ripmime --overwrite -i "$EML_FILE" -d $TIMESTAMP_DIR
	rm -f $TIMESTAMP_DIR/textfile*
done
