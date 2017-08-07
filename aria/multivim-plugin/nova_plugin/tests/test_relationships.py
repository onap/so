#########
# Copyright (c) 2016 GigaSpaces Technologies Ltd. All rights reserved
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

"""Test the functions related to retrieving relationship information.

Functions under test are mostly inside openstack_plugin_common:
get_relationships_by_openstack_type
get_connected_nodes_by_openstack_type
get_openstack_ids_of_connected_nodes_by_openstack_type
get_single_connected_node_by_openstack_type
"""

import uuid
from unittest import TestCase

from neutron_plugin.network import NETWORK_OPENSTACK_TYPE

from cloudify.exceptions import NonRecoverableError

from cloudify.mocks import (
    MockCloudifyContext,
    MockNodeContext,
    MockNodeInstanceContext,
    MockRelationshipContext,
    MockRelationshipSubjectContext,
)
from openstack_plugin_common import (
    OPENSTACK_ID_PROPERTY,
    OPENSTACK_TYPE_PROPERTY,
    get_openstack_id_of_single_connected_node_by_openstack_type,
    get_openstack_ids_of_connected_nodes_by_openstack_type,
    get_relationships_by_openstack_type,
    get_single_connected_node_by_openstack_type,
)


class RelationshipsTestBase(TestCase):
    def _make_vm_ctx_with_relationships(self, rel_specs, properties=None):
        """Prepare a mock CloudifyContext from the given relationship spec.

        rel_specs is an ordered collection of relationship specs - dicts
        with the keys "node" and "instance" used to construct the
        MockNodeContext and the MockNodeInstanceContext, and optionally a
        "type" key.
        Examples: [
            {},
            {"node": {"id": 5}},
            {
                "type": "some_type",
                "instance": {
                    "id": 3,
                    "runtime_properties":{}
                }
            }
        ]
        """
        if properties is None:
            properties = {}
        relationships = []
        for rel_spec in rel_specs:
            node = rel_spec.get('node', {})
            node_id = node.pop('id', uuid.uuid4().hex)

            instance = rel_spec.get('instance', {})
            instance_id = instance.pop('id', '{0}_{1}'.format(
                node_id, uuid.uuid4().hex))
            if 'properties' not in node:
                node['properties'] = {}
            node_ctx = MockNodeContext(id=node_id, **node)
            instance_ctx = MockNodeInstanceContext(id=instance_id, **instance)

            rel_subject_ctx = MockRelationshipSubjectContext(
                node=node_ctx, instance=instance_ctx)
            rel_type = rel_spec.get('type')
            rel_ctx = MockRelationshipContext(target=rel_subject_ctx,
                                              type=rel_type)
            relationships.append(rel_ctx)
        return MockCloudifyContext(node_id='vm', properties=properties,
                                   relationships=relationships)


class TestGettingRelatedResources(RelationshipsTestBase):

    def test_get_relationships_finds_all_by_type(self):
        """get_relationships_by_openstack_type returns all rels that match."""
        rel_specs = [{
            'instance': {
                'id': instance_id,
                'runtime_properties': {
                    OPENSTACK_TYPE_PROPERTY: NETWORK_OPENSTACK_TYPE
                }
            }
        } for instance_id in range(3)]

        rel_specs.append({
            'instance': {
                'runtime_properties': {
                    OPENSTACK_TYPE_PROPERTY: 'something else'
                }
            }
        })

        ctx = self._make_vm_ctx_with_relationships(rel_specs)
        filtered = get_relationships_by_openstack_type(ctx,
                                                       NETWORK_OPENSTACK_TYPE)
        self.assertEqual(3, len(filtered))

    def test_get_ids_of_nodes_by_type(self):

        rel_spec = {
            'instance': {
                'runtime_properties': {
                    OPENSTACK_TYPE_PROPERTY: NETWORK_OPENSTACK_TYPE,
                    OPENSTACK_ID_PROPERTY: 'the node id'
                }
            }
        }
        ctx = self._make_vm_ctx_with_relationships([rel_spec])
        ids = get_openstack_ids_of_connected_nodes_by_openstack_type(
            ctx, NETWORK_OPENSTACK_TYPE)
        self.assertEqual(['the node id'], ids)


class TestGetSingleByID(RelationshipsTestBase):
    def _make_instances(self, ids):
        """Mock a context with relationships to instances with given ids."""
        rel_specs = [{
            'node': {
                'id': node_id
            },
            'instance': {
                'runtime_properties': {
                    OPENSTACK_TYPE_PROPERTY: NETWORK_OPENSTACK_TYPE,
                    OPENSTACK_ID_PROPERTY: node_id
                }
            }
        } for node_id in ids]
        return self._make_vm_ctx_with_relationships(rel_specs)

    def test_get_single_id(self):
        ctx = self._make_instances(['the node id'])
        found_id = get_openstack_id_of_single_connected_node_by_openstack_type(
            ctx, NETWORK_OPENSTACK_TYPE)
        self.assertEqual('the node id', found_id)

    def test_get_single_id_two_found(self):
        ctx = self._make_instances([0, 1])
        self.assertRaises(
            NonRecoverableError,
            get_openstack_id_of_single_connected_node_by_openstack_type, ctx,
            NETWORK_OPENSTACK_TYPE)

    def test_get_single_id_two_found_if_exists_true(self):
        ctx = self._make_instances([0, 1])

        try:
            get_openstack_id_of_single_connected_node_by_openstack_type(
                ctx, NETWORK_OPENSTACK_TYPE, if_exists=True)
        except NonRecoverableError as e:
            self.assertIn(NETWORK_OPENSTACK_TYPE, e.message)
        else:
            self.fail()

    def test_get_single_id_if_exists_none_found(self):
        ctx = self._make_instances([])
        found = get_openstack_id_of_single_connected_node_by_openstack_type(
            ctx, NETWORK_OPENSTACK_TYPE, if_exists=True)
        self.assertIsNone(found)

    def test_get_single_id_none_found(self):
        rel_spec = []
        ctx = self._make_vm_ctx_with_relationships(rel_spec)
        self.assertRaises(
            NonRecoverableError,
            get_openstack_id_of_single_connected_node_by_openstack_type,
            ctx,
            NETWORK_OPENSTACK_TYPE)

    def test_get_single_node(self):
        ctx = self._make_instances(['the node id'])
        found_node = get_single_connected_node_by_openstack_type(
            ctx, NETWORK_OPENSTACK_TYPE)
        self.assertEqual('the node id', found_node.id)

    def test_get_single_node_two_found(self):
        ctx = self._make_instances([0, 1])
        self.assertRaises(
            NonRecoverableError,
            get_single_connected_node_by_openstack_type,
            ctx, NETWORK_OPENSTACK_TYPE)

    def test_get_single_node_two_found_if_exists(self):
        ctx = self._make_instances([0, 1])

        self.assertRaises(
            NonRecoverableError,
            get_single_connected_node_by_openstack_type,
            ctx,
            NETWORK_OPENSTACK_TYPE,
            if_exists=True)

    def test_get_single_node_if_exists_none_found(self):
        ctx = self._make_instances([])

        found = get_single_connected_node_by_openstack_type(
            ctx, NETWORK_OPENSTACK_TYPE, if_exists=True)
        self.assertIsNone(found)

    def test_get_single_node_none_found(self):
        ctx = self._make_instances([])

        self.assertRaises(
            NonRecoverableError,
            get_single_connected_node_by_openstack_type,
            ctx,
            NETWORK_OPENSTACK_TYPE)
