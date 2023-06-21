#!/bin/bash
SCRIPT_DIR=$(dirname $(readlink -m $0))

INPUT_EML_TABULAR_DATA=./private/1-eml-tabular-data
OUTPUT_TABULAR_DIR=./private/2-input-tabular-data

cp -v -r $INPUT_EML_TABULAR_DATA/20* $OUTPUT_TABULAR_DIR/
mv -v  $INPUT_EML_TABULAR_DATA/20* $INPUT_EML_TABULAR_DATA/done/
