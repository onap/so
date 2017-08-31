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

from os import path
import tempfile

import unittest
import mock

import nova_plugin
from cloudify.test_utils import workflow_test

from openstack_plugin_common import NeutronClientWithSugar, \
    OPENSTACK_TYPE_PROPERTY, OPENSTACK_ID_PROPERTY
from neutron_plugin.network import NETWORK_OPENSTACK_TYPE
from neutron_plugin.port import PORT_OPENSTACK_TYPE
from nova_plugin.tests.test_relationships import RelationshipsTestBase
from nova_plugin.server import _prepare_server_nics
from cinder_plugin.volume import VOLUME_OPENSTACK_TYPE
from cloudify.exceptions import NonRecoverableError
from cloudify.state import current_ctx

from cloudify.utils import setup_logger

from cloudify.mocks import (
    MockNodeContext,
    MockCloudifyContext,
    MockNodeInstanceContext,
    MockRelationshipContext,
    MockRelationshipSubjectContext
)


class TestServer(unittest.TestCase):

    blueprint_path = path.join('resources',
                               'test-start-operation-retry-blueprint.yaml')

    @mock.patch('nova_plugin.server.create')
    @mock.patch('nova_plugin.server._set_network_and_ip_runtime_properties')
    @workflow_test(blueprint_path, copy_plugin_yaml=True)
    def test_nova_server_lifecycle_start(self, cfy_local, *_):

        test_vars = {
            'counter': 0,
            'server': mock.MagicMock()
        }

        def mock_get_server_by_context(*_):
            s = test_vars['server']
            if test_vars['counter'] == 0:
                s.status = nova_plugin.server.SERVER_STATUS_BUILD
            else:
                s.status = nova_plugin.server.SERVER_STATUS_ACTIVE
            test_vars['counter'] += 1
            return s

        with mock.patch('nova_plugin.server.get_server_by_context',
                        new=mock_get_server_by_context):
            cfy_local.execute('install', task_retries=3)

        self.assertEqual(2, test_vars['counter'])
        self.assertEqual(0, test_vars['server'].start.call_count)

    @workflow_test(blueprint_path, copy_plugin_yaml=True)
    @mock.patch('nova_plugin.server.create')
    @mock.patch('nova_plugin.server._set_network_and_ip_runtime_properties')
    def test_nova_server_lifecycle_start_after_stop(self, cfy_local, *_):

        test_vars = {
            'counter': 0,
            'server': mock.MagicMock()
        }

        def mock_get_server_by_context(_):
            s = test_vars['server']
            if test_vars['counter'] == 0:
                s.status = nova_plugin.server.SERVER_STATUS_SHUTOFF
            elif test_vars['counter'] == 1:
                setattr(s,
                        nova_plugin.server.OS_EXT_STS_TASK_STATE,
                        nova_plugin.server.SERVER_TASK_STATE_POWERING_ON)
            else:
                s.status = nova_plugin.server.SERVER_STATUS_ACTIVE
            test_vars['counter'] += 1
            test_vars['server'] = s
            return s

        with mock.patch('nova_plugin.server.get_server_by_context',
                        new=mock_get_server_by_context):
            cfy_local.execute('install', task_retries=3)

        self.assertEqual(1, test_vars['server'].start.call_count)
        self.assertEqual(3, test_vars['counter'])

    @workflow_test(blueprint_path, copy_plugin_yaml=True)
    @mock.patch('nova_plugin.server.create')
    @mock.patch('nova_plugin.server._set_network_and_ip_runtime_properties')
    def test_nova_server_lifecycle_start_unknown_status(self, cfy_local, *_):
        test_vars = {
            'counter': 0,
            'server': mock.MagicMock()
        }

        def mock_get_server_by_context(_):
            s = test_vars['server']
            if test_vars['counter'] == 0:
                s.status = '### unknown-status ###'
            test_vars['counter'] += 1
            test_vars['server'] = s
            return s

        with mock.patch('nova_plugin.server.get_server_by_context',
                        new=mock_get_server_by_context):
            self.assertRaisesRegexp(RuntimeError,
                                    'Unexpected server state',
                                    cfy_local.execute,
                                    'install')

        self.assertEqual(0, test_vars['server'].start.call_count)
        self.assertEqual(1, test_vars['counter'])

    @workflow_test(blueprint_path, copy_plugin_yaml=True)
    @mock.patch('nova_plugin.server.start')
    @mock.patch('nova_plugin.server._handle_image_or_flavor')
    @mock.patch('nova_plugin.server._fail_on_missing_required_parameters')
    @mock.patch('openstack_plugin_common.nova_client')
    def test_nova_server_creation_param_integrity(
            self, cfy_local, mock_nova, *args):
        cfy_local.execute('install', task_retries=0)
        calls = mock_nova.Client.return_value.servers.method_calls
        self.assertEqual(1, len(calls))
        kws = calls[0][2]
        self.assertIn('scheduler_hints', kws)
        self.assertEqual(kws['scheduler_hints'],
                         {'group': 'affinity-group-id'},
                         'expecting \'scheduler_hints\' value to exist')

    @workflow_test(blueprint_path, copy_plugin_yaml=True,
                   inputs={'use_password': True})
    @mock.patch('nova_plugin.server.create')
    @mock.patch('nova_plugin.server._set_network_and_ip_runtime_properties')
    @mock.patch(
        'nova_plugin.server.get_single_connected_node_by_openstack_type',
        autospec=True, return_value=None)
    def test_nova_server_with_use_password(self, cfy_local, *_):

        test_vars = {
            'counter': 0,
            'server': mock.MagicMock()
        }

        tmp_path = tempfile.NamedTemporaryFile(prefix='key_name')
        key_path = tmp_path.name

        def mock_get_server_by_context(_):
            s = test_vars['server']
            if test_vars['counter'] == 0:
                s.status = nova_plugin.server.SERVER_STATUS_BUILD
            else:
                s.status = nova_plugin.server.SERVER_STATUS_ACTIVE
            test_vars['counter'] += 1

            def check_agent_key_path(private_key):
                self.assertEqual(private_key, key_path)
                return private_key

            s.get_password = check_agent_key_path
            return s

        with mock.patch('nova_plugin.server.get_server_by_context',
                        mock_get_server_by_context):
            with mock.patch(
                    'cloudify.context.BootstrapContext.'
                    'CloudifyAgent.agent_key_path',
                    new_callable=mock.PropertyMock, return_value=key_path):
                cfy_local.execute('install', task_retries=5)


class TestMergeNICs(unittest.TestCase):
    def test_merge_prepends_management_network(self):
        """When the mgmt network isnt in a relationship, its the 1st nic."""
        mgmt_network_id = 'management network'
        nics = [{'net-id': 'other network'}]

        merged = nova_plugin.server._merge_nics(mgmt_network_id, nics)

        self.assertEqual(len(merged), 2)
        self.assertEqual(merged[0]['net-id'], 'management network')

    def test_management_network_in_relationships(self):
        """When the mgmt network was in a relationship, it's not prepended."""
        mgmt_network_id = 'management network'
        nics = [{'net-id': 'other network'}, {'net-id': 'management network'}]

        merged = nova_plugin.server._merge_nics(mgmt_network_id, nics)

        self.assertEqual(nics, merged)


class TestNormalizeNICs(unittest.TestCase):
    def test_normalize_port_priority(self):
        """Whe there's both net-id and port-id, port-id is used."""
        nics = [{'net-id': '1'}, {'port-id': '2'}, {'net-id': 3, 'port-id': 4}]
        normalized = nova_plugin.server._normalize_nics(nics)
        expected = [{'net-id': '1'}, {'port-id': '2'}, {'port-id': 4}]
        self.assertEqual(expected, normalized)


class MockNeutronClient(NeutronClientWithSugar):
    """A fake neutron client with hard-coded test data."""

    @mock.patch('openstack_plugin_common.OpenStackClient.__init__',
                new=mock.Mock())
    def __init__(self):
        super(MockNeutronClient, self).__init__()

    @staticmethod
    def _search_filter(objs, search_params):
        """Mock neutron's filtering by attributes in list_* methods.

        list_* methods (list_networks, list_ports)
        """
        def _matches(obj, search_params):
            return all(obj[k] == v for k, v in search_params.items())
        return [obj for obj in objs if _matches(obj, search_params)]

    def list_networks(self, **search_params):
        networks = [
            {'name': 'network1', 'id': '1'},
            {'name': 'network2', 'id': '2'},
            {'name': 'network3', 'id': '3'},
            {'name': 'network4', 'id': '4'},
            {'name': 'network5', 'id': '5'},
            {'name': 'network6', 'id': '6'},
            {'name': 'other', 'id': 'other'}
        ]
        return {'networks': self._search_filter(networks, search_params)}

    def list_ports(self, **search_params):
        ports = [
            {'name': 'port1', 'id': '1', 'network_id': '1'},
            {'name': 'port2', 'id': '2', 'network_id': '1'},
            {'name': 'port3', 'id': '3', 'network_id': '2'},
            {'name': 'port4', 'id': '4', 'network_id': '2'},
        ]
        return {'ports': self._search_filter(ports, search_params)}

    def show_port(self, port_id):
        ports = self.list_ports(id=port_id)
        return {'port': ports['ports'][0]}


class NICTestBase(RelationshipsTestBase):
    """Base test class for the NICs tests.

    It comes with helper methods to create a mock cloudify context, with
    the specified relationships.
    """
    mock_neutron = MockNeutronClient()

    def _relationship_spec(self, obj, objtype):
        return {'node': {'properties': obj},
                'instance': {
                    'runtime_properties': {OPENSTACK_TYPE_PROPERTY: objtype,
                                           OPENSTACK_ID_PROPERTY: obj['id']}}}

    def _make_vm_ctx_with_ports(self, management_network_name, ports):
        port_specs = [self._relationship_spec(obj, PORT_OPENSTACK_TYPE)
                      for obj in ports]
        vm_properties = {'management_network_name': management_network_name}
        return self._make_vm_ctx_with_relationships(port_specs,
                                                    vm_properties)

    def _make_vm_ctx_with_networks(self, management_network_name, networks):
        network_specs = [self._relationship_spec(obj, NETWORK_OPENSTACK_TYPE)
                         for obj in networks]
        vm_properties = {'management_network_name': management_network_name}
        return self._make_vm_ctx_with_relationships(network_specs,
                                                    vm_properties)


class TestServerNICs(NICTestBase):
    """Test preparing the NICs list from server<->network relationships.

    Each test creates a cloudify context that represents a openstack VM
    with relationships to networks. Then, examine the NICs list produced from
    the relationships.
    """
    def test_nova_server_creation_nics_ordering(self):
        """NIC list keeps the order of the relationships.

        The nics= list passed to nova.server.create should be ordered
        depending on the relationships to the networks (as defined in the
        blueprint).
        """
        ctx = self._make_vm_ctx_with_networks(
            management_network_name='network1',
            networks=[
                {'id': '1'},
                {'id': '2'},
                {'id': '3'},
                {'id': '4'},
                {'id': '5'},
                {'id': '6'},
            ])
        server = {'meta': {}}

        _prepare_server_nics(
            self.mock_neutron, ctx, server)

        self.assertEqual(
            ['1', '2', '3', '4', '5', '6'],
            [n['net-id'] for n in server['nics']])

    def test_server_creation_prepends_mgmt_network(self):
        """If the management network isn't in a relation, it's the first NIC.

        Creating the server examines the relationships, and if it doesn't find
        a relationship to the management network, it adds the management
        network to the NICs list, as the first element.
        """
        ctx = self._make_vm_ctx_with_networks(
            management_network_name='other',
            networks=[
                {'id': '1'},
                {'id': '2'},
                {'id': '3'},
                {'id': '4'},
                {'id': '5'},
                {'id': '6'},
            ])
        server = {'meta': {}}

        _prepare_server_nics(
            self.mock_neutron, ctx, server)

        first_nic = server['nics'][0]
        self.assertEqual('other', first_nic['net-id'])
        self.assertEqual(7, len(server['nics']))

    def test_server_creation_uses_relation_mgmt_nic(self):
        """If the management network is in a relation, it isn't prepended.

        If the server has a relationship to the management network,
        a new NIC isn't prepended to the list.
        """
        ctx = self._make_vm_ctx_with_networks(
            management_network_name='network1',
            networks=[
                {'id': '1'},
                {'id': '2'},
                {'id': '3'},
                {'id': '4'},
                {'id': '5'},
                {'id': '6'},
            ])
        server = {'meta': {}}

        _prepare_server_nics(
            self.mock_neutron, ctx, server)
        self.assertEqual(6, len(server['nics']))


class TestServerPortNICs(NICTestBase):
    """Test preparing the NICs list from server<->port relationships.

    Create a cloudify ctx representing a vm with relationships to
    openstack ports. Then examine the resulting NICs list: check that it
    contains the networks that the ports were connected to, and that each
    connection uses the port that was provided.
    """

    def test_network_with_port(self):
        """Port on the management network is used to connect to it.

        The NICs list entry for the management network contains the
        port-id of the port from the relationship, but doesn't contain net-id.
        """
        ports = [{'id': '1'}]
        ctx = self._make_vm_ctx_with_ports('network1', ports)
        server = {'meta': {}}

        _prepare_server_nics(
            self.mock_neutron, ctx, server)

        self.assertEqual([{'port-id': '1'}], server['nics'])

    def test_port_not_to_mgmt_network(self):
        """A NICs list entry is added with the network and the port.

        A relationship to a port must not only add a NIC, but the NIC must
        also make sure to use that port.
        """
        ports = [{'id': '1'}]
        ctx = self._make_vm_ctx_with_ports('other', ports)
        server = {'meta': {}}

        _prepare_server_nics(
            self.mock_neutron, ctx, server)
        expected = [
            {'net-id': 'other'},
            {'port-id': '1'}
        ]
        self.assertEqual(expected, server['nics'])


class TestBootFromVolume(unittest.TestCase):

    @mock.patch('nova_plugin.server._get_boot_volume_relationships',
                autospec=True)
    def test_handle_boot_volume(self, mock_get_rels):
        mock_get_rels.return_value.runtime_properties = {
                'external_id': 'test-id',
                'availability_zone': 'test-az',
                }
        server = {}
        ctx = mock.MagicMock()
        nova_plugin.server._handle_boot_volume(server, ctx)
        self.assertEqual({'vda': 'test-id:::0'},
                         server['block_device_mapping'])
        self.assertEqual('test-az',
                         server['availability_zone'])

    @mock.patch('nova_plugin.server._get_boot_volume_relationships',
                autospec=True, return_value=[])
    def test_handle_boot_volume_no_boot_volume(self, *_):
        server = {}
        ctx = mock.MagicMock()
        nova_plugin.server._handle_boot_volume(server, ctx)
        self.assertNotIn('block_device_mapping', server)


class TestImageFromRelationships(unittest.TestCase):

    @mock.patch('glance_plugin.image.'
                'get_openstack_ids_of_connected_nodes_by_openstack_type',
                autospec=True, return_value=['test-id'])
    def test_handle_boot_image(self, *_):
        server = {}
        ctx = mock.MagicMock()
        nova_plugin.server.handle_image_from_relationship(server, 'image', ctx)
        self.assertEqual({'image': 'test-id'}, server)

    @mock.patch('glance_plugin.image.'
                'get_openstack_ids_of_connected_nodes_by_openstack_type',
                autospec=True, return_value=[])
    def test_handle_boot_image_no_image(self, *_):
        server = {}
        ctx = mock.MagicMock()
        nova_plugin.server.handle_image_from_relationship(server, 'image', ctx)
        self.assertNotIn('image', server)


class TestServerRelationships(unittest.TestCase):

    def _get_ctx_mock(self, instance_id, boot):
        rel_specs = [MockRelationshipContext(
            target=MockRelationshipSubjectContext(node=MockNodeContext(
                properties={'boot': boot}), instance=MockNodeInstanceContext(
                runtime_properties={
                    OPENSTACK_TYPE_PROPERTY: VOLUME_OPENSTACK_TYPE,
                    OPENSTACK_ID_PROPERTY: instance_id
                })))]
        ctx = mock.MagicMock()
        ctx.instance = MockNodeInstanceContext(relationships=rel_specs)
        ctx.logger = setup_logger('mock-logger')
        return ctx

    def test_boot_volume_relationship(self):
        instance_id = 'test-id'
        ctx = self._get_ctx_mock(instance_id, True)
        result = nova_plugin.server._get_boot_volume_relationships(
            VOLUME_OPENSTACK_TYPE, ctx)
        self.assertEqual(
                instance_id,
                result.runtime_properties['external_id'])

    def test_no_boot_volume_relationship(self):
        instance_id = 'test-id'
        ctx = self._get_ctx_mock(instance_id, False)
        result = nova_plugin.server._get_boot_volume_relationships(
            VOLUME_OPENSTACK_TYPE, ctx)
        self.assertFalse(result)


class TestServerNetworkRuntimeProperties(unittest.TestCase):

    @property
    def mock_ctx(self):
        return MockCloudifyContext(
            node_id='test',
            deployment_id='test',
            properties={},
            operation={'retry_number': 0},
            provider_context={'resources': {}}
        )

    def test_server_networks_runtime_properties_empty_server(self):
        ctx = self.mock_ctx
        current_ctx.set(ctx=ctx)
        server = mock.MagicMock()
        setattr(server, 'networks', {})
        with self.assertRaisesRegexp(
                NonRecoverableError,
                'The server was created but not attached to a network.'):
            nova_plugin.server._set_network_and_ip_runtime_properties(server)

    def test_server_networks_runtime_properties_valid_networks(self):
        ctx = self.mock_ctx
        current_ctx.set(ctx=ctx)
        server = mock.MagicMock()
        network_id = 'management_network'
        network_ips = ['good', 'bad1', 'bad2']
        setattr(server,
                'networks',
                {network_id: network_ips})
        nova_plugin.server._set_network_and_ip_runtime_properties(server)
        self.assertIn('networks', ctx.instance.runtime_properties.keys())
        self.assertIn('ip', ctx.instance.runtime_properties.keys())
        self.assertEquals(ctx.instance.runtime_properties['ip'], 'good')
        self.assertEquals(ctx.instance.runtime_properties['networks'],
                          {network_id: network_ips})

    def test_server_networks_runtime_properties_empty_networks(self):
        ctx = self.mock_ctx
        current_ctx.set(ctx=ctx)
        server = mock.MagicMock()
        network_id = 'management_network'
        network_ips = []
        setattr(server,
                'networks',
                {network_id: network_ips})
        nova_plugin.server._set_network_and_ip_runtime_properties(server)
        self.assertIn('networks', ctx.instance.runtime_properties.keys())
        self.assertIn('ip', ctx.instance.runtime_properties.keys())
        self.assertEquals(ctx.instance.runtime_properties['ip'], None)
        self.assertEquals(ctx.instance.runtime_properties['networks'],
                          {network_id: network_ips})
