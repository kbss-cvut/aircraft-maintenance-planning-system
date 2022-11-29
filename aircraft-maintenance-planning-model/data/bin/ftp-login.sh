#!/bin/bash

SCRIPT_DIR=$(dirname $(readlink -m $0))


# You have to provide token and hostname in "../private/ftp-config" file to access the FTP server
# The file should contain two lines: the first line is the token, the second line is the hostname
{ read -r CREDENTIALS; read -r HOSTNAME; } < $SCRIPT_DIR/../private/ftp-config

if [[ -z $CREDENTIALS || -z $HOSTNAME ]]; then
  echo "Missing credentials or hostname. Provide them in $SCRIPT_DIR/../private/ftp-config file."
  exit
fi

sshpass -p $CREDENTIALS sftp $HOSTNAME
