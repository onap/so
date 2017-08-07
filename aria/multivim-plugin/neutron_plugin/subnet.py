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
from cloudify.exceptions import NonRecoverableError
from openstack_plugin_common import (
    with_neutron_client,
    transform_resource_name,
    get_resource_id,
    get_openstack_id_of_single_connected_node_by_openstack_type,
    delete_resource_and_runtime_properties,
    delete_runtime_properties,
    use_external_resource,
    validate_resource,
    validate_ip_or_range_syntax,
    OPENSTACK_ID_PROPERTY,
    OPENSTACK_TYPE_PROPERTY,
    OPENSTACK_NAME_PROPERTY,
    COMMON_RUNTIME_PROPERTIES_KEYS
)

from neutron_plugin.network import NETWORK_OPENSTACK_TYPE

SUBNET_OPENSTACK_TYPE = 'subnet'

# Runtime properties
RUNTIME_PROPERTIES_KEYS = COMMON_RUNTIME_PROPERTIES_KEYS


@operation
@with_neutron_client
def create(neutron_client, args, **kwargs):

    if use_external_resource(ctx, neutron_client, SUBNET_OPENSTACK_TYPE):
        try:
            net_id = \
                get_openstack_id_of_single_connected_node_by_openstack_type(
                    ctx, NETWORK_OPENSTACK_TYPE, True)

            if net_id:
                subnet_id = \
                    ctx.instance.runtime_properties[OPENSTACK_ID_PROPERTY]

                if neutron_client.show_subnet(
                        subnet_id)['subnet']['network_id'] != net_id:
                    raise NonRecoverableError(
                        'Expected external resources subnet {0} and network'
                        ' {1} to be connected'.format(subnet_id, net_id))
            return
        except Exception:
            delete_runtime_properties(ctx, RUNTIME_PROPERTIES_KEYS)
            raise

    net_id = get_openstack_id_of_single_connected_node_by_openstack_type(
        ctx, NETWORK_OPENSTACK_TYPE)
    subnet = {
        'name': get_resource_id(ctx, SUBNET_OPENSTACK_TYPE),
        'network_id': net_id,
    }
    subnet.update(ctx.node.properties['subnet'], **args)
    transform_resource_name(ctx, subnet)

    s = neutron_client.create_subnet({'subnet': subnet})['subnet']
    ctx.instance.runtime_properties[OPENSTACK_ID_PROPERTY] = s['id']
    ctx.instance.runtime_properties[OPENSTACK_TYPE_PROPERTY] = \
        SUBNET_OPENSTACK_TYPE
    ctx.instance.runtime_properties[OPENSTACK_NAME_PROPERTY] = subnet['name']


@operation
@with_neutron_client
def delete(neutron_client, **kwargs):
    delete_resource_and_runtime_properties(ctx, neutron_client,
                                           RUNTIME_PROPERTIES_KEYS)


@operation
@with_neutron_client
def creation_validation(neutron_client, args, **kwargs):
    validate_resource(ctx, neutron_client, SUBNET_OPENSTACK_TYPE)
    subnet = dict(ctx.node.properties['subnet'], **args)

    if 'cidr' not in subnet:
        err = '"cidr" property must appear under the "subnet" property of a ' \
              'subnet node'
        ctx.logger.error('VALIDATION ERROR: ' + err)
        raise NonRecoverableError(err)
    validate_ip_or_range_syntax(ctx, subnet['cidr'])
