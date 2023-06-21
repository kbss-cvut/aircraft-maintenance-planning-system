#!/bin/bash

###
### Fixes badly quoted TSV file taken from FTP (i.e. quote (") occurs in the middle of TSV field but is not escaped (by ""))
###

backup-trash.sh $1

sed -i 's/\([^"\t]\)"\([^"\t]\)/\1""\2/g' $1
