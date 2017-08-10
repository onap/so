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
    provider,
    is_external_relationship,
    is_external_relationship_not_conditionally_created,
    OPENSTACK_ID_PROPERTY
)
from openstack_plugin_common.floatingip import (
    use_external_floatingip,
    set_floatingip_runtime_properties,
    delete_floatingip,
    floatingip_creation_validation
)


@operation
@with_neutron_client
def create(neutron_client, args, **kwargs):

    if use_external_floatingip(neutron_client, 'floating_ip_address',
                               lambda ext_fip: ext_fip['floating_ip_address']):
        return

    floatingip = {
        # No defaults
    }
    floatingip.update(ctx.node.properties['floatingip'], **args)

    # Sugar: floating_network_name -> (resolve) -> floating_network_id
    if 'floating_network_name' in floatingip:
        floatingip['floating_network_id'] = neutron_client.cosmo_get_named(
            'network', floatingip['floating_network_name'])['id']
        del floatingip['floating_network_name']
    elif 'floating_network_id' not in floatingip:
        provider_context = provider(ctx)
        ext_network = provider_context.ext_network
        if ext_network:
            floatingip['floating_network_id'] = ext_network['id']
        else:
            raise NonRecoverableError(
                'Missing floating network id, name or external network')

    fip = neutron_client.create_floatingip(
        {'floatingip': floatingip})['floatingip']
    set_floatingip_runtime_properties(fip['id'], fip['floating_ip_address'])

    ctx.logger.info('Floating IP creation response: {0}'.format(fip))


@operation
@with_neutron_client
def delete(neutron_client, **kwargs):
    delete_floatingip(neutron_client)


@operation
@with_neutron_client
def creation_validation(neutron_client, **kwargs):
    floatingip_creation_validation(neutron_client, 'floating_ip_address')


@operation
@with_neutron_client
def connect_port(neutron_client, **kwargs):
    if is_external_relationship_not_conditionally_created(ctx):
        return

    port_id = ctx.source.instance.runtime_properties[OPENSTACK_ID_PROPERTY]
    floating_ip_id = ctx.target.instance.runtime_properties[
        OPENSTACK_ID_PROPERTY]
    fip = {'port_id': port_id}
    neutron_client.update_floatingip(floating_ip_id, {'floatingip': fip})


@operation
@with_neutron_client
def disconnect_port(neutron_client, **kwargs):
    if is_external_relationship(ctx):
        ctx.logger.info('Not disassociating floatingip and port since '
                        'external floatingip and port are being used')
        return

    floating_ip_id = ctx.target.instance.runtime_properties[
        OPENSTACK_ID_PROPERTY]
    fip = {'port_id': None}
    neutron_client.update_floatingip(floating_ip_id, {'floatingip': fip})
