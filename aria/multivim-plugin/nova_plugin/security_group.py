#########
# Copyright (c) 2014 GigaSpaces Technologies Ltd. All rights reserved
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
from openstack_plugin_common import (
    transform_resource_name,
    with_nova_client,
    delete_resource_and_runtime_properties
)
from openstack_plugin_common.security_group import (
    build_sg_data,
    process_rules,
    use_external_sg,
    set_sg_runtime_properties,
    delete_sg,
    sg_creation_validation,
    RUNTIME_PROPERTIES_KEYS
)


@operation
@with_nova_client
def create(nova_client, args, **kwargs):

    security_group = build_sg_data(args)
    security_group['description'] = ctx.node.properties['description']

    sgr_default_values = {
        'ip_protocol': 'tcp',
        'from_port': 1,
        'to_port': 65535,
        'cidr': '0.0.0.0/0',
        # 'group_id': None,
        # 'parent_group_id': None,
    }
    sg_rules = process_rules(nova_client, sgr_default_values,
                             'cidr', 'group_id', 'from_port', 'to_port')

    if use_external_sg(nova_client):
        return

    transform_resource_name(ctx, security_group)

    sg = nova_client.security_groups.create(
        security_group['name'], security_group['description'])

    set_sg_runtime_properties(sg, nova_client)

    try:
        for sgr in sg_rules:
            sgr['parent_group_id'] = sg.id
            nova_client.security_group_rules.create(**sgr)
    except Exception:
        delete_resource_and_runtime_properties(ctx, nova_client,
                                               RUNTIME_PROPERTIES_KEYS)
        raise


@operation
@with_nova_client
def delete(nova_client, **kwargs):
    delete_sg(nova_client)


@operation
@with_nova_client
def creation_validation(nova_client, **kwargs):
    sg_creation_validation(nova_client, 'cidr')
