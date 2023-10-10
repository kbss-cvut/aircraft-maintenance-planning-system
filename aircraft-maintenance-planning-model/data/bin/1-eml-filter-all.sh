#!/bin/bash

# prepare eml tabular data for next processsing 
# 	copy "eml tabular data" --> "input tabular data" + mark "eml tabular data"

SCRIPT_DIR=$(dirname $(readlink -m $0))

INPUT_EML_TABULAR_DATA=./private/1-eml-tabular-data
OUTPUT_TABULAR_DIR=./private/2-input-tabular-data

echo 'INFO: Setting eml tabular data as input data for next processing'
cp -v -r $INPUT_EML_TABULAR_DATA/20* $OUTPUT_TABULAR_DIR/

echo 'INFO: Marking eml tabular data as processed (DONE)'
mkdir -p $INPUT_EML_TABULAR_DATA/done/
mv -v  $INPUT_EML_TABULAR_DATA/20* $INPUT_EML_TABULAR_DATA/done/
