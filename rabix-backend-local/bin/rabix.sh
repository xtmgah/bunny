#!/bin/sh

loggingConfiguration=$(dirname $0)/config/logback.xml
for var in "$@"
do
    if [ "$var" = "-v" -o "$var" = "-verbose" ]
    then
        loggingConfiguration=$(dirname $0)/config/logback-verbose.xml
    fi
done

command="java -Dlogback.configurationFile=${loggingConfiguration} 2>/dev/null -jar $(dirname $0)/lib/rabix-backend-local-0.0.1-SNAPSHOT.jar 2>/dev/null"

for i in "$@"
do
    command="$command $i"
done

eval $command