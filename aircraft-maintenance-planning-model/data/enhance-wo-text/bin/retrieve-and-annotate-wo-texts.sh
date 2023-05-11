#!/bin/bash

DATASET_TYPE_DIR="$(dirname $(dirname $(realpath $0)))"
DATASET_TYPE="$(basename "${DATASET_TYPE_DIR}")"

# get input from rdf4j repository and save it to target/input.ttl
"$DATASET_TYPE_DIR"/bin/get-input-from-rdf4j-repository.sh