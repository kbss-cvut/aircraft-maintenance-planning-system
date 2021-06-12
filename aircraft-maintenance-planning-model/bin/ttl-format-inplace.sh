#!/bin/bash

if [ ! "$#" -eq 1 ]; then
        echo "Override provided TTL_FILE so it contains formated ttl content." >&2
        echo "Usage :  $0  TTL_FILE" >&2
        echo "Example :" >&2
        echo "   echo -e '$0 ./example.ttl" >&2
        exit
fi

FILE="`readlink -f $1`"
DIR="`dirname $FILE`"
FILE_NAME="`basename $FILE`"

TEMP_FILE=`tempfile`

rdfpipe -i "text/turtle" $FILE_NAME > $TEMP_FILE && cp $TEMP_FILE $FILE_NAME
