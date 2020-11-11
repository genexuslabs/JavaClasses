#!/bin/bash

GIT_HASH=$(git rev-parse HEAD)
if ! [ "$(git symbolic-ref --short HEAD)" = "beta" ]; then
  echo "## Is MASTER or UPGRADE  branch, use pom.xml version"
else
  currentversion=$(sed -n '/<version>.*-SNAPSHOT<\/version>/ s/<version>\(.*\)-SNAPSHOT<\/version>/\1/p' pom.xml)
  devversionname="$currentversion-${GIT_HASH:0:7}-SNAPSHOT"
  echo "## Is DEVELOPER branch, update pom.xml version to: $devversionname"
  mvn -B versions:set -DgenerateBackupPoms=false -DnewVersion="$devversionname"
fi

scmversion="<vcm_hash>$GIT_HASH</vcm_hash>"
scmv=$(echo $scmversion | sed 's/\//\\\//g')
sed -i "/<\/properties>/ s/.*/${scmv}\n&/" pom.xml