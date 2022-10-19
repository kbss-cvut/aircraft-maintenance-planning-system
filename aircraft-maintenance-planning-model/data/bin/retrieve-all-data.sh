#!/bin/bash
# If the parameter is equal to "--download", the script automatically download files from ftp
DOWNLOAD=$1
SCRIPT_DIR=$(dirname $(readlink -m $0))

source "$SCRIPT_DIR/data-dirs"

for index in ${!FILE_NAMES[*]}; do
	FILE_NAME=${FILE_NAMES[$index]}
 	DATA_DIR=${DATA_DIRS[$index]}

	if [[ $# -eq 1 && "$1" == "--download" ]]; then
		# download ftp files
		$(echo "$(echo $SCRIPT_DIR/download-ftp-file.sh) $FILE_NAME $DATA_DIR")  
	fi
	
	# retrieve data
	$(echo "$(echo $SCRIPT_DIR/../$DATA_DIR/bin/retrieve-data.sh)") 	
done
