#!/bin/bash
#
# Init script for graylog2-web-interface
#
# chkconfig: - 85 15
# description: Init script for graylog2-web-interface

# Source function library.
. /etc/rc.d/init.d/functions

if [ -f /etc/sysconfig/graylog2-web-interface ]; then
    . /etc/sysconfig/graylog2-web-interface
fi

prog=graylog2-web-interface
RETVAL=0
GRAYLOG2_WEB_PORT=${GRAYLOG2_WEB_PORT:-5000}
GRAYLOG2_WEB_USER=${GRAYLOG2_WEB_USER:-graylog2-web-interface}
GRAYLOG2_WEB_HOME=${GRAYLOG2_WEB_HOME:-/usr/share/graylog2-web-interface}
GRAYLOG2_WEB_ENV=${GRAYLOG2_WEB_ENV:-production}
GRAYLOG2_WEB_PID=${GRAYLOG2_WEB_PID:-${GRAYLOG2_WEB_HOME}/tmp/pids/server.pid}

start() {
    echo -n $"Starting $prog: "
    daemon --user ${GRAYLOG2_WEB_USER} /usr/bin/ruby ${GRAYLOG2_WEB_HOME}/script/server -p ${GRAYLOG2_WEB_PORT} -e ${GRAYLOG2_WEB_ENV} -d > /dev/null
    RETVAL=$?
    if [ $RETVAL = 0 ]
    then
        echo_success
    else
        echo_failure
    fi

    echo
    return $RETVAL
}
stop() {
    echo -n $"Stopping $prog: "
    if [ -f ${GRAYLOG2_WEB_PID} ]; then
        killproc -p ${GRAYLOG2_WEB_PID}
        RETVAL=$?
    else
        echo -n $"Foreman was not running.";
        failure $"Foreman was not running.";
        echo
        return 1
    fi
    echo
    return $RETVAL
}

# See how we were called.
case "$1" in
    start)
        start
    ;;
    stop)
        stop
    ;;
    status)
        echo -n "Foreman"
        status -p $GRAYLOG2_WEB_PID
        RETVAL=$?
    ;;
    restart)
        stop
        start
    ;;
    condrestart)
        if [ -f ${GRAYLOG2_WEB_HOME}/tmp/pids/server.pid ]; then
            stop
            start
            RETVAL=$?
        fi
        ;;

    *)
        echo $"Usage: $prog {start|stop|restart|condrestart}"
        exit 1
esac
