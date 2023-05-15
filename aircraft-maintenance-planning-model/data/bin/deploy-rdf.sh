#!/bin/bash

SERVER=https://graphdb.onto.fel.cvut.cz
REPOSITORY_NAME=csat-data

REPLACE_FLAG="$1"
REPLACE_PARAMETER="-R"

if [ "$REPLACE_FLAG" = "--append-context" ]; then
  REPLACE_PARAMETER=""
  shift
fi
FILE="$1"

ls "$FILE" | while read -r ONTOLOGY_FILE; do
  ONTOLOGY_IRI=$(awk '/(rdf:type|a).*owl:Ontology/{print prev1; print $0; getline; print; exit} {prev1 = $0}' "$ONTOLOGY_FILE" | awk -F'[<>]' '/^<http:/{print $2; exit}')
  echo INFO: Deploying "$ONTOLOGY_FILE" into context "$ONTOLOGY_IRI" ...
	 ../../bin/rdf4j-deploy-context.sh "$REPLACE_PARAMETER" -C 'text/turtle' -s "$SERVER" -r "$REPOSITORY_NAME" -c "$ONTOLOGY_IRI" "$ONTOLOGY_FILE"
done