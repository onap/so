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

import novaclient.v2.client as nvclient

from system_tests.openstack_handler import OpenstackHandler


class OpenstackNovaNetHandler(OpenstackHandler):

    # using the Config Readers of the regular OpenstackHandler - attempts
    # of reading neutron-related data may fail but shouldn't happen from
    # nova-net tests in the first place
    # CloudifyConfigReader = None

    def openstack_clients(self):
        creds = self._client_creds()
        return nvclient.Client(**creds)

    def openstack_infra_state(self):
        nova = self.openstack_clients()
        prefix = self.env.resources_prefix
        return {
            'security_groups': dict(self._security_groups(nova, prefix)),
            'servers': dict(self._servers(nova, prefix)),
            'key_pairs': dict(self._key_pairs(nova, prefix)),
            'floatingips': dict(self._floatingips(nova, prefix)),
        }

    def _floatingips(self, nova, prefix):
        return [(ip.id, ip.ip)
                for ip in nova.floating_ips.list()]

    def _security_groups(self, nova, prefix):
        return [(n.id, n.name)
                for n in nova.security_groups.list()
                if self._check_prefix(n.name, prefix)]

    def _remove_openstack_resources_impl(self, resources_to_remove):
        nova = self.openstack_clients()

        servers = nova.servers.list()
        keypairs = nova.keypairs.list()
        floatingips = nova.floating_ips.list()
        security_groups = nova.security_groups.list()

        failed = {
            'servers': {},
            'key_pairs': {},
            'floatingips': {},
            'security_groups': {}
        }

        for server in servers:
            if server.id in resources_to_remove['servers']:
                with self._handled_exception(server.id, failed, 'servers'):
                    nova.servers.delete(server)
        for key_pair in keypairs:
            if key_pair.name == self.env.agent_keypair_name and \
                    self.env.use_existing_agent_keypair:
                # this is a pre-existing agent key-pair, do not remove
                continue
            elif key_pair.name == self.env.management_keypair_name and \
                    self.env.use_existing_manager_keypair:
                # this is a pre-existing manager key-pair, do not remove
                continue
            elif key_pair.id in resources_to_remove['key_pairs']:
                with self._handled_exception(key_pair.id, failed, 'key_pairs'):
                    nova.keypairs.delete(key_pair)
        for floatingip in floatingips:
            if floatingip.id in resources_to_remove['floatingips']:
                with self._handled_exception(floatingip.id, failed,
                                             'floatingips'):
                    nova.floating_ips.delete(floatingip)
        for security_group in security_groups:
            if security_group.name == 'default':
                continue
            if security_group.id in resources_to_remove['security_groups']:
                with self._handled_exception(security_group.id, failed,
                                             'security_groups'):
                    nova.security_groups.delete(security_group)

        return failed


handler = OpenstackNovaNetHandler
