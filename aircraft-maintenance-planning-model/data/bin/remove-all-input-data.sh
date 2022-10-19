#!/bin/bash 
source "$SCRIPT_DIR/data-dirs"

SCRIPT_DIR=$(dirname $(readlink -m $0))
for DATA_DIR in ${DATA_DIRS[*]}; do 
    rm "$SCRIPT_DIR/../$DATA_DIR/target/input.csv"
done