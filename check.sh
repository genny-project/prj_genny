#!/bin/bash
branch=`git branch | grep \* | awk '{print $2}'`
echo "Current branch is ${branch}, will use checkrules:${branch}"
docker pull gennyproject/checkrules:${branch}
docker run -v "$PWD/rules:/rules"  gennyproject/checkrules:${branch}  -a -r /rules

