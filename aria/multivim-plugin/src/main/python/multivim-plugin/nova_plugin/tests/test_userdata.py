#########
# Copyright (c) 2015 GigaSpaces Technologies Ltd. All rights reserved
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

from cloudify.mocks import MockCloudifyContext

from nova_plugin import userdata


def ctx_mock():
    result = MockCloudifyContext(
        node_id='d',
        properties={})
    result.node.type_hierarchy = ['cloudify.nodes.Compute']
    return result


class TestServerUserdataHandling(unittest.TestCase):

    @mock.patch('nova_plugin.userdata.ctx', ctx_mock())
    def test_no_userdata(self):
        server_conf = {}
        userdata.handle_userdata(server_conf)
        self.assertEqual(server_conf, {})

    def test_agent_installation_userdata(self):
        ctx = ctx_mock()
        ctx.agent.init_script = lambda: 'SCRIPT'
        with mock.patch('nova_plugin.userdata.ctx', ctx):
            server_conf = {}
            userdata.handle_userdata(server_conf)
            self.assertEqual(server_conf, {'userdata': 'SCRIPT'})

    @mock.patch('nova_plugin.userdata.ctx', ctx_mock())
    def test_existing_userdata(self):
        server_conf = {'userdata': 'EXISTING'}
        server_conf_copy = server_conf.copy()
        userdata.handle_userdata(server_conf)
        self.assertEqual(server_conf, server_conf_copy)

    def test_existing_and_agent_installation_userdata(self):
        ctx = ctx_mock()
        ctx.agent.init_script = lambda: '#! SCRIPT'
        with mock.patch('nova_plugin.userdata.ctx', ctx):
            server_conf = {'userdata': '#! EXISTING'}
            userdata.handle_userdata(server_conf)
            self.assertTrue(server_conf['userdata'].startswith(
                'Content-Type: multi'))
