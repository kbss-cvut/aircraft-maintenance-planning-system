#!/bin/bash

DATASET_TYPE_DIR="$(dirname $(dirname $(realpath $0)))"
DATASET_TYPE="$(basename "${DATASET_TYPE_DIR}")"

# get input from rdf4j repository and save it to target/input.ttl
"$DATASET_TYPE_DIR"/bin/get-input-from-rdf4j-repository.sh

# deploy slovnik.ttl and target/output.ttl.txt
"$DATASET_TYPE_DIR"/../bin/deploy-rdf.sh slovnik.ttl
"$DATASET_TYPE_DIR"/../bin/deploy-rdf.sh ./target/output.ttl.txt


