import mock
import unittest

from cloudify.context import NODE_INSTANCE

from cloudify.mocks import (
    MockContext,
    MockNodeInstanceContext,
    MockNodeContext
)
from openstack_plugin_common import (
    OPENSTACK_ID_PROPERTY,
    OPENSTACK_NAME_PROPERTY,
    OPENSTACK_TYPE_PROPERTY
    )
from keystone_plugin.project import PROJECT_OPENSTACK_TYPE
import keystone_plugin


class TestProject(unittest.TestCase):

    test_id = 'test-id'
    test_name = 'test-name'
    test_deployment_id = 'test-deployment-id'
    test_user = 'test-user'
    test_role = 'test-role'

    class MockProjectOS:
        def __init__(self, id, name):
            self._id = id
            self._name = name
            self._users = {}

        @property
        def id(self):
            return self._id

        @property
        def name(self):
            return self._name

        def find(self, *_, **__):
            return mock.MagicMock(id='test-role')

        def grant(self, role, user, *_, **__):
            self._users[user] = role

    def mock_keystone_client(self, mock_project):
        keystone_client = mock.MagicMock()
        keystone_client.projects.create.return_value = mock_project
        keystone_client.users.find.return_value = mock.MagicMock(
            id=self.test_user)
        keystone_client.roles = mock_project
        return keystone_client

    def mock_ctx(self, test_vars, test_id,
                 test_deployment_id, runtime_properties=None):
        ctx = MockContext()
        ctx.node = MockNodeContext(properties=test_vars)
        ctx.instance = MockNodeInstanceContext(
            id=test_id, runtime_properties=runtime_properties or {})
        ctx.deployment = mock.Mock()
        ctx.deployment.id = test_deployment_id
        ctx.type = NODE_INSTANCE
        return ctx

    @mock.patch('openstack_plugin_common._put_client_in_kw',
                autospec=True, return_value=None)
    def test_keystone_project_create(self, *_):
        test_vars = {
            'project': {},
            'resource_id': '',
            'quota': {},
            'users': {}
        }

        ctx = self.mock_ctx(test_vars, self.test_id, self.test_deployment_id)
        keystone_plugin.project.ctx = ctx
        keystone_plugin.project.create(
            self.mock_keystone_client(self.MockProjectOS(self.test_id,
                                                         self.test_name)))
        self.assertEqual(self.test_name,
                         ctx.instance.runtime_properties[
                             OPENSTACK_NAME_PROPERTY])
        self.assertEqual(self.test_id,
                         ctx.instance.runtime_properties[
                             OPENSTACK_ID_PROPERTY])
        self.assertEqual(PROJECT_OPENSTACK_TYPE,
                         ctx.instance.runtime_properties[
                             OPENSTACK_TYPE_PROPERTY])

    @mock.patch('openstack_plugin_common._put_client_in_kw',
                autospec=True, return_value=None)
    def test_assign_user(self, *_):
        test_vars = {
            'project': {},
            'resource_id': '',
            'quota': {},
            'users': [{'name': self.test_user,
                       'roles': [self.test_role]}]
        }
        ctx = self.mock_ctx(test_vars,
                            self.test_id,
                            self.test_deployment_id,
                            {OPENSTACK_ID_PROPERTY: self.test_id})
        mock_project = self.MockProjectOS(self.test_id, self.test_name)
        keystone_client = self.mock_keystone_client(mock_project)
        keystone_plugin.project.ctx = ctx
        keystone_plugin.project.start(
            keystone_client,
            mock.MagicMock(),  # nova_client
            mock.MagicMock(),  # cinder_client
            mock.MagicMock())  # neutron_client
        self.assertEqual({self.test_user: self.test_role},
                         mock_project._users)
