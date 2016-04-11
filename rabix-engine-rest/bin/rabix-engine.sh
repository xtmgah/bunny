#!/bin/sh

command="java -jar ./lib/rabix-engine-0.0.1-SNAPSHOT.jar"
for i in "$@"
do
    command="$command $i"
done

eval $command