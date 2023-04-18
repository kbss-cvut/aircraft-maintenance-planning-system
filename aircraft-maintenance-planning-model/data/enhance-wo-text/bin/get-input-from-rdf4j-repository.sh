#!/bin/bash

DATASET_TYPE_DIR="$(dirname $(dirname $(realpath -s $0)))"

cd $DATASET_TYPE_DIR

REPOSITORY_ID="csat-data"
SERVER_URL="https://graphdb.onto.fel.cvut.cz"
RDF4J_REPOSITORY_URL="${SERVER_URL}/repositories/${REPOSITORY_ID}"
SPARQL_CONSTRUCT_FILE="./queries/retrieve-mwo-with-wo-texts.rq"
NETRC_FILE=../../private/netrc


#QUERY="CONSTRUCT {?s ?p ?o} WHERE {?s ?p ?o}"
QUERY="$(cat $SPARQL_CONSTRUCT_FILE)"

OUTPUT_FILE="./target/input.ttl"

curl -X POST \
     --netrc-file "$NETRC_FILE"  \
     --header "Accept: text/turtle" \
     --data-urlencode "query=$QUERY" \
     "$RDF4J_REPOSITORY_URL" \
     > "$OUTPUT_FILE"

echo "Turtle file saved to $OUTPUT_FILE"
