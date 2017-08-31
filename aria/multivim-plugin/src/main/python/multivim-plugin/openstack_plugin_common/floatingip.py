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
from openstack_plugin_common import (
    delete_resource_and_runtime_properties,
    use_external_resource,
    validate_resource,
    COMMON_RUNTIME_PROPERTIES_KEYS,
    OPENSTACK_ID_PROPERTY,
    OPENSTACK_TYPE_PROPERTY)


FLOATINGIP_OPENSTACK_TYPE = 'floatingip'

# Runtime properties
IP_ADDRESS_PROPERTY = 'floating_ip_address'  # the actual ip address
RUNTIME_PROPERTIES_KEYS = COMMON_RUNTIME_PROPERTIES_KEYS + \
    [IP_ADDRESS_PROPERTY]


def use_external_floatingip(client, ip_field_name, ext_fip_ip_extractor):
    external_fip = use_external_resource(
        ctx, client, FLOATINGIP_OPENSTACK_TYPE, ip_field_name)
    if external_fip:
        ctx.instance.runtime_properties[IP_ADDRESS_PROPERTY] = \
            ext_fip_ip_extractor(external_fip)
        return True

    return False


def set_floatingip_runtime_properties(fip_id, ip_address):
    ctx.instance.runtime_properties[OPENSTACK_ID_PROPERTY] = fip_id
    ctx.instance.runtime_properties[OPENSTACK_TYPE_PROPERTY] = \
        FLOATINGIP_OPENSTACK_TYPE
    ctx.instance.runtime_properties[IP_ADDRESS_PROPERTY] = ip_address


def delete_floatingip(client, **kwargs):
    delete_resource_and_runtime_properties(ctx, client,
                                           RUNTIME_PROPERTIES_KEYS)


def floatingip_creation_validation(client, ip_field_name, **kwargs):
    validate_resource(ctx, client, FLOATINGIP_OPENSTACK_TYPE,
                      ip_field_name)


def get_server_floating_ip(neutron_client, server_id):

    floating_ips = neutron_client.list_floatingips()

    floating_ips = floating_ips.get('floatingips')
    if not floating_ips:
        return None

    for floating_ip in floating_ips:
        port_id = floating_ip.get('port_id')
        if not port_id:
            # this floating ip is not attached to any port
            continue

        port = neutron_client.show_port(port_id)['port']
        device_id = port.get('device_id')
        if not device_id:
            # this port is not attached to any server
            continue

        if server_id == device_id:
            return floating_ip
    return None
