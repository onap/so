#!/bin/sh

#Copying mariadb-java-client for connectivity to mariadb
unzip /camunda/webapps/mso.war
cp /camunda/WEB-INF/lib/mariadb-java-client-2.6.2.jar /camunda/lib

if [ `id -u` = 0 ]
then
	# Install certificates found in the /app/ca-certificates volume, if any.

	needUpdate=FALSE

    for certificate in `ls -1 /camunda/app/ca-certificates`; do
        echo "Installing $certificate in /usr/local/share/ca-certificates"
        cp /camunda/app/ca-certificates/$certificate /usr/local/share/ca-certificates/$certificate
        needUpdate=TRUE
    done

	if [ $needUpdate = TRUE ]; then
		update-ca-certificates --fresh
	fi

    # Re-exec this script as the 'camunda' user.
    this=`readlink -f $0`
    exec su camunda -c  "$this"
fi

if [ -z "$APP" ]; then
	echo "CONFIG ERROR: APP environment variable not set"
	exit 1
fi

if [ ! -z "$DB_HOST" -a -z "$DB_PORT" ]; then
	export DB_PORT=3306
fi

if [ -z "${CONFIG_PATH}" ]; then
	export CONFIG_PATH=/camunda/app/config/override.yaml
fi

if [ -n "${AAF_SSL_CERTS_ENABLED}" ]; then
  read_properties "$(head -n 4 /camunda/app/certs/.passphrases)"
fi

# Running camunda.sh
./camunda.sh