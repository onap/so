# -*- coding: utf-8 -*-
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

import unittest

from mock import Mock, patch
from requests.exceptions import RequestException

from neutron_plugin import security_group

from cloudify.exceptions import NonRecoverableError
from cloudify.state import current_ctx

from cloudify.mocks import MockCloudifyContext


class FakeException(Exception):
    pass


@patch('openstack_plugin_common.OpenStackClient._validate_auth_params')
@patch('openstack_plugin_common.NeutronClientWithSugar')
class TestSecurityGroup(unittest.TestCase):

    def setUp(self):
        super(TestSecurityGroup, self).setUp()
        self.nova_client = Mock()

        self.ctx = MockCloudifyContext(
            node_id='test',
            deployment_id='test',
            properties={
                'description': 'The best Security Group. Great',
                'rules': [],
                'resource_id': 'mock_sg',
                'security_group': {
                },
                'server': {},
                'openstack_config': {
                    'auth_url': 'things/v3',
                },
            },
            operation={'retry_number': 0},
            provider_context={'resources': {}}
        )
        current_ctx.set(self.ctx)
        self.addCleanup(current_ctx.clear)

        findctx = patch(
            'openstack_plugin_common._find_context_in_kw',
            return_value=self.ctx,
        )
        findctx.start()
        self.addCleanup(findctx.stop)

    def test_set_sg_runtime_properties(self, mock_nc, *_):
        security_group.create(
            nova_client=self.nova_client,
            ctx=self.ctx,
            args={},
            )

        self.assertEqual(
            {
                'external_type': 'security_group',
                'external_id': mock_nc().get_id_from_resource(),
                'external_name': mock_nc().get_name_from_resource(),
            },
            self.ctx.instance.runtime_properties
        )

    def test_create_sg_wait_timeout(self, mock_nc, *_):
        mock_nc().show_security_group.side_effect = RequestException

        with self.assertRaises(NonRecoverableError):
            security_group.create(
                nova_client=self.nova_client,
                ctx=self.ctx,
                args={},
                status_attempts=3,
                status_timeout=0.001,
                )

    @patch(
        'neutron_plugin.security_group.delete_resource_and_runtime_properties')
    def test_dont_duplicate_if_failed_rule(self, mock_del_res, mock_nc, *_):
        self.ctx.node.properties['rules'] = [
            {
                'port': '🍷',
            },
        ]
        mock_nc().create_security_group_rule.side_effect = FakeException
        mock_del_res.side_effect = FakeException('the 2nd')

        with self.assertRaises(NonRecoverableError) as e:
            security_group.create(
                nova_client=self.nova_client,
                ctx=self.ctx,
                args={},
                )

        self.assertIn('the 2nd', str(e.exception))
