#!/bin/bash

#Place this script in the top level backup folder containing all backup artifacts from all service instance VMs

if [ $# -ne 1 ]; then
  echo "Script needs 1 argument. Usage: ha-bbr-restore <service deployment name>"
fi

export METADATA_FILES=$(find . -type f | grep "metadata" | while read line ; do echo $line ; done)

yq eval-all '. as $item ireduce ({}; . *+ $item)' $METADATA_FILES > metadata

find . -type f | grep ".*tar" | while read line ; do cp $line ./ ; done

SVC_DEPLOYMENT_NAME=$1
bbr deployment --debug  --deployment $SVC_DEPLOYMENT_NAME restore --artifact-path .

