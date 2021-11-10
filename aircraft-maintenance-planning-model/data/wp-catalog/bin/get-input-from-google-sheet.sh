#!/bin/bash

DATASET_TYPE_DIR="$(dirname $(dirname $(realpath -s $0)))"
DATASET_TYPE="$(basename ${DATASET_TYPE_DIR})"
GOOGLE_SHEET_URL=$(sed -n 's/^URL=//p' $DATASET_TYPE_DIR/private/input-google-drive-stylesheet.url)

$DATASET_TYPE_DIR/../bin/get-input-from-google-sheet.sh $DATASET_TYPE $GOOGLE_SHEET_URL 

