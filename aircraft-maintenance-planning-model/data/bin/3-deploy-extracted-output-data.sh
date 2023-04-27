#!/bin/bash
SCRIPT_DIR=$(dirname $(readlink -m $0))

SERVER=https://graphdb.onto.fel.cvut.cz
REPOSITORY_NAME=csat-recovery-update-2
#REPOSITORY_NAME=mbtest
LOG_FILE=deploy-extracted-output-data.log

INPUT_ROOT_DIR=./private/3-ttl-data

source "$SCRIPT_DIR/data-dirs"

DATA_DIRS=(
    time-analysis
    time-estimate
    wo-tc-ref
    wp-catalog
    project-numbers
    defect-class
    presence
)

log_info() { 
	echo $(date +%F--%H:%M:%S) INFO: $* | tee >> $LOG_FILE 
}

log_error() { 
	echo $(date +%F--%H:%M:%S) ERROR: $* | tee >> $LOG_FILE 
}

log_with_date() {
	cat | sed "s/^/$(date +%F--%H:%M:%S) /g" | tee >> $LOG_FILE
}


############
### MAIN ###
############
log_info "Executing $0"

for index in ${!DATA_DIRS[*]}; do
        DATA_DIR_NAME=${DATA_DIRS[$index]}

	
	ls -d -1 $INPUT_ROOT_DIR/20* | sort | while read DIR; do

        	TIMESTAMP=$(basename $(basename $DIR))


        	INPUT_DIR=$INPUT_ROOT_DIR/$TIMESTAMP
		ONTOLOGY_FILE="$INPUT_DIR/$DATA_DIR_NAME.ttl"
		METADATA_ONTOLOGY_FILE="$INPUT_DIR/metadata/$DATA_DIR_NAME.ttl"
		
		if [ ! -f "$ONTOLOGY_FILE" ]; then
        		log_info "Skiping dataset $DATA_DIR_NAME from $TIMESTAMP ..."
			continue;
		fi

        	ONTOLOGY_IRI=http://onto.fel.cvut.cz/dataset--$TIMESTAMP--$DATA_DIR_NAME
        	log_info "Deploying dataset $DATA_DIR_NAME from $TIMESTAMP ..."

		if [ ! -f "$METADATA_ONTOLOGY_FILE" ]; then	
        		log_error "Skiping -- missing metadata file $METADATA_ONTOLOGY_FILE for dataset $DATA_DIR_NAME from $TIMESTAMP ..."
			continue;
		fi
			
	        ../bin/rdf4j-deploy-context.sh -R -C 'text/turtle' -s $SERVER -r $REPOSITORY_NAME -c $ONTOLOGY_IRI $ONTOLOGY_FILE $METADATA_ONTOLOGY_FILE 2>&1 | log_with_date
	done
done
