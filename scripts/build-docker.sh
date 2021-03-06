#!/bin/bash

if [ -z "${1}" ]; then
   version="latest"
else
   version="${1}"
fi

docker pull gennyproject/checkrules
docker build --no-cache  -t gennyproject/prj_genny:${version} .
