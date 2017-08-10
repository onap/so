########
# Copyright (c) 2014 GigaSpaces Technologies Ltd. All rights reserved
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#        http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
#    * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#    * See the License for the specific language governing permissions and
#    * limitations under the License.

import unittest

import mock

import neutron_plugin.port
from cloudify.mocks import (MockCloudifyContext,
                            MockNodeInstanceContext,
                            MockRelationshipSubjectContext)
from openstack_plugin_common import (NeutronClientWithSugar,
                                     OPENSTACK_ID_PROPERTY)
from cloudify.exceptions import OperationRetry


class TestPort(unittest.TestCase):

    def test_fixed_ips_no_fixed_ips(self):
        node_props = {'fixed_ip': ''}

        with mock.patch(
                'neutron_plugin.port.'
                'get_openstack_id_of_single_connected_node_by_openstack_type',
                self._get_connected_subnet_mock(return_empty=True)):
            with mock.patch(
                    'neutron_plugin.port.ctx',
                    self._get_mock_ctx_with_node_properties(node_props)):

                port = {}
                neutron_plugin.port._handle_fixed_ips(port)

        self.assertNotIn('fixed_ips', port)

    def test_fixed_ips_subnet_only(self):
        node_props = {'fixed_ip': ''}

        with mock.patch(
                'neutron_plugin.port.'
                'get_openstack_id_of_single_connected_node_by_openstack_type',
                self._get_connected_subnet_mock(return_empty=False)):
            with mock.patch(
                    'neutron_plugin.port.ctx',
                    self._get_mock_ctx_with_node_properties(node_props)):

                port = {}
                neutron_plugin.port._handle_fixed_ips(port)

        self.assertEquals([{'subnet_id': 'some-subnet-id'}],
                          port.get('fixed_ips'))

    def test_fixed_ips_ip_address_only(self):
        node_props = {'fixed_ip': '1.2.3.4'}

        with mock.patch(
                'neutron_plugin.port.'
                'get_openstack_id_of_single_connected_node_by_openstack_type',
                self._get_connected_subnet_mock(return_empty=True)):
            with mock.patch(
                    'neutron_plugin.port.ctx',
                    self._get_mock_ctx_with_node_properties(node_props)):

                port = {}
                neutron_plugin.port._handle_fixed_ips(port)

        self.assertEquals([{'ip_address': '1.2.3.4'}],
                          port.get('fixed_ips'))

    def test_fixed_ips_subnet_and_ip_address(self):
        node_props = {'fixed_ip': '1.2.3.4'}

        with mock.patch(
                'neutron_plugin.port.'
                'get_openstack_id_of_single_connected_node_by_openstack_type',
                self._get_connected_subnet_mock(return_empty=False)):
            with mock.patch(
                    'neutron_plugin.port.ctx',
                    self._get_mock_ctx_with_node_properties(node_props)):

                port = {}
                neutron_plugin.port._handle_fixed_ips(port)

        self.assertEquals([{'ip_address': '1.2.3.4',
                            'subnet_id': 'some-subnet-id'}],
                          port.get('fixed_ips'))

    @staticmethod
    def _get_connected_subnet_mock(return_empty=True):
        return lambda *args, **kw: None if return_empty else 'some-subnet-id'

    @staticmethod
    def _get_mock_ctx_with_node_properties(properties):
        return MockCloudifyContext(node_id='test_node_id',
                                   properties=properties)


class MockNeutronClient(NeutronClientWithSugar):
    """A fake neutron client with hard-coded test data."""
    def __init__(self, update):
        self.update = update
        self.body = {'port': {'id': 'test-id', 'security_groups': []}}

    def show_port(self, *_):
        return self.body

    def update_port(self, _, b, **__):
        if self.update:
            self.body.update(b)
        return

    def cosmo_get(self, *_, **__):
        return self.body['port']


class TestPortSG(unittest.TestCase):
    @mock.patch('openstack_plugin_common._put_client_in_kw')
    def test_connect_sg_to_port(self, *_):
        mock_neutron = MockNeutronClient(update=True)
        ctx = MockCloudifyContext(
            source=MockRelationshipSubjectContext(node=mock.MagicMock(),
                                                  instance=mock.MagicMock()),
            target=MockRelationshipSubjectContext(node=mock.MagicMock(),
                                                  instance=mock.MagicMock()))

        with mock.patch('neutron_plugin.port.ctx', ctx):
            neutron_plugin.port.connect_security_group(mock_neutron)
            self.assertIsNone(ctx.operation._operation_retry)

    @mock.patch('openstack_plugin_common._put_client_in_kw')
    def test_connect_sg_to_port_race_condition(self, *_):
        mock_neutron = MockNeutronClient(update=False)

        ctx = MockCloudifyContext(
            source=MockRelationshipSubjectContext(node=mock.MagicMock(),
                                                  instance=mock.MagicMock()),
            target=MockRelationshipSubjectContext(
                node=mock.MagicMock(),
                instance=MockNodeInstanceContext(
                    runtime_properties={
                        OPENSTACK_ID_PROPERTY: 'test-sg-id'})))
        with mock.patch('neutron_plugin.port.ctx', ctx):
            neutron_plugin.port.connect_security_group(mock_neutron, ctx=ctx)
            self.assertIsInstance(ctx.operation._operation_retry,
                                  OperationRetry)
