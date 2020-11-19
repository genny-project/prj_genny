#!/bin/bash
realm=$1
export PROJECT_REALM=${realm}; mvn clean compiler:testCompile surefire:test  -DskipTests=true
#export PROJECT_REALM=${realm}; mvn clean compiler:testCompile surefire:test  -Dtest=AdamTest test
