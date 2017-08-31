#########
# Copyright (c) 2015 GigaSpaces Technologies Ltd. All rights reserved
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
#  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  * See the License for the specific language governing permissions and
#  * limitations under the License.

from cloudify import ctx
from cloudify.decorators import operation
from cloudify.exceptions import NonRecoverableError

from openstack_plugin_common import (with_keystone_client,
                                     with_nova_client,
                                     with_cinder_client,
                                     with_neutron_client,
                                     get_resource_id,
                                     use_external_resource,
                                     delete_resource_and_runtime_properties,
                                     validate_resource,
                                     COMMON_RUNTIME_PROPERTIES_KEYS,
                                     OPENSTACK_ID_PROPERTY,
                                     OPENSTACK_TYPE_PROPERTY,
                                     OPENSTACK_NAME_PROPERTY)


PROJECT_OPENSTACK_TYPE = 'project'

TENANT_QUOTA_TYPE = 'quota'

RUNTIME_PROPERTIES_KEYS = COMMON_RUNTIME_PROPERTIES_KEYS


@operation
@with_keystone_client
def create(keystone_client, **kwargs):
    if use_external_resource(ctx, keystone_client, PROJECT_OPENSTACK_TYPE):
        return

    project_dict = {
        'name': get_resource_id(ctx, PROJECT_OPENSTACK_TYPE),
        'domain': 'default'
    }

    project_dict.update(ctx.node.properties['project'])
    project = keystone_client.projects.create(**project_dict)

    ctx.instance.runtime_properties[OPENSTACK_ID_PROPERTY] = project.id
    ctx.instance.runtime_properties[OPENSTACK_TYPE_PROPERTY] = \
        PROJECT_OPENSTACK_TYPE
    ctx.instance.runtime_properties[OPENSTACK_NAME_PROPERTY] = project.name


@operation
@with_keystone_client
@with_nova_client
@with_cinder_client
@with_neutron_client
def start(keystone_client, nova_client, cinder_client, neutron_client,
          **kwargs):
    project_id = ctx.instance.runtime_properties[OPENSTACK_ID_PROPERTY]
    users = ctx.node.properties['users']
    validate_users(users, keystone_client)

    assign_users(project_id, users, keystone_client)

    quota = ctx.node.properties[TENANT_QUOTA_TYPE]
    update_quota(project_id, quota, nova_client, 'nova')
    update_quota(project_id, quota, neutron_client, 'neutron')
    update_quota(project_id, quota, cinder_client, 'cinder')


@operation
@with_keystone_client
@with_nova_client
@with_cinder_client
@with_neutron_client
def delete(keystone_client, nova_client, cinder_client,
           neutron_client, **kwargs):
    tenant_id = ctx.instance.runtime_properties[OPENSTACK_ID_PROPERTY]
    quota = ctx.node.properties[TENANT_QUOTA_TYPE]
    delete_quota(tenant_id, quota, nova_client, 'nova')
    delete_quota(tenant_id, quota, neutron_client, 'neutron')
    delete_quota(tenant_id, quota, cinder_client, 'cinder')
    delete_resource_and_runtime_properties(ctx, keystone_client,
                                           RUNTIME_PROPERTIES_KEYS)


@operation
@with_keystone_client
def creation_validation(keystone_client, **kwargs):
    validate_resource(ctx, keystone_client, PROJECT_OPENSTACK_TYPE)


def assign_users(project_id, users, keystone_client):
    for user in users:
        roles = user['roles']
        u = keystone_client.users.find(name=user['name'])
        for role in roles:
            r = keystone_client.roles.find(name=role)
            keystone_client.roles.grant(user=u.id,
                                        project=project_id,
                                        role=r.id)


def validate_users(users, keystone_client):
    user_names = [user['name'] for user in users]
    if len(user_names) > len(set(user_names)):
        raise NonRecoverableError('Users are not unique')

    for user_name in user_names:
        keystone_client.users.find(name=user_name)

    for user in users:
        if len(user['roles']) > len(set(user['roles'])):
            msg = 'Roles for user {} are not unique'
            raise NonRecoverableError(msg.format(user['name']))

    role_names = {role for user in users for role in user['roles']}
    for role_name in role_names:
        keystone_client.roles.find(name=role_name)


def update_quota(tenant_id, quota, client, what_quota):
    updated_quota = quota.get(what_quota)
    if updated_quota:
        if what_quota == 'neutron':
            new_quota = client.update_quota(tenant_id=tenant_id,
                                            body={'quota': updated_quota})
        else:
            new_quota = client.quotas.update(tenant_id=tenant_id,
                                             **updated_quota)
        ctx.logger.info(
            'Updated {0} quota: {1}'.format(what_quota, str(new_quota)))


def delete_quota(project_id, quota, client, what_quota):
    deleting_quota = quota.get(what_quota)
    if deleting_quota:
        if what_quota == 'neutron':
            client.delete_quota(tenant_id=project_id)
        else:
            client.quotas.delete(tenant_id=project_id)
