#!/bin/bash

DATASET_TYPE_DIR="$(dirname $(dirname $(realpath -s $0)))"

cd $DATASET_TYPE_DIR

REPOSITORY_ID="csat-data"
SERVER_URL="https://graphdb.onto.fel.cvut.cz"
RDF4J_REPOSITORY_URL="${SERVER_URL}/repositories/${REPOSITORY_ID}"

# if the parameter --all is present, then the query will retrieve all the WO texts
if [[ "$1" == "--all" ]]; then
  SPARQL_CONSTRUCT_FILE="./queries/retrieve-mwo-with-wo-texts.rq"
else
  SPARQL_CONSTRUCT_FILE="./queries/retrieve-non-annotated-mwo-with-wo-texts.rq"
fi

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
