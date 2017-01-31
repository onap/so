#!/bin/sh
# Copyright 2015 AT&T Intellectual Properties
##############################################################################
#       Script to initialize the chef-repo branch and.chef
#
##############################################################################

set -x
cd /tmp/git/
export GIT_SSH_COMMAND="ssh -o StrictHostKeyChecking=false -i ~/user"
git clone -b ${BRANCH_NAME:-master} --single-branch ssh://${REPO_USERNAME}@${REPO_ADDRESS}/${CHEF_REPO_NAME}.git


# Will have to be removed later
#mkdir -p /var/chef/nodes
sed "s/CHEF_REPO_NAME_TO_REPLACE/${CHEF_REPO_NAME}/g" -i /tmp/git/solo.rb
mv /tmp/git/solo.rb /tmp/git/${CHEF_REPO_NAME}/
cd /tmp/git/${CHEF_REPO_NAME}

echo "Vendor cookbooks with Berkshelf"
berks vendor /var/berks-cookbooks -b Berksfile.mso-docker

# Execute the ChefClient to configure the mso-config
echo "Update config with chef solo"
chef-solo -c /var/berks-cookbooks/${CHEF_REPO_NAME}/solo.rb -o recipe[mso-config::apih],recipe[mso-config::bpmn],recipe[mso-config::jra]