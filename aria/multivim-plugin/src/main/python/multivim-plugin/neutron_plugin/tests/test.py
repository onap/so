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

import mock
import random
import string
import unittest

from cloudify.exceptions import NonRecoverableError
from cloudify.context import BootstrapContext

from cloudify.mocks import MockCloudifyContext

import openstack_plugin_common as common
import openstack_plugin_common.tests.test as common_test

import neutron_plugin
import neutron_plugin.network
import neutron_plugin.port
import neutron_plugin.router
import neutron_plugin.security_group


class ResourcesRenamingTest(unittest.TestCase):
    def setUp(self):
        neutron_plugin.port._find_network_in_related_nodes = mock.Mock()
        # *** Configs from files ********************
        common.Config.get = mock.Mock()
        common.Config.get.return_value = {}
        # *** Neutron ********************
        self.neutron_mock = mock.Mock()

        def neutron_mock_connect(unused_self, unused_cfg):
            return self.neutron_mock
        common.NeutronClient.connect = neutron_mock_connect

        self.neutron_mock.cosmo_list = mock.Mock()
        self.neutron_mock.cosmo_list.return_value = []

    def _setup_ctx(self, obj_type):
        ctx = common_test.create_mock_ctx_with_provider_info(
            node_id='__cloudify_id_something_001',
            properties={
                obj_type: {
                    'name': obj_type + '_name',
                },
                'rules': []  # For security_group
            }
        )
        return ctx

    def _test(self, obj_type):
        ctx = self._setup_ctx(obj_type)
        attr = getattr(self.neutron_mock, 'create_' + obj_type)
        attr.return_value = {
            obj_type: {
                'id': obj_type + '_id',
            }
        }
        getattr(neutron_plugin, obj_type).create(ctx)
        calls = attr.mock_calls
        self.assertEquals(len(calls), 1)  # Exactly one object created
        # Indexes into call[]:
        # 0 - the only call
        # 1 - regular arguments
        # 0 - first argument
        arg = calls[0][1][0]
        self.assertEquals(arg[obj_type]['name'], 'p2_' + obj_type + '_name')

    def test_network(self):
        self._test('network')

    def test_port(self):
        self._test('port')

    def test_router(self):
        self._test('router')

    def test_security_group(self):
        self._test('security_group')

    # Network chosen arbitrary for this test.
    # Just testing something without prefix.
    def test_network_no_prefix(self):
        ctx = self._setup_ctx('network')
        for pctx in common_test.BOOTSTRAP_CONTEXTS_WITHOUT_PREFIX:
            ctx._bootstrap_context = BootstrapContext(pctx)
            self.neutron_mock.create_network.reset_mock()
            self.neutron_mock.create_network.return_value = {
                'network': {
                    'id': 'network_id',
                }
            }
            neutron_plugin.network.create(ctx)
            calls = self.neutron_mock.create_network.mock_calls
            self.assertEquals(len(calls), 1)  # Exactly one network created
            # Indexes into call[]:
            # 0 - the only call
            # 1 - regular arguments
            # 0 - first argument
            arg = calls[0][1][0]
            self.assertEquals(arg['network']['name'], 'network_name',
                              "Failed with context: " + str(pctx))


def _rand_str(n):
    chars = string.ascii_uppercase + string.digits
    return ''.join(random.choice(chars) for _ in range(n))


class SecurityGroupTest(unittest.TestCase):
    def setUp(self):
        # *** Configs from files ********************
        common.Config.get = mock.Mock()
        common.Config.get.return_value = {}
        # *** Neutron ********************
        self.neutron_mock = mock.Mock()

        def neutron_mock_connect(unused_self, unused_cfg):
            return self.neutron_mock
        common.NeutronClient.connect = neutron_mock_connect
        neutron_plugin.security_group._rules_for_sg_id = mock.Mock()
        neutron_plugin.security_group._rules_for_sg_id.return_value = []

    def _setup_ctx(self):
        sg_name = _rand_str(6) + '_new'
        ctx = MockCloudifyContext(properties={
            'security_group': {
                'name': sg_name,
                'description': 'blah'
            },
            'rules': [{'port': 80}],
            'disable_default_egress_rules': True,
        })
        return ctx

    def test_sg_new(self):
        ctx = self._setup_ctx()
        self.neutron_mock.cosmo_list = mock.Mock()
        self.neutron_mock.cosmo_list.return_value = []
        self.neutron_mock.create_security_group = mock.Mock()
        self.neutron_mock.create_security_group.return_value = {
            'security_group': {
                'description': 'blah',
                'id': ctx['security_group']['name'] + '_id',
            }
        }
        neutron_plugin.security_group.create(ctx)
        self.assertTrue(self.neutron_mock.create_security_group.mock_calls)

    def test_sg_use_existing(self):
        ctx = self._setup_ctx()
        self.neutron_mock.cosmo_list = mock.Mock()
        self.neutron_mock.cosmo_list.return_value = [{
            'id': ctx['security_group']['name'] + '_existing_id',
            'description': 'blah',
            'security_group_rules': [{
                'remote_group_id': None,
                'direction': 'ingress',
                'protocol': 'tcp',
                'ethertype': 'IPv4',
                'port_range_max': 80,
                'port_range_min': 80,
                'remote_ip_prefix': '0.0.0.0/0',
            }]
        }]
        self.neutron_mock.create_security_group = mock.Mock()
        self.neutron_mock.create_security_group.return_value = {
            'security_group': {
                'description': 'blah',
                'id': ctx['security_group']['name'] + '_id',
            }
        }
        neutron_plugin.security_group.create(ctx)
        self.assertFalse(self.neutron_mock.create_security_group.mock_calls)

    def test_sg_use_existing_with_other_rules(self):
        ctx = self._setup_ctx()
        self.neutron_mock.cosmo_list = mock.Mock()
        self.neutron_mock.cosmo_list.return_value = [{
            'id': ctx['security_group']['name'] + '_existing_id',
            'description': 'blah',
            'security_group_rules': [{
                'remote_group_id': None,
                'direction': 'ingress',
                'protocol': 'tcp',
                'ethertype': 'IPv4',
                'port_range_max': 81,  # Note the different port!
                'port_range_min': 81,  # Note the different port!
                'remote_ip_prefix': '0.0.0.0/0',
            }]
        }]
        self.neutron_mock.create_security_group = mock.Mock()
        self.neutron_mock.create_security_group.return_value = {
            'security_group': {
                'description': 'blah',
                'id': ctx['security_group']['name'] + '_id',
            }
        }
        self.assertRaises(
            NonRecoverableError,
            neutron_plugin.security_group.create,
            ctx
        )


if __name__ == '__main__':
    unittest.main()
