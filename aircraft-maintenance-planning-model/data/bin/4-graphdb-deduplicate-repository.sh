#!/bin/bash

if [[ $# -ne 1 ]]; then
  echo "Usage: $0 <repository_id>"
  echo "Example: $0 mbtest"
  exit 1
fi

REPOSITORY_ID="$1"

NETRC_FILE=../private/netrc 
SERVER_URL="https://graphdb.onto.fel.cvut.cz"
GRAPHDB_REPOSITORY_URL="${SERVER_URL}/repositories/${REPOSITORY_ID}"

curl --netrc-file "$NETRC_FILE" $GRAPHDB_REPOSITORY_URL/contexts 2>/dev/null | tail +2 | sed -e "s/\r//g" | sort -r > private/graphs.csv

cat private/graphs.csv | while read GRAPH; do
	rm ./queries/deduplicate-triples.uq
	cp ./queries/deduplicate-triples-simple.uq ./queries/deduplicate-triples.uq
	sed -i "s|?newerG|<$GRAPH>|g" ./queries/deduplicate-triples.uq

	echo "INFO: Considering newer graph $GRAPH ..."
	REMOVED_TRIPLES=1
	while [ "$REMOVED_TRIPLES" -gt 0 ]; do
	  
	  	EXPLICIT_TRIPLES_START_COUNT=$(curl --netrc-file "$NETRC_FILE" $GRAPHDB_REPOSITORY_URL/size 2>/dev/null)
	  	./bin/graphdb-deduplicate-repository.sh $REPOSITORY_ID
	  	EXPLICIT_TRIPLES_END_COUNT=$(curl --netrc-file "$NETRC_FILE" $GRAPHDB_REPOSITORY_URL/size 2>/dev/null)
		REMOVED_TRIPLES=$(echo "($EXPLICIT_TRIPLES_START_COUNT-$EXPLICIT_TRIPLES_END_COUNT)" | bc)
		echo "INFO: Removed triples -- $REMOVED_TRIPLES ..."
	done
done
