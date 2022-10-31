#!/bin/bash -e
# If the parameter is equal to "--download", the script automatically download files from ftp
DOWNLOAD=$1
SCRIPT_DIR=$(dirname $(readlink -m $0))

source "$SCRIPT_DIR/data-dirs"

for index in ${!DATA_DIRS[*]}; do
	DATA_DIR_NAME=${DATA_DIRS[$index]}
	cd $DATA_DIR_NAME

	if [[ $# -eq 1 && "$1" == "--download" ]]; then
		# download ftp files
		./bin/get-input-from-ftp.sh
	fi
	
	# retrieve data
	./bin/retrieve-data.sh

	cd -
done
