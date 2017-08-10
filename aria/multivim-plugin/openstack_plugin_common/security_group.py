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

import copy
import re

from cloudify import ctx
from cloudify.exceptions import NonRecoverableError

from openstack_plugin_common import (
    get_resource_id,
    use_external_resource,
    delete_resource_and_runtime_properties,
    validate_resource,
    validate_ip_or_range_syntax,
    OPENSTACK_ID_PROPERTY,
    OPENSTACK_TYPE_PROPERTY,
    OPENSTACK_NAME_PROPERTY,
    COMMON_RUNTIME_PROPERTIES_KEYS
)

SECURITY_GROUP_OPENSTACK_TYPE = 'security_group'

# Runtime properties
RUNTIME_PROPERTIES_KEYS = COMMON_RUNTIME_PROPERTIES_KEYS

NODE_NAME_RE = re.compile('^(.*)_.*$')  # Anything before last underscore


def build_sg_data(args=None):
    security_group = {
        'description': None,
        'name': get_resource_id(ctx, SECURITY_GROUP_OPENSTACK_TYPE),
    }

    args = args or {}
    security_group.update(ctx.node.properties['security_group'], **args)

    return security_group


def process_rules(client, sgr_default_values, cidr_field_name,
                  remote_group_field_name, min_port_field_name,
                  max_port_field_name):
    rules_to_apply = ctx.node.properties['rules']
    security_group_rules = []
    for rule in rules_to_apply:
        security_group_rules.append(
            _process_rule(rule, client, sgr_default_values, cidr_field_name,
                          remote_group_field_name, min_port_field_name,
                          max_port_field_name))

    return security_group_rules


def use_external_sg(client):
    return use_external_resource(ctx, client,
                                 SECURITY_GROUP_OPENSTACK_TYPE)


def set_sg_runtime_properties(sg, client):
    ctx.instance.runtime_properties[OPENSTACK_ID_PROPERTY] =\
        client.get_id_from_resource(sg)
    ctx.instance.runtime_properties[OPENSTACK_TYPE_PROPERTY] =\
        SECURITY_GROUP_OPENSTACK_TYPE
    ctx.instance.runtime_properties[OPENSTACK_NAME_PROPERTY] = \
        client.get_name_from_resource(sg)


def delete_sg(client, **kwargs):
    delete_resource_and_runtime_properties(ctx, client,
                                           RUNTIME_PROPERTIES_KEYS)


def sg_creation_validation(client, cidr_field_name, **kwargs):
    validate_resource(ctx, client, SECURITY_GROUP_OPENSTACK_TYPE)

    ctx.logger.debug('validating CIDR for rules with a {0} field'.format(
        cidr_field_name))
    for rule in ctx.node.properties['rules']:
        if cidr_field_name in rule:
            validate_ip_or_range_syntax(ctx, rule[cidr_field_name])


def _process_rule(rule, client, sgr_default_values, cidr_field_name,
                  remote_group_field_name, min_port_field_name,
                  max_port_field_name):
    ctx.logger.debug(
        "Security group rule before transformations: {0}".format(rule))

    sgr = copy.deepcopy(sgr_default_values)
    if 'port' in rule:
        rule[min_port_field_name] = rule['port']
        rule[max_port_field_name] = rule['port']
        del rule['port']
    sgr.update(rule)

    if (remote_group_field_name in sgr) and sgr[remote_group_field_name]:
        sgr[cidr_field_name] = None
    elif ('remote_group_node' in sgr) and sgr['remote_group_node']:
        _, remote_group_node = _capabilities_of_node_named(
            sgr['remote_group_node'])
        sgr[remote_group_field_name] = remote_group_node[OPENSTACK_ID_PROPERTY]
        del sgr['remote_group_node']
        sgr[cidr_field_name] = None
    elif ('remote_group_name' in sgr) and sgr['remote_group_name']:
        sgr[remote_group_field_name] = \
            client.get_id_from_resource(
                client.cosmo_get_named(
                    SECURITY_GROUP_OPENSTACK_TYPE, sgr['remote_group_name']))
        del sgr['remote_group_name']
        sgr[cidr_field_name] = None

    ctx.logger.debug(
        "Security group rule after transformations: {0}".format(sgr))
    return sgr


def _capabilities_of_node_named(node_name):
    result = None
    caps = ctx.capabilities.get_all()
    for node_id in caps:
        match = NODE_NAME_RE.match(node_id)
        if match:
            candidate_node_name = match.group(1)
            if candidate_node_name == node_name:
                if result:
                    raise NonRecoverableError(
                        "More than one node named '{0}' "
                        "in capabilities".format(node_name))
                result = (node_id, caps[node_id])
    if not result:
        raise NonRecoverableError(
            "Could not find node named '{0}' "
            "in capabilities".format(node_name))
    return result
