#!/bin/bash

DATASET_TYPE_DIR="$(dirname $(dirname $(realpath -s $0)))"
DATASET_TYPE="$(basename "${DATASET_TYPE_DIR}")"

"$DATASET_TYPE_DIR"/../bin/retrieve-data.sh "$DATASET_TYPE"
