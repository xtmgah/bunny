#!/bin/sh

command="java -Dlogback.configurationFile=$(dirname $0)/config/logback.xml 2>/dev/null -jar $(dirname $0)/lib/rabix-backend-local-0.0.1-SNAPSHOT.jar 2>/dev/null"
for i in "$@"
do
    command="$command $i"
done

eval $command