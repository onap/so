FROM docker.io/onap/integration-java11:7.0.0 

#Create User so
RUN addgroup -g 1000 so && adduser -S -u 1000 -G so so

ARG project_version=1.1.0-SNAPSHOT
ARG artifact_id
ARG docker_repository
ENV docker_repository $docker_repository
ENV project_version $project_version
ENV artifact_id $artifact_id

ENV no_proxy localhost

VOLUME /tmp 
WORKDIR /app

ADD /maven/${artifact_id}-${project_version}.jar /app/app.jar 
RUN chown -R so:so /app

USER so 


ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app/app.jar"]