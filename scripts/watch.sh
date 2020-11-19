#!/bin/bash
fswatch --batch-marker=EOF -xn  ./rules | while read file event; do 
   echo $file 
   foo=${file#"$PWD"}
   echo $foo
   if [ $file = "EOF" ]; then 
      echo $PWD
#      docker run -v  "$PWD/rules:/rules"   gennyproject/checkrules -d  "${foo}" 
      result=$(docker run -v  "$PWD/rules:/rules"   gennyproject/checkrules  -a -r /rules) | grep 'Compilation error'
 	say $result 
 fi
done

