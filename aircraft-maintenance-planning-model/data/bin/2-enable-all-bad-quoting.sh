#!/bin/bash

###
### Fixes badly quoted TSV file taken from FTP (i.e. quote (") occurs in the middle of TSV field but is not escaped (by ""))
###


find ./private/input-data/ -name '*.bad-quoting' | while read ORIGINAL_FILE; do 
	CORRECTED_FILE=$(echo $ORIGINAL_FILE | sed 's/.bad-quoting$//')

	if [ -f $CORRECTED_FILE ]; then
		echo INFO: Skipping $CORRECTED_FILE as it seems like fixed.
		continue
	fi
	cp -v $ORIGINAL_FILE $CORRECTED_FILE
	#sed -i 's/\(\t[^\t"]*\)"\([^\t"]*\t\)/\1""\2/' $CORRECTED_FILE
	#meld $ORIGINAL_FILE $CORRECTED_FILE
done
