#!/bin/sh

if [ -z "${APP_HOME}" ]; then
	export APP_HOME="/home/mso"
fi

FURL="jdbc:mariadb://${FLYWAY_URL}:${FLYWAY_PORT}/"

# since this script is called once for each of these three components (catalogdb, requestdb, bpmn
# we can safely remove the sql files that are not pertaining to the component being invoked 

if [[ "${FLYWAY_SCHEMAS}" = 'catalogdb' ]]; then
    echo "Running catalogdb migration"
    
    rm -rf ${APP_HOME}/*__camundabpmn.sql
    rm -rf ${APP_HOME}/*__mso_requests.sql
    rm -rf ${APP_HOME}/V2.4__VLANTagging1806.sql    
	${APP_HOME}/flyway -user=${FLYWAY_USERNAME} -password=${FLYWAY_PASSWORD} -schemas=catalogdb -locations=filesystem:${APP_HOME}/flyway-5.0.7/sql -url=${FURL} migrate	
fi

if [[ "${FLYWAY_SCHEMAS}" = 'requestdb' ]]; then
    echo "Running requestdb migration"

    cp -rf ${APP_HOME}/V1__mso_requests.sql ${APP_HOME}/V1__mso_requests.bak
	cp -rf ${APP_HOME}/V2.4__VLANTagging1806.sql ${APP_HOME}/V2.4__VLANTagging1806.bak

    rm -rf ${APP_HOME}/*.sql

	mv ${APP_HOME}/V1__mso_requests.bak ${APP_HOME}/V1__mso_requests.sql
	mv ${APP_HOME}/V2.4__VLANTagging1806.bak ${APP_HOME}/V2.4__VLANTagging1806.sql

	${APP_HOME}/flyway -user=${FLYWAY_USERNAME} -password=${FLYWAY_PASSWORD} -schemas=requestdb -locations=filesystem:${APP_HOME}/flyway-5.0.7/sql -url=${FURL} migrate	
fi

if [[ "${FLYWAY_SCHEMAS}" = 'camundabpmn' ]]; then
    echo "Running camundabpmn migration"
    
    cp -rf ${APP_HOME}/V1__camundabpmn.sql ${APP_HOME}/V1__camundabpmn.bak
    rm -f ${APP_HOME}/*.sql
	mv ${APP_HOME}/V1__camundabpmn.bak ${APP_HOME}/V1__camundabpmn.sql

	${APP_HOME}/flyway -user=${FLYWAY_USERNAME} -password=${FLYWAY_PASSWORD} -schemas=camundabpmn -locations=filesystem:${APP_HOME}/flyway-5.0.7/sql -url=${FURL} migrate	
fi