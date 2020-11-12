#!/bin/bash

function ReadPomMajorMinorNumbers {
    currentVersion=$(sed -n '/<version>.*<\/version>/ s/<version>\(.*\)<\/version>/\1/p' pom.xml)
    semVerComponents=( ${currentVersion//-/ } )
    semVerComponents=${semVerComponents[0]}
    semVerComponents=( ${semVerComponents//./ } )
    
    pomMajorNumber=${semVerComponents[0]}
    pomMinorNumber=${semVerComponents[1]}
    pomPatchNumber="$(git rev-list --count origin/master..)"
}

ReadPomMajorMinorNumbers

branch="$(git symbolic-ref --short HEAD)"
case "$branch" in
    beta)
        echo "## Is BETA branch, add +100 to major number"
        
        pomMajorNumber=$(expr $pomMajorNumber + 100)
        
        newVersion="$pomMajorNumber.$pomMinorNumber-SNAPSHOT"
        ;;

    beta-corona)
        echo "## Is BETA-CORONA branch, use fixed version"
        
        pomMajorNumber="116"
        pomMinorNumber="0"
        

        newVersion="$pomMajorNumber.$pomMinorNumber.$pomPatchNumber"
        ;;

    release-*)
        echo "## Is RELEASE/UPGRADE branch, use pom.xml version modifing patch number"

        newVersion="$pomMajorNumber.$pomMinorNumber.$pomPatchNumber"
        ;;

    *)
        echo "## Is MASTER or feature branch, use pom.xml version"
        ;;
esac

if [[ -n "${newVersion}" ]]; then
    echo "## Updating pom.xml version to: $newVersion"
    mvn -B versions:set -DgenerateBackupPoms=false -DnewVersion="$newVersion"
fi

# Add current commit's SHA to pom.xml
GIT_HASH=$(git rev-parse HEAD)
scmversion="<vcm_hash>$GIT_HASH</vcm_hash>"
scmv=$(echo $scmversion | sed 's/\//\\\//g')
sed -i "/<\/properties>/ s/.*/${scmv}\n&/" pom.xml
