#!/bin/sh

#Copying mariadb-java-client for connectivity to mariadb
if [ ${APP} = "bpmn-infra" ]; then
    unzip /camunda/webapps/mso.war
    cp /camunda/WEB-INF/lib/mariadb-java-client-2.6.2.jar /camunda/lib
fi

if [ `id -u` = 0 ]
then
	# Install certificates found in the /app/ca-certificates volume, if any.

	needUpdate=FALSE
	if [ ${APP} = "bpmn-infra" ]; then
        for certificate in `ls -1 /camunda/app/ca-certificates`; do
            echo "Installing $certificate in /usr/local/share/ca-certificates"
            cp /camunda/app/ca-certificates/$certificate /usr/local/share/ca-certificates/$certificate
            needUpdate=TRUE
        done
    else
         for certificate in `ls -1 /app/ca-certificates`; do
            echo "Installing $certificate in /usr/local/share/ca-certificates"
            cp /app/ca-certificates/$certificate /usr/local/share/ca-certificates/$certificate
            needUpdate=TRUE
         done
    fi

	if [ $needUpdate = TRUE ]; then
		update-ca-certificates --fresh
	fi

    if [ ${APP} = "bpmn-infra" ]; then
        # Re-exec this script as the 'camunda' user.
        this=`readlink -f $0`
        exec su camunda -c  "$this"
    else
        # Re-exec this script as the 'onap' user.
        this=`readlink -f $0`
        exec su so -c  "$this"
    fi
fi

touch /app/app.jar

if [ -z "$APP" ]; then
	echo "CONFIG ERROR: APP environment variable not set"
	exit 1
fi

if [ ! -z "$DB_HOST" -a -z "$DB_PORT" ]; then
	export DB_PORT=3306
fi

if [ -z "${CONFIG_PATH}" ]; then
	export CONFIG_PATH=/app/config/override.yaml
fi

if [ -z "${LOG_PATH}" ]; then
	export LOG_PATH="logs/${APP}"
fi

if [ ${APP} = "sdc-controller" ]; then
	ln -s ${LOG_PATH} ASDC
fi

if [ ${APP} = "bpmn-infra" ]; then
	ln -s ${LOG_PATH} BPMN
fi

if [ ${APP} = "so-monitoring" ]; then
	ln -s ${LOG_PATH} MONITORING
fi

if [ ${APP} = "openstack-adapter" ]; then
	export DISABLE_SNI="-Djsse.enableSNIExtension=false"
fi

if [ "${SSL_DEBUG}" = "log" ]; then
	export SSL_DEBUG="-Djavax.net.debug=all"
else
	export SSL_DEBUG=
fi

# Set java keystore and truststore options, if specified in the environment.

jksargs=

if [ ! -z "${KEYSTORE}" ]; then
	jksargs="$jksargs -Dmso.load.ssl.client.keystore=true"
	jksargs="$jksargs -Djavax.net.ssl.keyStore=$KEYSTORE"
	jksargs="$jksargs -Djavax.net.ssl.keyStorePassword=${KEYSTORE_PASSWORD}"
fi

if [ ! -z "${TRUSTSTORE}" ]; then
	jksargs="$jksargs -Djavax.net.ssl.trustStore=${TRUSTSTORE}"
	jksargs="$jksargs -Djavax.net.ssl.trustStorePassword=${TRUSTSTORE_PASSWORD}"
fi

if [ -z "${ACTIVE_PROFILE}" ]; then
	export ACTIVE_PROFILE="basic"
fi

jvmargs="${JVM_ARGS} -Dspring.profiles.active=${ACTIVE_PROFILE} -Djava.security.egd=file:/dev/./urandom -Dlogs_dir=${LOG_PATH} -Dlogging.config=/app/logback-spring.xml $jksargs -Dspring.config.additional-location=$CONFIG_PATH ${SSL_DEBUG} ${DISABLE_SNI}"


read_properties(){
    while IFS="=" read -r key value; do
        case "${key}" in
          '#'*) ;;
             *)
               eKey=$(echo $key | tr '[:lower:]' '[:upper:]')
               export "$eKey"="$value"
        esac
    done <<-EOF
	$1
	EOF
}


if [ ${APP} = "bpmn-infra" ]; then
    if [ -n "${AAF_SSL_CERTS_ENABLED}" ]; then
      read_properties "$(head -n 4 /camunda/app/certs/.passphrases)"
    fi
else
   if [ -n "${AAF_SSL_CERTS_ENABLED}" ]; then
     read_properties "$(head -n 4 /app/certs/.passphrases)"
   fi
fi

echo "JVM Arguments: ${jvmargs}"

if [ ! ${APP} = "bpmn-infra" ]; then
	java ${jvmargs} -jar app.jar
    rc=$?
    echo "Application exiting with status code $rc"

    if [ ! -z "${EXIT_DELAY}" -a "${EXIT_DELAY}" != 0 ]; then
        echo "Delaying $APP exit for $EXIT_DELAY seconds"
        sleep $EXIT_DELAY
    fi

    exit $rc
else
   ./camunda.sh
fi
