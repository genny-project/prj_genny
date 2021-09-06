#!/bin/bash
docker run -v  "$PWD/rules:/rules"   gennyproject/checkrules  -a -r /rules

