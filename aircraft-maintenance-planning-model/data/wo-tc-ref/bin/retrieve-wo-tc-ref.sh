#!/bin/bash


mkdir -p ./target

SPIPES_SERVICE=http://localhost:8080/s-pipes

WORKSPACE_DIR=/home/blcha/projects/20doprava-csat/git/aircraft-maintenance-planning-system/aircraft-maintenance-planning-model
DATASET_TYPE=wo-tc-ref
#DATASET_URL="https://docs.google.com/spreadsheets/d/1D7bI_0LsmgMvv-GFSZ2pQ_Pd0O2qegd-LWScR4gfGFw/export?format=csv&#gid=1994218813"
DATASET_URL="https://docs.google.com/spreadsheets/d/1SPNR5T0drTc9Axnqzc193XjNPzyOSjnAx-pUGz1J0Nw/export?format=csv&#gid=977875978"
OUTPUT_FILE=./target/output.jsonld
FUNCTION_ID=transform-$DATASET_TYPE-data



echo "==================================="
echo "INFO: s-pipes service url $SPIPES_SERVICE"
echo "INFO: function id $FUNCTION_ID"
echo "INFO: workspace dir $WORKSPACE_DIR"
echo "INFO: dataset type $DATASET_TYPE"
echo "INFO: dataset url $DATASET_URL"
echo "==================================="



DATASET_URL_ENCODED="`echo $DATASET_URL | urlencode.sh`"

URL="$SPIPES_SERVICE/service?_pId=$FUNCTION_ID&workspaceDir=$WORKSPACE_DIR&datasetType=$DATASET_TYPE&datasetUrl=$DATASET_URL_ENCODED"    

set -x

#curl  -H 'Accept: text/json, text/plain, */*'  "$URL" > $OUTPUT_FILE
curl  -H 'Accept: text/turtle'  "$URL" > $OUTPUT_FILE

set +x
