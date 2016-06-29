#!/bin/sh

# Setup variables
EXEC=/usr/bin/jsvc
JAVA_HOME=/usr/lib/jvm/java-7-oracle
CLASS_PATH="/home/janko/Desktop/engine/lib/rabix-engine-rest-0.0.1-SNAPSHOT.jar"
CLASS=org.rabix.engine.rest.EngineRestEntryDaemon
USER=janko
PID=/tmp/bunny.pid
LOG_OUT=/tmp/bunny.out
LOG_ERR=/tmp/bunny.err
CONFIG_DIR=/home/janko/Desktop/config

do_exec()
{
    $EXEC -debug -jvm server -home "$JAVA_HOME" -cp $CLASS_PATH -user $USER -outfile $LOG_OUT -errfile $LOG_ERR -pidfile $PID $1 $CLASS $CONFIG_DIR
}

case "$1" in
    start)
        do_exec
            ;;
    stop)
        do_exec "-stop"
            ;;
    restart)
        if [ -f "$PID" ]; then
            do_exec "-stop"
            do_exec
        else
            echo "service not running, will do nothing"
            exit 1
        fi
            ;;
    *)
            echo "usage: daemon {start|stop|restart}" >&2
            exit 3
            ;;
esac