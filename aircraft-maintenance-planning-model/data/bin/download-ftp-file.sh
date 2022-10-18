#!/bin/bash

INPUT_FILE_PATH=$1
OUTPUT_FILE_PATH=$2

SCRIPT_DIR=$(dirname $(readlink -m $0))
OUTPUT_FILE_PATH=$SCRIPT_DIR/../$OUTPUT_FILE_PATH/target/input.csv

#INPUT_FILE=Dashboard2_time-analysis.csv
#OUTPUT_FILE=./target/input.tsv

INPUT_FILE_NAME=$(echo $INPUT_FILE_PATH | sed 's|.*/||')

# You have to provide token and hostname in "../private/ftp-config" file to access the FTP server
# The file should contain two lines: the first line is the token, the second line is the hostname
{ read -r CREDENTIALS; read -r HOSTNAME; } < $SCRIPT_DIR/../private/ftp-config

if [[ -z $CREDENTIALS || -z $HOSTNAME ]]; then
  echo "Missing credentials or hostname. Provide them in $SCRIPT_DIR/../private/ftp-config file."
  exit
fi

$(echo "sshpass -p $CREDENTIALS sftp $HOSTNAME:$INPUT_FILE_NAME")
# create output directory if does not exists
mkdir -p $(dirname $OUTPUT_FILE_PATH)

cat $INPUT_FILE_NAME > $OUTPUT_FILE_PATH

rm $INPUT_FILE_NAME
