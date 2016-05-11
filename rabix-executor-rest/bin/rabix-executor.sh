#!/bin/sh

command="java -jar ./lib/rabix-executor-rest-0.0.1-SNAPSHOT.jar --configuration-dir ./config"
for i in "$@"
do
    command="$command $i"
done

eval $command