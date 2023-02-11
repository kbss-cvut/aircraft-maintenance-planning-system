#!/bin/bash -e
SCRIPT_DIR=$(dirname $(readlink -m $0))

source "$SCRIPT_DIR/data-dirs"

for index in ${!DATA_DIRS[*]}; do
	DATA_DIR_NAME=${DATA_DIRS[$index]}
	cd $DATA_DIR_NAME

	./bin/get-input-from-ftp.sh

	cd -
done
