#!/bin/sh
touch /app/app.jar
if [ -z "${TARGET_ENV}" ]; then
	export TARGET_ENV="dev"
fi
export CONFIG_PATH=/app/config/override.yaml
if [ -z "${LOG_PATH}" ]; then
	export LOG_PATH="./logs/apih"
fi
if [ ${LOG_PATH} = "./logs/asdc/" ]; then
	ln -s /app/logs/asdc ASDC
fi
if [ ${LOG_PATH} = "./logs/bpmn/" ]; then
	ln -s /app/logs/bpmn BPMN
fi 
if [ "${SSL_DEBUG}" = "log" ]; then
	export SSL_DEBUG="-Djavax.net.debug=all"
else
	export SSL_DEBUG=""
fi

op=`basename  $LOG_PATH`
if [ $op = "openstack" ]; then
	export DISABLE_SNI="-Djsse.enableSNIExtension=false"
fi 

JAVA_VERSION_MAJOR=$(java -Xinternalversion | sed -n 's/.*JRE ([0-9]\.\([0-9]\+\)\.[0-9].*/\1/p')
JAVA_VERSION_MINOR=$(java -Xinternalversion | sed -n 's/.*JRE ([0-9]\.[0-9]\+\.[0-9]_\([0-9]\+\).*/\1/p')

if [[ ${JAVA_VERSION_MAJOR} -eq 8 && ${JAVA_VERSION_MINOR} -lt 131 ]]; then
	if [[ ${LOG_PATH} = "./logs/bpmn/" ]]; then
	    jvmargs="-Xmx8g "
	else
		jvmargs="-Xmx4g "
	fi 
elif [[ ${JAVA_VERSION_MAJOR} -eq 8 && ${JAVA_VERSION_MINOR} -ge 131 ]] || [[ ${JAVA_VERSION_MAJOR} -eq 9 ]]; then
	jvmargs="-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -XX:MaxRAMFraction=1 "
fi

jvmargs=${jvmargs}"-Dlogs_dir=${LOG_PATH} -Dlogging.config=/app/logback-spring.xml  -Dmso.load.ssl.client.keystore=true -Djavax.net.ssl.keyStore=msoClientKeyStore-${TARGET_ENV}.jks -Djavax.net.ssl.keyStorePassword=${MSO_KEYSTORE_PASSWORD} -Djavax.net.ssl.trustStore=msoTrustStore.jks -Djavax.net.ssl.trustStorePassword=${MSO_TRUSTSTORE_PASSWORD} -Dspring.config.location=$CONFIG_PATH ${SSL_DEBUG} ${DISABLE_SNI}"

echo JVM Arguments: ${jvmargs}

java ${jvmargs} -jar app.jar
