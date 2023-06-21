#!/bin/bash
SCRIPT_DIR=$(dirname $(readlink -m $0))

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
	#echo $(date +%F--%H:%M:%S) INFO: $* | tee >> $LOG_FILE 
	echo $(date +%F--%H:%M:%S) INFO: $*
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

		OUTPUT_METADATA_DIR="$INPUT_DIR/metadata"
		OUTPUT_METADATA_FILE="$OUTPUT_METADATA_DIR/$DATA_DIR_NAME.ttl"

		mkdir -p $OUTPUT_METADATA_DIR
		rm -f "$OUTPUT_METADATA_FILE"
		
		
		if [ ! -f "$ONTOLOGY_FILE" ]; then
        		log_info "Skiping -- no computed dataset $DATA_DIR_NAME from $TIMESTAMP ..."
			continue;
		fi

 		HAS_TRIPLES="$(cat "$INPUT_DIR/$DATA_DIR_NAME.ttl" | grep -v @prefix | grep -v "^$" | head -1)"
		if [ -z "$HAS_TRIPLES" ]; then 
        		log_info "Skiping -- no triples found in \"$INPUT_DIR/$DATA_DIR_NAME.ttl\" ..."
			continue;
		fi

        	log_info "Adding metadata info to dataset $DATA_DIR_NAME from $TIMESTAMP -- file $OUTPUT_METADATA_FILE..."
		echo "<http://onto.fel.cvut.cz/dataset--$TIMESTAMP--$DATA_DIR_NAME> a <http://onto.fel.cvut.cz/ontologies/csat-maintenance/dataset> ;" > "$OUTPUT_METADATA_FILE"
		echo "    <http://onto.fel.cvut.cz/ontologies/csat-maintenance/dataset-id> \"$DATA_DIR_NAME\" ;" >> "$OUTPUT_METADATA_FILE"
		echo "    <http://onto.fel.cvut.cz/ontologies/csat-maintenance/dataset-timestamp> \"$TIMESTAMP\" ;" >> "$OUTPUT_METADATA_FILE" 
		echo "." >> "$OUTPUT_METADATA_FILE" 
	done
done
