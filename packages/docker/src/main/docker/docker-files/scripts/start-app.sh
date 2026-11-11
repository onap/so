#!/bin/sh

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

# The ASDC/BPMN/MONITORING links below are working directories, not log
# directories: sdc-controller writes the distributed CSARs through ASDC.
mkdir -p "${LOG_PATH}"

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

jvmargs="${JVM_ARGS} -Djava.security.egd=file:/dev/./urandom -Dlogs_dir=${LOG_PATH} -Dlogging.config=${LOGGING_CONFIG:-classpath:org/onap/so/logger/logback-so.xml} -Dspring.main.banner-mode=off $jksargs -Dspring.config.additional-location=$CONFIG_PATH ${SSL_DEBUG} ${DISABLE_SNI}"

# optionally set the active spring profile
if [ -n "${ACTIVE_PROFILE}" ]; then
    jvmargs="${jvmargs} -Dspring.profiles.active=${ACTIVE_PROFILE}"
fi


echo "JVM Arguments: $(echo "${jvmargs}" | sed -E 's/(Password=)[^ ]*/\1***REDACTED***/g')"

java ${jvmargs} -jar app.jar
rc=$?

echo "Application exiting with status code $rc"

if [ ! -z "${EXIT_DELAY}" -a "${EXIT_DELAY}" != 0 ]; then
	echo "Delaying $APP exit for $EXIT_DELAY seconds"
	sleep $EXIT_DELAY
fi

exit $rc
