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

import warnings

from cloudify import ctx
from cloudify.decorators import operation
from cloudify.exceptions import NonRecoverableError

from openstack_plugin_common import (
    provider,
    transform_resource_name,
    get_resource_id,
    with_neutron_client,
    use_external_resource,
    is_external_relationship,
    is_external_relationship_not_conditionally_created,
    delete_runtime_properties,
    get_openstack_ids_of_connected_nodes_by_openstack_type,
    delete_resource_and_runtime_properties,
    get_resource_by_name_or_id,
    validate_resource,
    COMMON_RUNTIME_PROPERTIES_KEYS,
    OPENSTACK_ID_PROPERTY,
    OPENSTACK_TYPE_PROPERTY,
    OPENSTACK_NAME_PROPERTY
)

from neutron_plugin.network import NETWORK_OPENSTACK_TYPE


ROUTER_OPENSTACK_TYPE = 'router'

# Runtime properties
RUNTIME_PROPERTIES_KEYS = COMMON_RUNTIME_PROPERTIES_KEYS


@operation
@with_neutron_client
def create(neutron_client, args, **kwargs):

    if use_external_resource(ctx, neutron_client, ROUTER_OPENSTACK_TYPE):
        try:
            ext_net_id_by_rel = _get_connected_ext_net_id(neutron_client)

            if ext_net_id_by_rel:
                router_id = \
                    ctx.instance.runtime_properties[OPENSTACK_ID_PROPERTY]

                router = neutron_client.show_router(router_id)['router']
                if not (router['external_gateway_info'] and 'network_id' in
                        router['external_gateway_info'] and
                        router['external_gateway_info']['network_id'] ==
                        ext_net_id_by_rel):
                    raise NonRecoverableError(
                        'Expected external resources router {0} and '
                        'external network {1} to be connected'.format(
                            router_id, ext_net_id_by_rel))
            return
        except Exception:
            delete_runtime_properties(ctx, RUNTIME_PROPERTIES_KEYS)
            raise

    router = {
        'name': get_resource_id(ctx, ROUTER_OPENSTACK_TYPE),
    }
    router.update(ctx.node.properties['router'], **args)
    transform_resource_name(ctx, router)

    _handle_external_network_config(router, neutron_client)

    r = neutron_client.create_router({'router': router})['router']

    ctx.instance.runtime_properties[OPENSTACK_ID_PROPERTY] = r['id']
    ctx.instance.runtime_properties[OPENSTACK_TYPE_PROPERTY] =\
        ROUTER_OPENSTACK_TYPE
    ctx.instance.runtime_properties[OPENSTACK_NAME_PROPERTY] = r['name']


@operation
@with_neutron_client
def connect_subnet(neutron_client, **kwargs):
    router_id = ctx.target.instance.runtime_properties[OPENSTACK_ID_PROPERTY]
    subnet_id = ctx.source.instance.runtime_properties[OPENSTACK_ID_PROPERTY]

    if is_external_relationship_not_conditionally_created(ctx):
        ctx.logger.info('Validating external subnet and router '
                        'are associated')
        for port in neutron_client.list_ports(device_id=router_id)['ports']:
            for fixed_ip in port.get('fixed_ips', []):
                if fixed_ip.get('subnet_id') == subnet_id:
                    return
        raise NonRecoverableError(
            'Expected external resources router {0} and subnet {1} to be '
            'connected'.format(router_id, subnet_id))

    neutron_client.add_interface_router(router_id, {'subnet_id': subnet_id})


@operation
@with_neutron_client
def disconnect_subnet(neutron_client, **kwargs):
    if is_external_relationship(ctx):
        ctx.logger.info('Not connecting subnet and router since external '
                        'subnet and router are being used')
        return

    neutron_client.remove_interface_router(
        ctx.target.instance.runtime_properties[OPENSTACK_ID_PROPERTY], {
            'subnet_id': ctx.source.instance.runtime_properties[
                OPENSTACK_ID_PROPERTY]
        }
    )


@operation
@with_neutron_client
def delete(neutron_client, **kwargs):
    delete_resource_and_runtime_properties(ctx, neutron_client,
                                           RUNTIME_PROPERTIES_KEYS)


@operation
@with_neutron_client
def creation_validation(neutron_client, **kwargs):
    validate_resource(ctx, neutron_client, ROUTER_OPENSTACK_TYPE)


def _insert_ext_net_id_to_router_config(ext_net_id, router):
    router['external_gateway_info'] = router.get(
        'external_gateway_info', {})
    router['external_gateway_info']['network_id'] = ext_net_id


def _handle_external_network_config(router, neutron_client):
    # attempting to find an external network for the router to connect to -
    # first by either a network name or id passed in explicitly; then by a
    # network connected by a relationship; with a final optional fallback to an
    # external network set in the Provider-context. Otherwise the router will
    # simply not get connected to an external network

    provider_context = provider(ctx)

    ext_net_id_by_rel = _get_connected_ext_net_id(neutron_client)
    ext_net_by_property = ctx.node.properties['external_network']

    # the following is meant for backwards compatibility with the
    # 'network_name' sugaring
    if 'external_gateway_info' in router and 'network_name' in \
            router['external_gateway_info']:
        warnings.warn(
            'Passing external "network_name" inside the '
            'external_gateway_info key of the "router" property is now '
            'deprecated; Use the "external_network" property instead',
            DeprecationWarning)

        ext_net_by_property = router['external_gateway_info']['network_name']
        del (router['external_gateway_info']['network_name'])

    # need to check if the user explicitly passed network_id in the external
    # gateway configuration as it affects external network behavior by
    # relationship and/or provider context
    if 'external_gateway_info' in router and 'network_id' in \
            router['external_gateway_info']:
        ext_net_by_property = router['external_gateway_info']['network_name']

    if ext_net_by_property and ext_net_id_by_rel:
        raise RuntimeError(
            "Router can't have an external network connected by both a "
            'relationship and by a network name/id')

    if ext_net_by_property:
        ext_net_id = get_resource_by_name_or_id(
            ext_net_by_property, NETWORK_OPENSTACK_TYPE, neutron_client)['id']
        _insert_ext_net_id_to_router_config(ext_net_id, router)
    elif ext_net_id_by_rel:
        _insert_ext_net_id_to_router_config(ext_net_id_by_rel, router)
    elif ctx.node.properties['default_to_managers_external_network'] and \
            provider_context.ext_network:
        _insert_ext_net_id_to_router_config(provider_context.ext_network['id'],
                                            router)


def _check_if_network_is_external(neutron_client, network_id):
    return neutron_client.show_network(
        network_id)['network']['router:external']


def _get_connected_ext_net_id(neutron_client):
    ext_net_ids = \
        [net_id
            for net_id in
            get_openstack_ids_of_connected_nodes_by_openstack_type(
                ctx, NETWORK_OPENSTACK_TYPE) if
            _check_if_network_is_external(neutron_client, net_id)]

    if len(ext_net_ids) > 1:
        raise NonRecoverableError(
            'More than one external network is connected to router {0}'
            ' by a relationship; External network IDs: {0}'.format(
                ext_net_ids))

    return ext_net_ids[0] if ext_net_ids else None
