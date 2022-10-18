#!/bin/bash 

INPUT_FILE_NAME=input.csv
INPUT_FILE=./target/$INPUT_FILE_NAME

if [ ! "$#" -eq 1 ]; then
        echo Retrieve DATASET_TYPE data based on existing tsv file located in file $INPUT_FILE wtihin DATASET_TYPE directory.
        echo Usage :  $0 "DATASET_TYPE" 
        echo Example:
        echo "  $0 time-analysis"
        exit
fi


DATASET_TYPE="$1"

SPIPES_SERVICE=http://localhost:8080/s-pipes
#SPIPES_SERVICE=https://kbss.felk.cvut.cz/s-pipes-csat

DIR="$(dirname $(realpath -s $0))"

OUTPUT_FILE=./target/output.ttl.txt
FUNCTION_ID=transform-$DATASET_TYPE-data

echo "==================================="
echo "INFO: s-pipes service url $SPIPES_SERVICE"
echo "INFO: function id $FUNCTION_ID"
echo "INFO: dataset type $DATASET_TYPE"
echo "INFO: input file $INPUT_FILE"
echo "==================================="

cd $DIR/../$DATASET_TYPE
mkdir -p ./target

URL="$SPIPES_SERVICE/service?_pId=$FUNCTION_ID&datasetResource=@$INPUT_FILE_NAME"    
INPUT_FILE_ABSOLUTE="`realpath $INPUT_FILE`"

set -x

curl --location --request POST  \
	--header 'Accept: text/turtle' \
	--form 'files=@"'${INPUT_FILE_ABSOLUTE}'"' \
	"$URL" > $OUTPUT_FILE

set +x

