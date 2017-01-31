#!/bin/bash -x

GIT_REPO=$mso_git_repository
GIT_BRANCH=$mso_git_branch
! [[ $GIT_SSH_KEY ]] && GIT_SSH_KEY=/home/jboss/user
MVN_CENTRAL_USER=$mvn_central_user
MVN_CENTRAL_PWD=$mvn_central_pwd
WILDFLY_TAR=wildfly-10.1.0.Final.tar.gz;
CHEF_DEB=chefdk_0.17.17-1_amd64.deb

echo "Jboss Home:"
echo ${JBOSS_HOME}
echo "Repository :"
echo ${GIT_REPO}
echo "Branch :"
echo ${GIT_BRANCH}
echo "Ssh key file :"
echo ${GIT_SSH_KEY}
echo "Maven central user :"
echo ${MVN_CENTRAL_USER}

[[ ${MVN_CENTRAL_PWD} ]] && echo "with password" || echo "without password"

function update_terminal() {
    echo "--------------------------------------------------------------------------"
    echo $*
    echo "--------------------------------------------------------------------------"
}

function update_ubuntu() {
    update_terminal "Updating ubuntu"
    apt-get update
    apt-get -y dist-upgrade
}


function set_ssh_key() {
    [[ -f /home/jboss/user ]] && return || update_terminal "Setting ssh key"
    mkdir -p /home/jboss/.ssh/
    mv /tmp/id_rsa /home/jboss/user
    chown jboss:jboss -R /home/jboss/user
    chmod 600 /home/jboss/user
    chown jboss:jboss /home/jboss/.ssh
    chmod 700 /home/jboss/.ssh

}

function set_maven_settings() {
    [[ -f /home/jboss/.m2/settings.xml ]] && return || update_terminal "Updating maven settings"
    mkdir -p /home/jboss/.m2/
    mv /tmp/settings.xml /home/jboss/.m2/settings.xml
    chown -R jboss:jboss /home/jboss/.m2/

    # set login and password for maven central
    sed -i "s/#PASSWORD#/$MVN_CENTRAL_PWD/g" /home/jboss/.m2/settings.xml \
	&& sed -i "s/#USERNAME#/$MVN_CENTRAL_USER/g" /home/jboss/.m2/settings.xml
}

function install_jboss() {
    [[ -f $JBOSS_HOME/bin/standalone.conf ]] && [[ $(grep "LAUNCH_JBOSS_IN_BACKGROUND=true" $JBOSS_HOME/bin/standalone.conf) ]] && return || update_terminal "Installing jboss";


    adduser --system --group jboss

    curl -C - -LO http://download.jboss.org/wildfly/10.1.0.Final/$WILDFLY_TAR ;
    tar xvfz $WILDFLY_TAR -C /opt/;
    mv /opt/${WILDFLY_TAR%.tar.gz} $JBOSS_HOME;

    chown -R jboss:jboss $JBOSS_HOME
    echo "JAVA_OPTS=\"\$JAVA_OPTS -Djboss.bind.address=0.0.0.0 -Djboss.bind.address.management=0.0.0.0 -Dmso.db=MARIADB -Dmso.config.path=/etc/mso/config.d/ -Dmso.aaf.enable=false \"" >> $JBOSS_HOME/bin/standalone.conf
    echo "LAUNCH_JBOSS_IN_BACKGROUND=true" >> $JBOSS_HOME/bin/standalone.conf

}

function create_log_folders() {
    [[ -d /var/log/ecomp ]] && [[ /var/log/ecomp/MSO/ ]] && return || update_terminal "Creating log folders"
    mkdir -p /var/log/ecomp/MSO/
    chown -R jboss:jboss /var/log/ecomp
}

function install_mariadb_connector() {
    [[ -f $JBOSS_HOME/standalone/configuration/standalone-full-ha-mso.xml ]] && return || update_terminal "Installing mariadb connector"
    MARIADB_DIR=$JBOSS_HOME/modules/mariadb
    curl -C - -O -L http://central.maven.org/maven2/org/mariadb/jdbc/mariadb-java-client/1.5.4/mariadb-java-client-1.5.4.jar
    mkdir -p $MARIADB_DIR/main
    mv mariadb-java-client-1.5.4.jar $MARIADB_DIR/main
    cp /tmp/jboss-configs//modules/mariadb/main/module.xml $MARIADB_DIR/main
    cp /tmp/jboss-configs/standalone-full-ha.xml $JBOSS_HOME/standalone/configuration/standalone-full-ha-mso.xml
    cp /tmp/jboss-configs/configuration/application-roles.properties $JBOSS_HOME/standalone/configuration/application-roles.properties
    cp /tmp/jboss-configs/configuration/application-users.properties $JBOSS_HOME/standalone/configuration/application-users.properties
    cp /tmp/jboss-configs/configuration/mgmt-groups.properties $JBOSS_HOME/standalone/configuration/mgmt-groups.properties
    cp /tmp/jboss-configs/configuration/mgmt-users.properties $JBOSS_HOME/standalone/configuration/mgmt-users.properties
    
    chown -R jboss:jboss $MARIADB_DIR
}

function dep_install() {
    update_terminal "Installing dependencies"
    # install requirements
    apt-get -y install openjdk-8-jre-headless curl git maven
    STATUS=$?
    if [[ $STATUS != 0 ]];
    then
	exit 1
    fi
}

function clone_mso() {
    [[ $("ls /tmp/mso-core") ]] && return || update_terminal "Cloning mso repository"
    # build git command
    GIT_CMD="git clone --single-branch -b ${GIT_BRANCH-master} ${GIT_REPO} -v"

    # build ssh command
    export GIT_SSH_COMMAND="ssh -i ${GIT_SSH_KEY} -o StrictHostKeyChecking=false"

    # cloning
    su - jboss -s /bin/bash -c "export GIT_SSH_COMMAND=\"ssh -i ${GIT_SSH_KEY} -o StrictHostKeyChecking=false\"; cd /tmp/; ${GIT_CMD} mso-core"
    STATUS=$?
    if [[ $STATUS != 0 ]];
    then
	exit 2
    fi
}
#export MAVEN_OPTS="$MAVEN_OPTS -Dhttp.proxyHost=one.proxy.att.com -Dhttp.proxyPort=8080 -Dhttps.proxyHost=one.proxy.att.com -Dhttps.proxyPort=8080"

function mso_build() {
    update_terminal "Building Mso core"
    # building
    cd /tmp/mso-core
    su jboss -s /bin/bash -c "mvn clean install"
    STATUS=$?
    if [[ $STATUS != 0 ]];
    then
	exit 4
    fi
}

function war_to_temp() {
    [[ $("ls /tmp/wars/") ]] && return || update_terminal "Copying wars to tmp directory"
    function copy_wars() {
	for war in `find . -iname "*.war" `;
	do
	    cp $war /tmp/wars/
	done
    }
    export -f copy_wars
    su - jboss -s /bin/bash -c copy_wars
    #tar xzf ./packages/deliveries/target/assembly/war-packs/*.tar.gz -C /tmp/wars/
}

function install_chef() {
    [[ -d /home/jboss/.chef/nodes ]] && return || update_terminal "Installing chef"
    curl -C - -LO  https://packages.chef.io/stable/ubuntu/12.04/$CHEF_DEB
    dpkg -i $CHEF_DEB
    for dir in "/etc/chef /etc/mso /var/berks-cookbooks /tmp/git /var/nodes /home/jboss/.chef/nodes";
    do
	mkdir -p $dir
	chown jboss:jboss $dir
	chmod 700 $dir
    done
}

function chef_init() {
    update_terminal "Initializing chef"
    mkdir -p /tmp/git
    cp /shared/solo.rb /tmp/git/
    chown -R jboss:jboss /tmp/git
    su - jboss -s /bin/bash -c /opt/mso/scripts/init-chef.sh
    mv /var/berks-cookbooks/${CHEF_REPO_NAME}/environments/mso-docker.json /var/berks-cookbooks/${CHEF_REPO_NAME}/environments/mso-docker-init.json
    ln -s /shared/mso-docker.json /var/berks-cookbooks/${CHEF_REPO_NAME}/environments/mso-docker.json
}

function cleanup() {
    # cleaning & space freeup
    echo "Cleaning up"

    rm -rf /tmp/git/mso-core
    rm -f /$WILDFLY_TAR;
    rm -f /$CHEF_DEB
    
    rm -rf /home/jboss/.m2
    apt-get remove -y maven git curl

}

function build() {
    update_ubuntu
    dep_install

    install_jboss
    create_log_folders
    install_mariadb_connector

    set_ssh_key

    install_chef
    chef_init


    clone_mso

    set_maven_settings
    mso_build
    war_to_temp
    cleanup
}

function init_certif() {
	# Copy the certificates
	cp /shared/*.crt /usr/local/share/ca-certificates
	update-ca-certificates
}

function start() {
    su - jboss -s /bin/bash -c /opt/mso/scripts/start-jboss-server.sh
}

rm -f "$JBOSS_HOME/standalone/deployments/README.txt"
if ! [[ "$(ls -A $JBOSS_HOME/standalone/deployments/)" ]];
then
    mkdir -p /tmp/wars/
    build
    cp /tmp/wars/* $JBOSS_HOME/standalone/deployments/
    rm -rf /tmp/wars/
    init_certif
fi

cd /opt/jboss

start
