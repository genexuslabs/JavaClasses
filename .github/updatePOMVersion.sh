#!/bin/bash

scmversion="<vcm_hash>$GIT_HASH</vcm_hash>"
scmv=$(echo $scmversion | sed 's/\//\\\//g')
sed -i "/<\/properties>/ s/.*/${scmv}\n&/" pom.xml