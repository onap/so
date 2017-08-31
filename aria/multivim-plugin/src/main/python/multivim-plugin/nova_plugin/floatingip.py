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
from openstack_plugin_common import with_nova_client
from openstack_plugin_common.floatingip import (
    use_external_floatingip,
    set_floatingip_runtime_properties,
    delete_floatingip,
    floatingip_creation_validation
)


# random note regarding nova floating-ips: floating ips on nova-net have
# pre-assigned ids, and thus a call "nova.floating_ips.get(<fip_id>)" will
# return a value even if the floating-ip isn't even allocated.
# currently all lookups in the code, including by id, use search (i.e.
# nova.<type>.findall) and lists, which won't return such unallocated
# resources.

@operation
@with_nova_client
def create(nova_client, args, **kwargs):

    if use_external_floatingip(nova_client, 'ip',
                               lambda ext_fip: ext_fip.ip):
        return

    floatingip = {
        'pool': None
    }
    floatingip.update(ctx.node.properties['floatingip'], **args)

    fip = nova_client.floating_ips.create(floatingip['pool'])
    set_floatingip_runtime_properties(fip.id, fip.ip)


@operation
@with_nova_client
def delete(nova_client, **kwargs):
    delete_floatingip(nova_client)


@operation
@with_nova_client
def creation_validation(nova_client, **kwargs):
    floatingip_creation_validation(nova_client, 'ip')
