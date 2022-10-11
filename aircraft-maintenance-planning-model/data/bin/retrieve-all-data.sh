#!/bin/bash 

DATA_DIRS="
presence
project-numbers
time-analysis
wo-tc-ref
defect-class
time-estimate
wp-catalog
"

for DATA in $DATA_DIRS; do
	cd $DATA
	./bin/retrieve-data.sh
done
