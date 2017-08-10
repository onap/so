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


import unittest

import mock
from novaclient import exceptions as nova_exceptions

import nova_plugin.server as server
from cloudify.exceptions import NonRecoverableError
from cloudify.mocks import MockCloudifyContext


class TestServerImageAndFlavor(unittest.TestCase):

    def test_no_image_and_no_flavor(self):
        node_props = {
            'image': '',
            'flavor': ''
        }
        with mock.patch('nova_plugin.server.ctx',
                        self._get_mock_ctx_with_node_properties(node_props)):
            nova_client = self._get_mocked_nova_client()

            serv = {}
            self.assertRaises(NonRecoverableError,
                              server._handle_image_or_flavor,
                              serv, nova_client, 'image')
            self.assertRaises(NonRecoverableError,
                              server._handle_image_or_flavor,
                              serv, nova_client, 'flavor')

    def test_image_and_flavor_properties_as_names(self):
        node_props = {
            'image': 'some-image-name',
            'flavor': 'some-flavor-name'
        }
        with mock.patch('nova_plugin.server.ctx',
                        self._get_mock_ctx_with_node_properties(node_props)):
            nova_client = self._get_mocked_nova_client()

            serv = {}
            server._handle_image_or_flavor(serv, nova_client, 'image')
            server._handle_image_or_flavor(serv, nova_client, 'flavor')

        self.assertEquals('some-image-id', serv.get('image'))
        self.assertEquals('some-flavor-id', serv.get('flavor'))

    def test_image_and_flavor_properties_as_ids(self):
        node_props = {
            'image': 'some-image-id',
            'flavor': 'some-flavor-id'
        }
        with mock.patch('nova_plugin.server.ctx',
                        self._get_mock_ctx_with_node_properties(node_props)):
            nova_client = self._get_mocked_nova_client()

            serv = {}
            server._handle_image_or_flavor(serv, nova_client, 'image')
            server._handle_image_or_flavor(serv, nova_client, 'flavor')

        self.assertEquals('some-image-id', serv.get('image'))
        self.assertEquals('some-flavor-id', serv.get('flavor'))

    def test_image_id_and_flavor_id(self):
        node_props = {
            'image': '',
            'flavor': ''
        }
        with mock.patch('nova_plugin.server.ctx',
                        self._get_mock_ctx_with_node_properties(node_props)):
            nova_client = self._get_mocked_nova_client()

            serv = {}
            serv['image'] = 'some-image-id'
            serv['flavor'] = 'some-flavor-id'
            server._handle_image_or_flavor(serv, nova_client, 'image')
            server._handle_image_or_flavor(serv, nova_client, 'flavor')

        self.assertEquals('some-image-id', serv.get('image'))
        self.assertEquals('some-flavor-id', serv.get('flavor'))

    def test_image_name_and_flavor_name(self):
        node_props = {
            'image': '',
            'flavor': ''
        }
        with mock.patch('nova_plugin.server.ctx',
                        self._get_mock_ctx_with_node_properties(node_props)):
            nova_client = self._get_mocked_nova_client()

            serv = {}
            serv['image_name'] = 'some-image-name'
            serv['flavor_name'] = 'some-flavor-name'
            server._handle_image_or_flavor(serv, nova_client, 'image')
            server._handle_image_or_flavor(serv, nova_client, 'flavor')

        self.assertEquals('some-image-id', serv.get('image'))
        self.assertNotIn('image_name', serv)
        self.assertEquals('some-flavor-id', serv.get('flavor'))
        self.assertNotIn('flavor_name', serv)

    def test_unknown_image_name_and_flavor_name(self):
        node_props = {
            'image': '',
            'flavor': ''
        }
        with mock.patch('nova_plugin.server.ctx',
                        self._get_mock_ctx_with_node_properties(node_props)):
            nova_client = self._get_mocked_nova_client()

            serv = {}
            serv['image_name'] = 'some-unknown-image-name'
            serv['flavor_name'] = 'some-unknown-flavor-name'

            self.assertRaises(nova_exceptions.NotFound,
                              server._handle_image_or_flavor,
                              serv, nova_client, 'image')
            self.assertRaises(nova_exceptions.NotFound,
                              server._handle_image_or_flavor,
                              serv, nova_client, 'flavor')

    def test_image_id_and_flavor_id_override_on_properties(self):
        node_props = {
            'image': 'properties-image-id',
            'flavor': 'properties-flavor-id'
        }
        with mock.patch('nova_plugin.server.ctx',
                        self._get_mock_ctx_with_node_properties(node_props)):
            nova_client = self._get_mocked_nova_client()

            serv = {}
            serv['image'] = 'some-image-id'
            serv['flavor'] = 'some-flavor-id'
            server._handle_image_or_flavor(serv, nova_client, 'image')
            server._handle_image_or_flavor(serv, nova_client, 'flavor')

        self.assertEquals('some-image-id', serv.get('image'))
        self.assertEquals('some-flavor-id', serv.get('flavor'))

    def test_image_name_and_flavor_name_override_on_properties(self):
        node_props = {
            'image': 'properties-image-id',
            'flavor': 'properties-flavor-id'
        }
        with mock.patch('nova_plugin.server.ctx',
                        self._get_mock_ctx_with_node_properties(node_props)):
            nova_client = self._get_mocked_nova_client()

            serv = {}
            serv['image_name'] = 'some-image-name'
            serv['flavor_name'] = 'some-flavor-name'
            server._handle_image_or_flavor(serv, nova_client, 'image')
            server._handle_image_or_flavor(serv, nova_client, 'flavor')

        self.assertEquals('some-image-id', serv.get('image'))
        self.assertNotIn('image_name', serv)
        self.assertEquals('some-flavor-id', serv.get('flavor'))
        self.assertNotIn('flavor_name', serv)

    def test_image_name_and_flavor_name_override_on_image_and_flavor_ids(self):
        node_props = {
            'image': '',
            'flavor': ''
        }
        with mock.patch('nova_plugin.server.ctx',
                        self._get_mock_ctx_with_node_properties(node_props)):
            nova_client = self._get_mocked_nova_client()

            serv = {}
            serv['image'] = 'some-bad-image-id'
            serv['image_name'] = 'some-image-name'
            serv['flavor'] = 'some-bad-flavor-id'
            serv['flavor_name'] = 'some-flavor-name'
            server._handle_image_or_flavor(serv, nova_client, 'image')
            server._handle_image_or_flavor(serv, nova_client, 'flavor')

        self.assertEquals('some-image-id', serv.get('image'))
        self.assertNotIn('image_name', serv)
        self.assertEquals('some-flavor-id', serv.get('flavor'))
        self.assertNotIn('flavor_name', serv)

    @staticmethod
    def _get_mocked_nova_client():
        nova_client = mock.MagicMock()

        def mock_get_if_exists(prop_name, **kwargs):
            is_image = prop_name == 'image'
            searched_name = kwargs.get('name')
            if (is_image and searched_name == 'some-image-name') or \
                    (not is_image and searched_name == 'some-flavor-name'):
                result = mock.MagicMock()
                result.id = 'some-image-id' if \
                    is_image else 'some-flavor-id'
                return result
            return []

        def mock_find_generator(prop_name):
            def mock_find(**kwargs):
                result = mock_get_if_exists(prop_name, **kwargs)
                if not result:
                    raise nova_exceptions.NotFound(404)
                return result
            return mock_find

        nova_client.cosmo_plural = lambda x: '{0}s'.format(x)
        nova_client.cosmo_get_if_exists = mock_get_if_exists
        nova_client.images.find = mock_find_generator('image')
        nova_client.flavors.find = mock_find_generator('flavor')
        return nova_client

    @staticmethod
    def _get_mock_ctx_with_node_properties(properties):
        return MockCloudifyContext(node_id='test_node_id',
                                   properties=properties)
