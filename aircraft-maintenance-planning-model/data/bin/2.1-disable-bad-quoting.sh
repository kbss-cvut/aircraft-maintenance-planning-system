#!/bin/bash

###
### Disable badly quoted TSV file taken from FTP (i.e. quote (") occurs in the middle of TSV field but is not escaped (by ""))
###

mv -v $1 $1.bad-quoting

