#!/bin/bash
SCRIPT_DIR=$(dirname $(readlink -m $0))

INPUT_DATA_ROOT_DIR=./private/2-input-tabular-data
OUTPUT_ROOT_DIR=./private/3-ttl-data

#source "$SCRIPT_DIR/data-dirs"

DATA_DIRS=(
    time-analysis
    time-estimate
    wo-tc-ref
    wp-catalog
    project-numbers
    defect-class
    presence
)


WARN_ON_FAIL_AND_SLEEP=10

for index in ${!DATA_DIRS[*]}; do
        DATA_DIR_NAME=${DATA_DIRS[$index]}

	ls -d -1 $INPUT_DATA_ROOT_DIR/20* | sort | while read DIR; do

        	TIMESTAMP=$(basename $(basename $DIR))

        	echo "INFO: Extracting dataset $DATA_DIR_NAME from $TIMESTAMP ..."

        	INPUT_DIR=$INPUT_DATA_ROOT_DIR/$TIMESTAMP
        	OUTPUT_DIR=$OUTPUT_ROOT_DIR/$TIMESTAMP
        	mkdir -p $OUTPUT_DIR

		INPUT_FILE_NAME=$(cat $DATA_DIR_NAME/bin/get-input-from-ftp.sh | grep download-ftp | sed 's/.* \([^ ]*.csv\).*input.csv/\1/')
		INPUT_FILE_PATH=$INPUT_DIR/$INPUT_FILE_NAME

		### if already computed skip
		if [ -f "$OUTPUT_DIR/$DATA_DIR_NAME.ttl" ]; then
       			echo "INFO: skipping, already computed."
			continue;
		fi
		
		### restart docker

		### retrieve data
		if [ ! -f $INPUT_FILE_PATH ]; then
			echo "DEBUG: No dataset found in $TIMESTAMP (File $INPUT_FILE_PATH does not exist.)"
			continue;
		fi
        	rm -f $DATA_DIR_NAME/target/output.ttl.txt
        	rm -f $DATA_DIR_NAME/target/input.csv
		cp -v $INPUT_FILE_PATH $DATA_DIR_NAME/target/input.csv
		cd $DATA_DIR_NAME
		./bin/retrieve-data.sh
		cd -
        	
		cp -v $DATA_DIR_NAME/target/output.ttl.txt $OUTPUT_DIR/$DATA_DIR_NAME.ttl

		### dump output of docker	

		### log if extraction failed
		if [ ! -s "$OUTPUT_DIR/$DATA_DIR_NAME.ttl" ]; then
       			echo "WARN: extraction failed."
			if [ "$WARN_ON_FAIL_AND_SLEEP" ]; then  
				echo "WARN: waiting $WARN_ON_FAIL_AND_SLEEP seconds until resuming. Hit Ctrl-C to quit processing."
				./bin/2.1-remove-empty-files-in-output-data.sh
				sleep $WARN_ON_FAIL_AND_SLEEP	
				echo "Continuing ..."
			fi
			continue;
		fi
	done
done



