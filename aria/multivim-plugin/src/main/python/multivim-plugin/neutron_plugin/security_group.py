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

from time import sleep

from requests.exceptions import RequestException

from cloudify import ctx
from cloudify.decorators import operation
from cloudify.exceptions import NonRecoverableError
from openstack_plugin_common import (
    transform_resource_name,
    with_neutron_client,
    delete_resource_and_runtime_properties,
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

DEFAULT_RULE_VALUES = {
    'direction': 'ingress',
    'ethertype': 'IPv4',
    'port_range_min': 1,
    'port_range_max': 65535,
    'protocol': 'tcp',
    'remote_group_id': None,
    'remote_ip_prefix': '0.0.0.0/0',
}


@operation
@with_neutron_client
def create(
    neutron_client, args,
    status_attempts=10, status_timeout=2, **kwargs
):

    security_group = build_sg_data(args)
    if not security_group['description']:
        security_group['description'] = ctx.node.properties['description']

    sg_rules = process_rules(neutron_client, DEFAULT_RULE_VALUES,
                             'remote_ip_prefix', 'remote_group_id',
                             'port_range_min', 'port_range_max')

    disable_default_egress_rules = ctx.node.properties.get(
        'disable_default_egress_rules')

    if use_external_sg(neutron_client):
        return

    transform_resource_name(ctx, security_group)

    sg = neutron_client.create_security_group(
        {'security_group': security_group})['security_group']

    for attempt in range(max(status_attempts, 1)):
        sleep(status_timeout)
        try:
            neutron_client.show_security_group(sg['id'])
        except RequestException as e:
            ctx.logger.debug("Waiting for SG to be visible. Attempt {}".format(
                attempt))
        else:
            break
    else:
        raise NonRecoverableError(
            "Timed out waiting for security_group to exist", e)

    set_sg_runtime_properties(sg, neutron_client)

    try:
        if disable_default_egress_rules:
            for er in _egress_rules(_rules_for_sg_id(neutron_client,
                                                     sg['id'])):
                neutron_client.delete_security_group_rule(er['id'])

        for sgr in sg_rules:
            sgr['security_group_id'] = sg['id']
            neutron_client.create_security_group_rule(
                {'security_group_rule': sgr})
    except Exception:
        try:
            delete_resource_and_runtime_properties(
                ctx, neutron_client,
                RUNTIME_PROPERTIES_KEYS)
        except Exception as e:
            raise NonRecoverableError(
                'Exception while tearing down for retry', e)
        raise


@operation
@with_neutron_client
def delete(neutron_client, **kwargs):
    delete_sg(neutron_client)


@operation
@with_neutron_client
def creation_validation(neutron_client, **kwargs):
    sg_creation_validation(neutron_client, 'remote_ip_prefix')


def _egress_rules(rules):
    return [rule for rule in rules if rule.get('direction') == 'egress']


def _rules_for_sg_id(neutron_client, id):
    rules = neutron_client.list_security_group_rules()['security_group_rules']
    rules = [rule for rule in rules if rule['security_group_id'] == id]
    return rules
