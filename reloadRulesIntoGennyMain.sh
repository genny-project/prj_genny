#!/bin/bash
rm -Rf ..genny-main/rules/prj_*
#mkdir rules
for i in ` find .. -mindepth 1 -maxdepth 1 -type d | grep prj  | awk -F "/" '{ print $2 }'`;do
   mkdir -p ../geenny-main/rules/$i
   cp -rp ../$i/rules ../genny-main/rules/$i/
  echo $i
done
