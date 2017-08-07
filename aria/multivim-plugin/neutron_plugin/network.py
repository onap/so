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
    transform_resource_name,
    with_neutron_client,
    get_resource_id,
    is_external_resource,
    is_external_resource_not_conditionally_created,
    delete_resource_and_runtime_properties,
    use_external_resource,
    validate_resource,
    OPENSTACK_ID_PROPERTY,
    OPENSTACK_TYPE_PROPERTY,
    OPENSTACK_NAME_PROPERTY,
    COMMON_RUNTIME_PROPERTIES_KEYS
)

NETWORK_OPENSTACK_TYPE = 'network'

# Runtime properties
RUNTIME_PROPERTIES_KEYS = COMMON_RUNTIME_PROPERTIES_KEYS


@operation
@with_neutron_client
def create(neutron_client, args, **kwargs):

    if use_external_resource(ctx, neutron_client, NETWORK_OPENSTACK_TYPE):
        return

    network = {
        'admin_state_up': True,
        'name': get_resource_id(ctx, NETWORK_OPENSTACK_TYPE),
    }
    network.update(ctx.node.properties['network'], **args)
    transform_resource_name(ctx, network)

    net = neutron_client.create_network({'network': network})['network']
    ctx.instance.runtime_properties[OPENSTACK_ID_PROPERTY] = net['id']
    ctx.instance.runtime_properties[OPENSTACK_TYPE_PROPERTY] =\
        NETWORK_OPENSTACK_TYPE
    ctx.instance.runtime_properties[OPENSTACK_NAME_PROPERTY] = net['name']


@operation
@with_neutron_client
def start(neutron_client, **kwargs):
    network_id = ctx.instance.runtime_properties[OPENSTACK_ID_PROPERTY]

    if is_external_resource_not_conditionally_created(ctx):
        ctx.logger.info('Validating external network is started')
        if not neutron_client.show_network(
                network_id)['network']['admin_state_up']:
            raise NonRecoverableError(
                'Expected external resource network {0} to be in '
                '"admin_state_up"=True'.format(network_id))
        return

    neutron_client.update_network(
        network_id, {
            'network': {
                'admin_state_up': True
            }
        })


@operation
@with_neutron_client
def stop(neutron_client, **kwargs):
    if is_external_resource(ctx):
        ctx.logger.info('Not stopping network since an external network is '
                        'being used')
        return

    neutron_client.update_network(
        ctx.instance.runtime_properties[OPENSTACK_ID_PROPERTY], {
            'network': {
                'admin_state_up': False
            }
        })


@operation
@with_neutron_client
def delete(neutron_client, **kwargs):
    delete_resource_and_runtime_properties(ctx, neutron_client,
                                           RUNTIME_PROPERTIES_KEYS)


@operation
@with_neutron_client
def creation_validation(neutron_client, **kwargs):
    validate_resource(ctx, neutron_client, NETWORK_OPENSTACK_TYPE)
