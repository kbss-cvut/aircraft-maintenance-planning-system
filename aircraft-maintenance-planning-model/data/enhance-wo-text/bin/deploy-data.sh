#!/bin/bash

DATASET_TYPE_DIR="$(dirname $(dirname $(realpath -s $0)))"

# deploy slovnik.ttl and target/output.ttl.txt
"$DATASET_TYPE_DIR"/../bin/deploy-rdf.sh slovnik.ttl
"$DATASET_TYPE_DIR"/../bin/deploy-rdf.sh --append-context ./target/output.ttl.txt
