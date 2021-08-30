#!/bin/bash

mkdir -p ./target

SPIPES_SERVICE=http://localhost:8080/s-pipes

WORKSPACE_DIR=/home/blcha/projects/20doprava-csat/git/aircraft-maintenance-planning-system/aircraft-maintenance-planning-model
DATASET_TYPE=wo-tc-ref
INPUT_FILE_NAME=input.csv
INPUT_FILE=./target/$INPUT_FILE_NAME
OUTPUT_FILE=./target/output.jsonld
FUNCTION_ID=transform-$DATASET_TYPE-data


echo "==================================="
echo "INFO: s-pipes service url $SPIPES_SERVICE"
echo "INFO: function id $FUNCTION_ID"
echo "INFO: workspace dir $WORKSPACE_DIR"
echo "INFO: dataset type $DATASET_TYPE"
echo "INFO: input file $INPUT_FILE"
echo "==================================="


#DATASET_URL_ENCODED="`echo $DATASET_URL | urlencode.sh`"

URL="$SPIPES_SERVICE/service?_pId=$FUNCTION_ID&workspaceDir=$WORKSPACE_DIR&datasetType=$DATASET_TYPE&datasetResource=@$INPUT_FILE_NAME"    

set -x

curl --location --request POST  \
	--header 'Accept: text/turtle' \
	--form 'files=@"/home/blcha/projects/20doprava-csat/git/aircraft-maintenance-planning-system/aircraft-maintenance-planning-model/data/wo-tc-ref/target/input.csv"' \
	"$URL" > $OUTPUT_FILE

set +x

