#!/bin/bash

#This script is only for migration from 1.2.2 HA backup to 10.0.0 HA restore
#This also assumes 3 postgres instances in 1.2.2 HA service and 2 postgres instances in 10.0.0 HA service
#Place this script in the top level backup folder containing all backup artifacts from all service instance VMs


if [ $# -ne 1 ]; then
  echo "Script needs 1 argument. Usage: ha-bbr-restore-migration <service deployment name>"
fi

export METADATA_FILES=$(find . -type f | grep "metadata" | while read line ; do echo $line ; done)
echo $METADATA_FILES

yq eval-all '. as $item ireduce ({}; . *+ $item)' $METADATA_FILES > metadata

#Since 10.0.0 HA has only 2 postgres instances, we have to keep metadata of first 2 instances (index: 0, index: 1) and remove others.
#Removing index: 2 here
yq 'del(.instances[] | select(.index == "2"))' metadata > temp-file
cp temp-file metadata

find . -type f | grep ".*tar" | while read line ; do cp $line ./ ; done

SVC_DEPLOYMENT_NAME=$1
bbr deployment --debug  --deployment $SVC_DEPLOYMENT_NAME restore --artifact-path .

