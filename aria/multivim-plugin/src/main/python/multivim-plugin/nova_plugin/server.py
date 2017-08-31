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


import os
import time
import copy
import operator

from novaclient import exceptions as nova_exceptions

from cloudify import ctx
from cloudify.manager import get_rest_client
from cloudify.decorators import operation
from cloudify.exceptions import NonRecoverableError, RecoverableError
from cinder_plugin import volume
from openstack_plugin_common import (
    provider,
    transform_resource_name,
    get_resource_id,
    get_openstack_ids_of_connected_nodes_by_openstack_type,
    with_nova_client,
    with_cinder_client,
    assign_payload_as_runtime_properties,
    get_openstack_id_of_single_connected_node_by_openstack_type,
    get_openstack_names_of_connected_nodes_by_openstack_type,
    get_single_connected_node_by_openstack_type,
    is_external_resource,
    is_external_resource_by_properties,
    is_external_resource_not_conditionally_created,
    is_external_relationship_not_conditionally_created,
    use_external_resource,
    delete_runtime_properties,
    is_external_relationship,
    validate_resource,
    USE_EXTERNAL_RESOURCE_PROPERTY,
    OPENSTACK_AZ_PROPERTY,
    OPENSTACK_ID_PROPERTY,
    OPENSTACK_TYPE_PROPERTY,
    OPENSTACK_NAME_PROPERTY,
    COMMON_RUNTIME_PROPERTIES_KEYS,
    with_neutron_client)
from nova_plugin.keypair import KEYPAIR_OPENSTACK_TYPE
from nova_plugin import userdata
from openstack_plugin_common.floatingip import (IP_ADDRESS_PROPERTY,
                                                get_server_floating_ip)
from neutron_plugin.network import NETWORK_OPENSTACK_TYPE
from neutron_plugin.port import PORT_OPENSTACK_TYPE
from cinder_plugin.volume import VOLUME_OPENSTACK_TYPE
from openstack_plugin_common.security_group import \
    SECURITY_GROUP_OPENSTACK_TYPE
from glance_plugin.image import handle_image_from_relationship

SERVER_OPENSTACK_TYPE = 'server'

# server status constants. Full lists here: http://docs.openstack.org/api/openstack-compute/2/content/List_Servers-d1e2078.html  # NOQA
SERVER_STATUS_ACTIVE = 'ACTIVE'
SERVER_STATUS_BUILD = 'BUILD'
SERVER_STATUS_SHUTOFF = 'SHUTOFF'

OS_EXT_STS_TASK_STATE = 'OS-EXT-STS:task_state'
SERVER_TASK_STATE_POWERING_ON = 'powering-on'

MUST_SPECIFY_NETWORK_EXCEPTION_TEXT = 'More than one possible network found.'
SERVER_DELETE_CHECK_SLEEP = 2

# Runtime properties
NETWORKS_PROPERTY = 'networks'  # all of the server's ips
IP_PROPERTY = 'ip'  # the server's private ip
ADMIN_PASSWORD_PROPERTY = 'password'  # the server's password
RUNTIME_PROPERTIES_KEYS = COMMON_RUNTIME_PROPERTIES_KEYS + \
    [NETWORKS_PROPERTY, IP_PROPERTY, ADMIN_PASSWORD_PROPERTY]


def _get_management_network_id_and_name(neutron_client, ctx):
    """Examine the context to find the management network id and name."""
    management_network_id = None
    management_network_name = None
    provider_context = provider(ctx)

    if ('management_network_name' in ctx.node.properties) and \
            ctx.node.properties['management_network_name']:
        management_network_name = \
            ctx.node.properties['management_network_name']
        management_network_name = transform_resource_name(
            ctx, management_network_name)
        management_network_id = neutron_client.cosmo_get_named(
            'network', management_network_name)
        management_network_id = management_network_id['id']
    else:
        int_network = provider_context.int_network
        if int_network:
            management_network_id = int_network['id']
            management_network_name = int_network['name']  # Already transform.

    return management_network_id, management_network_name


def _merge_nics(management_network_id, *nics_sources):
    """Merge nics_sources into a single nics list, insert mgmt network if
    needed.
    nics_sources are lists of networks received from several sources
    (server properties, relationships to networks, relationships to ports).
    Merge them into a single list, and if the management network isn't present
    there, prepend it as the first network.
    """
    merged = []
    for nics in nics_sources:
        merged.extend(nics)
    if management_network_id is not None and \
            not any(nic['net-id'] == management_network_id for nic in merged):
        merged.insert(0, {'net-id': management_network_id})
    return merged


def _normalize_nics(nics):
    """Transform the NICs passed to the form expected by openstack.

    If both net-id and port-id are provided, remove net-id: it is ignored
    by openstack anyway.
    """
    def _normalize(nic):
        if 'port-id' in nic and 'net-id' in nic:
            nic = nic.copy()
            del nic['net-id']
        return nic
    return [_normalize(nic) for nic in nics]


def _prepare_server_nics(neutron_client, ctx, server):
    """Update server['nics'] based on declared relationships.

    server['nics'] should contain the pre-declared nics, then the networks
    that the server has a declared relationship to, then the networks
    of the ports the server has a relationship to.

    If that doesn't include the management network, it should be prepended
    as the first network.

    The management network id and name are stored in the server meta properties
    """
    network_ids = get_openstack_ids_of_connected_nodes_by_openstack_type(
        ctx, NETWORK_OPENSTACK_TYPE)
    port_ids = get_openstack_ids_of_connected_nodes_by_openstack_type(
        ctx, PORT_OPENSTACK_TYPE)
    management_network_id, management_network_name = \
        _get_management_network_id_and_name(neutron_client, ctx)
    if management_network_id is None and (network_ids or port_ids):
        # Known limitation
        raise NonRecoverableError(
            "Nova server with NICs requires "
            "'management_network_name' in properties or id "
            "from provider context, which was not supplied")

    nics = _merge_nics(
        management_network_id,
        server.get('nics', []),
        [{'net-id': net_id} for net_id in network_ids],
        get_port_networks(neutron_client, port_ids))

    nics = _normalize_nics(nics)

    server['nics'] = nics
    if management_network_id is not None:
        server['meta']['cloudify_management_network_id'] = \
            management_network_id
    if management_network_name is not None:
        server['meta']['cloudify_management_network_name'] = \
            management_network_name


def _get_boot_volume_relationships(type_name, ctx):
    ctx.logger.debug('Instance relationship target instances: {0}'.format(str([
        rel.target.instance.runtime_properties
        for rel in ctx.instance.relationships])))
    targets = [
            rel.target.instance
            for rel in ctx.instance.relationships
            if rel.target.instance.runtime_properties.get(
                OPENSTACK_TYPE_PROPERTY) == type_name and
            rel.target.node.properties.get('boot', False)]

    if not targets:
        return None
    elif len(targets) > 1:
        raise NonRecoverableError("2 boot volumes not supported")
    return targets[0]


def _handle_boot_volume(server, ctx):
    boot_volume = _get_boot_volume_relationships(VOLUME_OPENSTACK_TYPE, ctx)
    if boot_volume:
        boot_volume_id = boot_volume.runtime_properties[OPENSTACK_ID_PROPERTY]
        ctx.logger.info('boot_volume_id: {0}'.format(boot_volume_id))
        az = boot_volume.runtime_properties[OPENSTACK_AZ_PROPERTY]
        # If a block device mapping already exists we shouldn't overwrite it
        # completely
        bdm = server.setdefault('block_device_mapping', {})
        bdm['vda'] = '{0}:::0'.format(boot_volume_id)
        # Some nova configurations allow cross-az server-volume connections, so
        # we can't treat that as an error.
        if not server.get('availability_zone'):
            server['availability_zone'] = az


@operation
@with_nova_client
@with_neutron_client
def create(nova_client, neutron_client, args, **kwargs):
    """
    Creates a server. Exposes the parameters mentioned in
    http://docs.openstack.org/developer/python-novaclient/api/novaclient.v1_1
    .servers.html#novaclient.v1_1.servers.ServerManager.create
    """

    external_server = use_external_resource(ctx, nova_client,
                                            SERVER_OPENSTACK_TYPE)

    if external_server:
        _set_network_and_ip_runtime_properties(external_server)
        if ctx._local:
            return
        else:
            network_ids = \
                get_openstack_ids_of_connected_nodes_by_openstack_type(
                    ctx, NETWORK_OPENSTACK_TYPE)
            port_ids = get_openstack_ids_of_connected_nodes_by_openstack_type(
                ctx, PORT_OPENSTACK_TYPE)
            try:
                _validate_external_server_nics(
                    neutron_client,
                    network_ids,
                    port_ids
                )
                _validate_external_server_keypair(nova_client)
                return
            except Exception:
                delete_runtime_properties(ctx, RUNTIME_PROPERTIES_KEYS)
                raise

    provider_context = provider(ctx)

    def rename(name):
        return transform_resource_name(ctx, name)

    server = {
        'name': get_resource_id(ctx, SERVER_OPENSTACK_TYPE),
    }
    server.update(copy.deepcopy(ctx.node.properties['server']))
    server.update(copy.deepcopy(args))

    _handle_boot_volume(server, ctx)
    handle_image_from_relationship(server, 'image', ctx)

    if 'meta' not in server:
        server['meta'] = dict()

    transform_resource_name(ctx, server)

    ctx.logger.debug(
        "server.create() server before transformations: {0}".format(server))

    for key in 'block_device_mapping', 'block_device_mapping_v2':
        if key in server:
            # if there is a connected boot volume, don't require the `image`
            # property.
            # However, python-novaclient requires an `image` input anyway, and
            # checks it for truthiness when deciding whether to pass it along
            # to the API
            if 'image' not in server:
                server['image'] = ctx.node.properties.get('image')
            break
    else:
        _handle_image_or_flavor(server, nova_client, 'image')
    _handle_image_or_flavor(server, nova_client, 'flavor')

    if provider_context.agents_security_group:
        security_groups = server.get('security_groups', [])
        asg = provider_context.agents_security_group['name']
        if asg not in security_groups:
            security_groups.append(asg)
        server['security_groups'] = security_groups
    elif not server.get('security_groups', []):
        # Make sure that if the server is connected to a security group
        # from CREATE time so that there the user can control
        # that there is never a time that a running server is not protected.
        security_group_names = \
            get_openstack_names_of_connected_nodes_by_openstack_type(
                ctx,
                SECURITY_GROUP_OPENSTACK_TYPE)
        server['security_groups'] = security_group_names

    # server keypair handling
    keypair_id = get_openstack_id_of_single_connected_node_by_openstack_type(
        ctx, KEYPAIR_OPENSTACK_TYPE, True)

    if 'key_name' in server:
        if keypair_id:
            raise NonRecoverableError("server can't both have the "
                                      '"key_name" nested property and be '
                                      'connected to a keypair via a '
                                      'relationship at the same time')
        server['key_name'] = rename(server['key_name'])
    elif keypair_id:
        server['key_name'] = _get_keypair_name_by_id(nova_client, keypair_id)
    elif provider_context.agents_keypair:
        server['key_name'] = provider_context.agents_keypair['name']
    else:
        server['key_name'] = None
        ctx.logger.info(
            'server must have a keypair, yet no keypair was connected to the '
            'server node, the "key_name" nested property '
            "wasn't used, and there is no agent keypair in the provider "
            "context. Agent installation can have issues.")

    _fail_on_missing_required_parameters(
        server,
        ('name', 'flavor'),
        'server')

    _prepare_server_nics(neutron_client, ctx, server)

    ctx.logger.debug(
        "server.create() server after transformations: {0}".format(server))

    userdata.handle_userdata(server)

    ctx.logger.info("Creating VM with parameters: {0}".format(str(server)))
    # Store the server dictionary contents in runtime properties
    assign_payload_as_runtime_properties(ctx, SERVER_OPENSTACK_TYPE, server)
    ctx.logger.debug(
        "Asking Nova to create server. All possible parameters are: {0})"
        .format(','.join(server.keys())))

    try:
        s = nova_client.servers.create(**server)
    except nova_exceptions.BadRequest as e:
        if 'Block Device Mapping is Invalid' in str(e):
            return ctx.operation.retry(
                message='Block Device Mapping is not created yet',
                retry_after=30)
        if str(e).startswith(MUST_SPECIFY_NETWORK_EXCEPTION_TEXT):
            raise NonRecoverableError(
                "Can not provision server: management_network_name or id"
                " is not specified but there are several networks that the "
                "server can be connected to.")
        raise
    ctx.instance.runtime_properties[OPENSTACK_ID_PROPERTY] = s.id
    ctx.instance.runtime_properties[OPENSTACK_TYPE_PROPERTY] = \
        SERVER_OPENSTACK_TYPE
    ctx.instance.runtime_properties[OPENSTACK_NAME_PROPERTY] = server['name']


def get_port_networks(neutron_client, port_ids):

    def get_network(port_id):
        port = neutron_client.show_port(port_id)
        return {
            'net-id': port['port']['network_id'],
            'port-id': port['port']['id']
        }

    return map(get_network, port_ids)


@operation
@with_nova_client
def start(nova_client, start_retry_interval, private_key_path, **kwargs):
    server = get_server_by_context(nova_client)

    if is_external_resource_not_conditionally_created(ctx):
        ctx.logger.info('Validating external server is started')
        if server.status != SERVER_STATUS_ACTIVE:
            raise NonRecoverableError(
                'Expected external resource server {0} to be in '
                '"{1}" status'.format(server.id, SERVER_STATUS_ACTIVE))
        return

    if server.status == SERVER_STATUS_ACTIVE:
        ctx.logger.info('Server is {0}'.format(server.status))

        if ctx.node.properties['use_password']:
            private_key = _get_private_key(private_key_path)
            ctx.logger.debug('retrieving password for server')
            password = server.get_password(private_key)

            if not password:
                return ctx.operation.retry(
                    message='Waiting for server to post generated password',
                    retry_after=start_retry_interval)

            ctx.instance.runtime_properties[ADMIN_PASSWORD_PROPERTY] = password
            ctx.logger.info('Server has been set with a password')

        _set_network_and_ip_runtime_properties(server)
        return

    server_task_state = getattr(server, OS_EXT_STS_TASK_STATE)

    if server.status == SERVER_STATUS_SHUTOFF and \
            server_task_state != SERVER_TASK_STATE_POWERING_ON:
        ctx.logger.info('Server is in {0} status - starting server...'.format(
            SERVER_STATUS_SHUTOFF))
        server.start()
        server_task_state = SERVER_TASK_STATE_POWERING_ON

    if server.status == SERVER_STATUS_BUILD or \
            server_task_state == SERVER_TASK_STATE_POWERING_ON:
        return ctx.operation.retry(
            message='Waiting for server to be in {0} state but is in {1}:{2} '
                    'state. Retrying...'.format(SERVER_STATUS_ACTIVE,
                                                server.status,
                                                server_task_state),
            retry_after=start_retry_interval)

    raise NonRecoverableError(
        'Unexpected server state {0}:{1}'.format(server.status,
                                                 server_task_state))


@operation
@with_nova_client
def stop(nova_client, **kwargs):
    """
    Stop server.

    Depends on OpenStack implementation, server.stop() might not be supported.
    """
    if is_external_resource(ctx):
        ctx.logger.info('Not stopping server since an external server is '
                        'being used')
        return

    server = get_server_by_context(nova_client)

    if server.status != SERVER_STATUS_SHUTOFF:
        nova_client.servers.stop(server)
    else:
        ctx.logger.info('Server is already stopped')


@operation
@with_nova_client
def delete(nova_client, **kwargs):
    if not is_external_resource(ctx):
        ctx.logger.info('deleting server')
        server = get_server_by_context(nova_client)
        nova_client.servers.delete(server)
        _wait_for_server_to_be_deleted(nova_client, server)
    else:
        ctx.logger.info('not deleting server since an external server is '
                        'being used')

    delete_runtime_properties(ctx, RUNTIME_PROPERTIES_KEYS)


def _wait_for_server_to_be_deleted(nova_client,
                                   server,
                                   timeout=120,
                                   sleep_interval=5):
    timeout = time.time() + timeout
    while time.time() < timeout:
        try:
            server = nova_client.servers.get(server)
            ctx.logger.debug('Waiting for server "{}" to be deleted. current'
                             ' status: {}'.format(server.id, server.status))
            time.sleep(sleep_interval)
        except nova_exceptions.NotFound:
            return
    # recoverable error
    raise RuntimeError('Server {} has not been deleted. waited for {} seconds'
                       .format(server.id, timeout))


def get_server_by_context(nova_client):
    return nova_client.servers.get(
        ctx.instance.runtime_properties[OPENSTACK_ID_PROPERTY])


def _set_network_and_ip_runtime_properties(server):

    ips = {}

    if not server.networks:
        raise NonRecoverableError(
            'The server was created but not attached to a network. '
            'Cloudify requires that a server is connected to '
            'at least one port.'
        )

    manager_network_ip = None
    management_network_name = server.metadata.get(
        'cloudify_management_network_name')

    for network, network_ips in server.networks.items():
        if (management_network_name and
                network == management_network_name) or not \
                manager_network_ip:
            manager_network_ip = next(iter(network_ips or []), None)
        ips[network] = network_ips
    ctx.instance.runtime_properties[NETWORKS_PROPERTY] = ips
    # The ip of this instance in the management network
    ctx.instance.runtime_properties[IP_PROPERTY] = manager_network_ip


@operation
@with_nova_client
def connect_floatingip(nova_client, fixed_ip, **kwargs):
    server_id = ctx.source.instance.runtime_properties[OPENSTACK_ID_PROPERTY]
    floating_ip_id = ctx.target.instance.runtime_properties[
        OPENSTACK_ID_PROPERTY]

    if is_external_relationship_not_conditionally_created(ctx):
        ctx.logger.info('Validating external floatingip and server '
                        'are associated')
        if nova_client.floating_ips.get(floating_ip_id).instance_id ==\
                server_id:
            return
        raise NonRecoverableError(
            'Expected external resources server {0} and floating-ip {1} to be '
            'connected'.format(server_id, floating_ip_id))

    floating_ip_address = ctx.target.instance.runtime_properties[
        IP_ADDRESS_PROPERTY]
    server = nova_client.servers.get(server_id)
    server.add_floating_ip(floating_ip_address, fixed_ip or None)

    server = nova_client.servers.get(server_id)
    all_server_ips = reduce(operator.add, server.networks.values())
    if floating_ip_address not in all_server_ips:
        return ctx.operation.retry(message='Failed to assign floating ip {0}'
                                           ' to machine {1}.'
                                   .format(floating_ip_address, server_id))


@operation
@with_nova_client
@with_neutron_client
def disconnect_floatingip(nova_client, neutron_client, **kwargs):
    if is_external_relationship(ctx):
        ctx.logger.info('Not disassociating floatingip and server since '
                        'external floatingip and server are being used')
        return

    server_id = ctx.source.instance.runtime_properties[OPENSTACK_ID_PROPERTY]
    ctx.logger.info("Remove floating ip {0}".format(
        ctx.target.instance.runtime_properties[IP_ADDRESS_PROPERTY]))
    server_floating_ip = get_server_floating_ip(neutron_client, server_id)
    if server_floating_ip:
        server = nova_client.servers.get(server_id)
        server.remove_floating_ip(server_floating_ip['floating_ip_address'])
        ctx.logger.info("Floating ip {0} detached from server"
                        .format(server_floating_ip['floating_ip_address']))


@operation
@with_nova_client
def connect_security_group(nova_client, **kwargs):
    server_id = ctx.source.instance.runtime_properties[OPENSTACK_ID_PROPERTY]
    security_group_id = ctx.target.instance.runtime_properties[
        OPENSTACK_ID_PROPERTY]
    security_group_name = ctx.target.instance.runtime_properties[
        OPENSTACK_NAME_PROPERTY]

    if is_external_relationship_not_conditionally_created(ctx):
        ctx.logger.info('Validating external security group and server '
                        'are associated')
        server = nova_client.servers.get(server_id)
        if [sg for sg in server.list_security_group() if sg.id ==
                security_group_id]:
            return
        raise NonRecoverableError(
            'Expected external resources server {0} and security-group {1} to '
            'be connected'.format(server_id, security_group_id))

    server = nova_client.servers.get(server_id)
    for security_group in server.list_security_group():
        # Since some security groups are already attached in
        # create this will ensure that they are not attached twice.
        if security_group_id != security_group.id and \
                security_group_name != security_group.name:
            # to support nova security groups as well,
            # we connect the security group by name
            # (as connecting by id
            # doesn't seem to work well for nova SGs)
            server.add_security_group(security_group_name)

    _validate_security_group_and_server_connection_status(nova_client,
                                                          server_id,
                                                          security_group_id,
                                                          security_group_name,
                                                          is_connected=True)


@operation
@with_nova_client
def disconnect_security_group(nova_client, **kwargs):
    if is_external_relationship(ctx):
        ctx.logger.info('Not disconnecting security group and server since '
                        'external security group and server are being used')
        return

    server_id = ctx.source.instance.runtime_properties[OPENSTACK_ID_PROPERTY]
    security_group_id = ctx.target.instance.runtime_properties[
        OPENSTACK_ID_PROPERTY]
    security_group_name = ctx.target.instance.runtime_properties[
        OPENSTACK_NAME_PROPERTY]
    server = nova_client.servers.get(server_id)
    # to support nova security groups as well, we disconnect the security group
    # by name (as disconnecting by id doesn't seem to work well for nova SGs)
    server.remove_security_group(security_group_name)

    _validate_security_group_and_server_connection_status(nova_client,
                                                          server_id,
                                                          security_group_id,
                                                          security_group_name,
                                                          is_connected=False)


@operation
@with_nova_client
@with_cinder_client
def attach_volume(nova_client, cinder_client, status_attempts,
                  status_timeout, **kwargs):
    server_id = ctx.target.instance.runtime_properties[OPENSTACK_ID_PROPERTY]
    volume_id = ctx.source.instance.runtime_properties[OPENSTACK_ID_PROPERTY]

    if is_external_relationship_not_conditionally_created(ctx):
        ctx.logger.info('Validating external volume and server '
                        'are connected')
        attachment = volume.get_attachment(cinder_client=cinder_client,
                                           volume_id=volume_id,
                                           server_id=server_id)
        if attachment:
            return
        else:
            raise NonRecoverableError(
                'Expected external resources server {0} and volume {1} to be '
                'connected'.format(server_id, volume_id))

    # Note: The 'device_name' property should actually be a property of the
    # relationship between a server and a volume; It'll move to that
    # relationship type once relationship properties are better supported.
    device = ctx.source.node.properties[volume.DEVICE_NAME_PROPERTY]
    nova_client.volumes.create_server_volume(
        server_id,
        volume_id,
        device if device != 'auto' else None)
    try:
        vol, wait_succeeded = volume.wait_until_status(
            cinder_client=cinder_client,
            volume_id=volume_id,
            status=volume.VOLUME_STATUS_IN_USE,
            num_tries=status_attempts,
            timeout=status_timeout
        )
        if not wait_succeeded:
            raise RecoverableError(
                'Waiting for volume status {0} failed - detaching volume and '
                'retrying..'.format(volume.VOLUME_STATUS_IN_USE))
        if device == 'auto':
            # The device name was assigned automatically so we
            # query the actual device name
            attachment = volume.get_attachment(
                cinder_client=cinder_client,
                volume_id=volume_id,
                server_id=server_id
            )
            device_name = attachment['device']
            ctx.logger.info('Detected device name for attachment of volume '
                            '{0} to server {1}: {2}'
                            .format(volume_id, server_id, device_name))
            ctx.source.instance.runtime_properties[
                volume.DEVICE_NAME_PROPERTY] = device_name
    except Exception, e:
        if not isinstance(e, NonRecoverableError):
            _prepare_attach_volume_to_be_repeated(
                nova_client, cinder_client, server_id, volume_id,
                status_attempts, status_timeout)
        raise


def _prepare_attach_volume_to_be_repeated(
        nova_client, cinder_client, server_id, volume_id,
        status_attempts, status_timeout):

    ctx.logger.info('Cleaning after a failed attach_volume() call')
    try:
        _detach_volume(nova_client, cinder_client, server_id, volume_id,
                       status_attempts, status_timeout)
    except Exception, e:
        ctx.logger.error('Cleaning after a failed attach_volume() call failed '
                         'raising a \'{0}\' exception.'.format(e))
        raise NonRecoverableError(e)


def _detach_volume(nova_client, cinder_client, server_id, volume_id,
                   status_attempts, status_timeout):
    attachment = volume.get_attachment(cinder_client=cinder_client,
                                       volume_id=volume_id,
                                       server_id=server_id)
    if attachment:
        nova_client.volumes.delete_server_volume(server_id, attachment['id'])
        volume.wait_until_status(cinder_client=cinder_client,
                                 volume_id=volume_id,
                                 status=volume.VOLUME_STATUS_AVAILABLE,
                                 num_tries=status_attempts,
                                 timeout=status_timeout)


@operation
@with_nova_client
@with_cinder_client
def detach_volume(nova_client, cinder_client, status_attempts,
                  status_timeout, **kwargs):
    if is_external_relationship(ctx):
        ctx.logger.info('Not detaching volume from server since '
                        'external volume and server are being used')
        return

    server_id = ctx.target.instance.runtime_properties[OPENSTACK_ID_PROPERTY]
    volume_id = ctx.source.instance.runtime_properties[OPENSTACK_ID_PROPERTY]

    _detach_volume(nova_client, cinder_client, server_id, volume_id,
                   status_attempts, status_timeout)


def _fail_on_missing_required_parameters(obj, required_parameters, hint_where):
    for k in required_parameters:
        if k not in obj:
            raise NonRecoverableError(
                "Required parameter '{0}' is missing (under host's "
                "properties.{1}). Required parameters are: {2}"
                .format(k, hint_where, required_parameters))


def _validate_external_server_keypair(nova_client):
    keypair_id = get_openstack_id_of_single_connected_node_by_openstack_type(
        ctx, KEYPAIR_OPENSTACK_TYPE, True)
    if not keypair_id:
        return

    keypair_instance_id = \
        [node_instance_id for node_instance_id, runtime_props in
         ctx.capabilities.get_all().iteritems() if
         runtime_props.get(OPENSTACK_ID_PROPERTY) == keypair_id][0]
    keypair_node_properties = _get_properties_by_node_instance_id(
        keypair_instance_id)
    if not is_external_resource_by_properties(keypair_node_properties):
        raise NonRecoverableError(
            "Can't connect a new keypair node to a server node "
            "with '{0}'=True".format(USE_EXTERNAL_RESOURCE_PROPERTY))

    server = get_server_by_context(nova_client)
    if keypair_id == _get_keypair_name_by_id(nova_client, server.key_name):
        return
    raise NonRecoverableError(
        "Expected external resources server {0} and keypair {1} to be "
        "connected".format(server.id, keypair_id))


def _get_keypair_name_by_id(nova_client, key_name):
    keypair = nova_client.cosmo_get_named(KEYPAIR_OPENSTACK_TYPE, key_name)
    return keypair.id


def _validate_external_server_nics(neutron_client, network_ids, port_ids):
    # validate no new nics are being assigned to an existing server (which
    # isn't possible on Openstack)
    new_nic_nodes = \
        [node_instance_id for node_instance_id, runtime_props in
         ctx.capabilities.get_all().iteritems() if runtime_props.get(
             OPENSTACK_TYPE_PROPERTY) in (PORT_OPENSTACK_TYPE,
                                          NETWORK_OPENSTACK_TYPE) and
         not is_external_resource_by_properties(
             _get_properties_by_node_instance_id(node_instance_id))]
    if new_nic_nodes:
        raise NonRecoverableError(
            "Can't connect new port and/or network nodes to a server node "
            "with '{0}'=True".format(USE_EXTERNAL_RESOURCE_PROPERTY))

    # validate all expected connected networks and ports are indeed already
    # connected to the server. note that additional networks (e.g. the
    # management network) may be connected as well with no error raised
    if not network_ids and not port_ids:
        return

    server_id = ctx.instance.runtime_properties[OPENSTACK_ID_PROPERTY]
    connected_ports = neutron_client.list_ports(device_id=server_id)['ports']

    # not counting networks connected by a connected port since allegedly
    # the connection should be on a separate port
    connected_ports_networks = {port['network_id'] for port in
                                connected_ports if port['id'] not in port_ids}
    connected_ports_ids = {port['id'] for port in
                           connected_ports}
    disconnected_networks = [network_id for network_id in network_ids if
                             network_id not in connected_ports_networks]
    disconnected_ports = [port_id for port_id in port_ids if port_id not
                          in connected_ports_ids]
    if disconnected_networks or disconnected_ports:
        raise NonRecoverableError(
            'Expected external resources to be connected to external server {'
            '0}: Networks - {1}; Ports - {2}'.format(server_id,
                                                     disconnected_networks,
                                                     disconnected_ports))


def _get_properties_by_node_instance_id(node_instance_id):
    client = get_rest_client()
    node_instance = client.node_instances.get(node_instance_id)
    node = client.nodes.get(ctx.deployment.id, node_instance.node_id)
    return node.properties


@operation
@with_nova_client
def creation_validation(nova_client, args, **kwargs):

    def validate_server_property_value_exists(server_props, property_name):
        ctx.logger.debug(
            'checking whether {0} exists...'.format(property_name))

        serv_props_copy = server_props.copy()
        try:
            handle_image_from_relationship(serv_props_copy, 'image', ctx)
            _handle_image_or_flavor(serv_props_copy, nova_client,
                                    property_name)
        except (NonRecoverableError, nova_exceptions.NotFound) as e:
            # temporary error - once image/flavor_name get removed, these
            # errors won't be relevant anymore
            err = str(e)
            ctx.logger.error('VALIDATION ERROR: ' + err)
            raise NonRecoverableError(err)

        prop_value_id = str(serv_props_copy[property_name])
        prop_values = list(nova_client.cosmo_list(property_name))
        for f in prop_values:
            if prop_value_id == f.id:
                ctx.logger.debug('OK: {0} exists'.format(property_name))
                return
        err = '{0} {1} does not exist'.format(property_name, prop_value_id)
        ctx.logger.error('VALIDATION ERROR: ' + err)
        if prop_values:
            ctx.logger.info('list of available {0}s:'.format(property_name))
            for f in prop_values:
                ctx.logger.info('    {0:>10} - {1}'.format(f.id, f.name))
        else:
            ctx.logger.info('there are no available {0}s'.format(
                property_name))
        raise NonRecoverableError(err)

    validate_resource(ctx, nova_client, SERVER_OPENSTACK_TYPE)

    server_props = dict(ctx.node.properties['server'], **args)
    validate_server_property_value_exists(server_props, 'flavor')


def _get_private_key(private_key_path):
    pk_node_by_rel = \
        get_single_connected_node_by_openstack_type(
            ctx, KEYPAIR_OPENSTACK_TYPE, True)

    if private_key_path:
        if pk_node_by_rel:
            raise NonRecoverableError("server can't both have a "
                                      '"private_key_path" input and be '
                                      'connected to a keypair via a '
                                      'relationship at the same time')
        key_path = private_key_path
    else:
        if pk_node_by_rel and pk_node_by_rel.properties['private_key_path']:
            key_path = pk_node_by_rel.properties['private_key_path']
        else:
            key_path = ctx.bootstrap_context.cloudify_agent.agent_key_path

    if key_path:
        key_path = os.path.expanduser(key_path)
        if os.path.isfile(key_path):
            return key_path

    err_message = 'Cannot find private key file'
    if key_path:
        err_message += '; expected file path was {0}'.format(key_path)
    raise NonRecoverableError(err_message)


def _validate_security_group_and_server_connection_status(
        nova_client, server_id, sg_id, sg_name, is_connected):

    # verifying the security group got connected or disconnected
    # successfully - this is due to Openstack concurrency issues that may
    # take place when attempting to connect/disconnect multiple SGs to the
    # same server at the same time
    server = nova_client.servers.get(server_id)

    if is_connected ^ any(sg for sg in server.list_security_group() if
                          sg.id == sg_id):
        raise RecoverableError(
            message='Security group {0} did not get {2} server {1} '
                    'properly'
            .format(
                sg_name,
                server.name,
                'connected to' if is_connected else 'disconnected from'))


def _handle_image_or_flavor(server, nova_client, prop_name):
    if prop_name not in server and '{0}_name'.format(prop_name) not in server:
        # setting image or flavor - looking it up by name; if not found, then
        # the value is assumed to be the id
        server[prop_name] = ctx.node.properties[prop_name]

        # temporary error message: once the 'image' and 'flavor' properties
        # become mandatory, this will become less relevant
        if not server[prop_name]:
            raise NonRecoverableError(
                'must set {0} by either setting a "{0}" property or by setting'
                ' a "{0}" or "{0}_name" (deprecated) field under the "server" '
                'property'.format(prop_name))

        image_or_flavor = \
            nova_client.cosmo_get_if_exists(prop_name, name=server[prop_name])
        if image_or_flavor:
            server[prop_name] = image_or_flavor.id
    else:  # Deprecated sugar
        if '{0}_name'.format(prop_name) in server:
            prop_name_plural = nova_client.cosmo_plural(prop_name)
            server[prop_name] = \
                getattr(nova_client, prop_name_plural).find(
                    name=server['{0}_name'.format(prop_name)]).id
            del server['{0}_name'.format(prop_name)]
