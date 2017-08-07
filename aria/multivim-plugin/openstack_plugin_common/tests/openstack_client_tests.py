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

import os
import unittest
import tempfile
import json
import __builtin__ as builtins

import mock
from cloudify.exceptions import NonRecoverableError

from cloudify.mocks import MockCloudifyContext
import openstack_plugin_common as common


class ConfigTests(unittest.TestCase):

    @mock.patch.dict('os.environ', clear=True)
    def test__build_config_from_env_variables_empty(self):
        cfg = common.Config._build_config_from_env_variables()
        self.assertEqual({}, cfg)

    @mock.patch.dict('os.environ', clear=True,
                     OS_AUTH_URL='test_url')
    def test__build_config_from_env_variables_single(self):
        cfg = common.Config._build_config_from_env_variables()
        self.assertEqual({'auth_url': 'test_url'}, cfg)

    @mock.patch.dict('os.environ', clear=True,
                     OS_AUTH_URL='test_url',
                     OS_PASSWORD='pass',
                     OS_REGION_NAME='region')
    def test__build_config_from_env_variables_multiple(self):
        cfg = common.Config._build_config_from_env_variables()
        self.assertEqual({
            'auth_url': 'test_url',
            'password': 'pass',
            'region_name': 'region',
        }, cfg)

    @mock.patch.dict('os.environ', clear=True,
                     OS_INVALID='invalid',
                     PASSWORD='pass',
                     os_region_name='region')
    def test__build_config_from_env_variables_all_ignored(self):
        cfg = common.Config._build_config_from_env_variables()
        self.assertEqual({}, cfg)

    @mock.patch.dict('os.environ', clear=True,
                     OS_AUTH_URL='test_url',
                     OS_PASSWORD='pass',
                     OS_REGION_NAME='region',
                     OS_INVALID='invalid',
                     PASSWORD='pass',
                     os_region_name='region')
    def test__build_config_from_env_variables_extract_valid(self):
        cfg = common.Config._build_config_from_env_variables()
        self.assertEqual({
            'auth_url': 'test_url',
            'password': 'pass',
            'region_name': 'region',
        }, cfg)

    def test_update_config_empty_target(self):
        target = {}
        override = {'k1': 'u1'}
        result = override.copy()

        common.Config.update_config(target, override)
        self.assertEqual(result, target)

    def test_update_config_empty_override(self):
        target = {'k1': 'v1'}
        override = {}
        result = target.copy()

        common.Config.update_config(target, override)
        self.assertEqual(result, target)

    def test_update_config_disjoint_configs(self):
        target = {'k1': 'v1'}
        override = {'k2': 'u2'}
        result = target.copy()
        result.update(override)

        common.Config.update_config(target, override)
        self.assertEqual(result, target)

    def test_update_config_do_not_remove_empty_from_target(self):
        target = {'k1': ''}
        override = {}
        result = target.copy()

        common.Config.update_config(target, override)
        self.assertEqual(result, target)

    def test_update_config_no_empty_in_override(self):
        target = {'k1': 'v1', 'k2': 'v2'}
        override = {'k1': 'u2'}
        result = target.copy()
        result.update(override)

        common.Config.update_config(target, override)
        self.assertEqual(result, target)

    def test_update_config_all_empty_in_override(self):
        target = {'k1': '', 'k2': 'v2'}
        override = {'k1': '', 'k3': ''}
        result = target.copy()

        common.Config.update_config(target, override)
        self.assertEqual(result, target)

    def test_update_config_misc(self):
        target = {'k1': 'v1', 'k2': 'v2'}
        override = {'k1': '', 'k2': 'u2', 'k3': '', 'k4': 'u4'}
        result = {'k1': 'v1', 'k2': 'u2', 'k4': 'u4'}

        common.Config.update_config(target, override)
        self.assertEqual(result, target)

    @mock.patch.object(common.Config, 'update_config')
    @mock.patch.object(common.Config, '_build_config_from_env_variables',
                       return_value={})
    @mock.patch.dict('os.environ', clear=True,
                     values={common.Config.OPENSTACK_CONFIG_PATH_ENV_VAR:
                             '/this/should/not/exist.json'})
    def test_get_missing_static_config_missing_file(self, from_env, update):
        cfg = common.Config.get()
        self.assertEqual({}, cfg)
        from_env.assert_called_once_with()
        update.assert_not_called()

    @mock.patch.object(common.Config, 'update_config')
    @mock.patch.object(common.Config, '_build_config_from_env_variables',
                       return_value={})
    def test_get_empty_static_config_present_file(self, from_env, update):
        file_cfg = {'k1': 'v1', 'k2': 'v2'}
        env_var = common.Config.OPENSTACK_CONFIG_PATH_ENV_VAR
        file = tempfile.NamedTemporaryFile(delete=False)
        json.dump(file_cfg, file)
        file.close()

        with mock.patch.dict('os.environ', {env_var: file.name}, clear=True):
            common.Config.get()

        os.unlink(file.name)
        from_env.assert_called_once_with()
        update.assert_called_once_with({}, file_cfg)

    @mock.patch.object(common.Config, 'update_config')
    @mock.patch.object(common.Config, '_build_config_from_env_variables',
                       return_value={'k1': 'v1'})
    def test_get_present_static_config_empty_file(self, from_env, update):
        file_cfg = {}
        env_var = common.Config.OPENSTACK_CONFIG_PATH_ENV_VAR
        file = tempfile.NamedTemporaryFile(delete=False)
        json.dump(file_cfg, file)
        file.close()

        with mock.patch.dict('os.environ', {env_var: file.name}, clear=True):
            common.Config.get()

        os.unlink(file.name)
        from_env.assert_called_once_with()
        update.assert_called_once_with({'k1': 'v1'}, file_cfg)

    @mock.patch.object(common.Config, 'update_config')
    @mock.patch.object(common.Config, '_build_config_from_env_variables',
                       return_value={'k1': 'v1'})
    @mock.patch.dict('os.environ', clear=True,
                     values={common.Config.OPENSTACK_CONFIG_PATH_ENV_VAR:
                             '/this/should/not/exist.json'})
    def test_get_present_static_config_missing_file(self, from_env, update):
        cfg = common.Config.get()
        self.assertEqual({'k1': 'v1'}, cfg)
        from_env.assert_called_once_with()
        update.assert_not_called()

    @mock.patch.object(common.Config, 'update_config')
    @mock.patch.object(common.Config, '_build_config_from_env_variables',
                       return_value={'k1': 'v1'})
    def test_get_all_present(self, from_env, update):
        file_cfg = {'k2': 'u2'}
        env_var = common.Config.OPENSTACK_CONFIG_PATH_ENV_VAR
        file = tempfile.NamedTemporaryFile(delete=False)
        json.dump(file_cfg, file)
        file.close()

        with mock.patch.dict('os.environ', {env_var: file.name}, clear=True):
            common.Config.get()

        os.unlink(file.name)
        from_env.assert_called_once_with()
        update.assert_called_once_with({'k1': 'v1'}, file_cfg)


class OpenstackClientTests(unittest.TestCase):

    def test__merge_custom_configuration_no_custom_cfg(self):
        cfg = {'k1': 'v1'}
        new = common.OpenStackClient._merge_custom_configuration(cfg, "dummy")
        self.assertEqual(cfg, new)

    def test__merge_custom_configuration_client_present(self):
        cfg = {
            'k1': 'v1',
            'k2': 'v2',
            'custom_configuration': {
                'dummy': {
                    'k2': 'u2',
                    'k3': 'u3'
                }
            }
        }
        result = {
            'k1': 'v1',
            'k2': 'u2',
            'k3': 'u3'
        }
        bak = cfg.copy()
        new = common.OpenStackClient._merge_custom_configuration(cfg, "dummy")
        self.assertEqual(result, new)
        self.assertEqual(cfg, bak)

    def test__merge_custom_configuration_client_missing(self):
        cfg = {
            'k1': 'v1',
            'k2': 'v2',
            'custom_configuration': {
                'dummy': {
                    'k2': 'u2',
                    'k3': 'u3'
                }
            }
        }
        result = {
            'k1': 'v1',
            'k2': 'v2'
        }
        bak = cfg.copy()
        new = common.OpenStackClient._merge_custom_configuration(cfg, "baddy")
        self.assertEqual(result, new)
        self.assertEqual(cfg, bak)

    def test__merge_custom_configuration_multi_client(self):
        cfg = {
            'k1': 'v1',
            'k2': 'v2',
            'custom_configuration': {
                'dummy': {
                    'k2': 'u2',
                    'k3': 'u3'
                },
                'bummy': {
                    'k1': 'z1'
                }
            }
        }
        result = {
            'k1': 'z1',
            'k2': 'v2',
        }
        bak = cfg.copy()
        new = common.OpenStackClient._merge_custom_configuration(cfg, "bummy")
        self.assertEqual(result, new)
        self.assertEqual(cfg, bak)

    @mock.patch.object(common, 'ctx')
    def test__merge_custom_configuration_nova_url(self, mock_ctx):
        cfg = {
            'nova_url': 'gopher://nova',
        }
        bak = cfg.copy()

        self.assertEqual(
            common.OpenStackClient._merge_custom_configuration(
                cfg, 'nova_client'),
            {'endpoint_override': 'gopher://nova'},
        )
        self.assertEqual(
            common.OpenStackClient._merge_custom_configuration(
                cfg, 'dummy'),
            {},
        )
        self.assertEqual(cfg, bak)
        mock_ctx.logger.warn.assert_has_calls([
            mock.call(
                "'nova_url' property is deprecated. Use `custom_configuration."
                "nova_client.endpoint_override` instead."),
            mock.call(
                "'nova_url' property is deprecated. Use `custom_configuration."
                "nova_client.endpoint_override` instead."),
        ])

    @mock.patch('keystoneauth1.session.Session')
    def test___init___multi_region(self, m_session):
        mock_client_class = mock.MagicMock()

        cfg = {
            'auth_url': 'test-auth_url/v3',
            'region': 'test-region',
        }

        with mock.patch.object(
            builtins, 'open',
            mock.mock_open(
                read_data="""
                {
                    "region": "region from file",
                    "other": "this one should get through"
                }
                """
            ),
            create=True,
        ):
            common.OpenStackClient('fred', mock_client_class, cfg)

        mock_client_class.assert_called_once_with(
            region_name='test-region',
            other='this one should get through',
            session=m_session.return_value,
            )

    def test__validate_auth_params_missing(self):
        with self.assertRaises(NonRecoverableError):
            common.OpenStackClient._validate_auth_params({})

    def test__validate_auth_params_too_much(self):
        with self.assertRaises(NonRecoverableError):
            common.OpenStackClient._validate_auth_params({
                'auth_url': 'url',
                'password': 'pass',
                'username': 'user',
                'tenant_name': 'tenant',
                'project_id': 'project_test',
            })

    def test__validate_auth_params_v2(self):
        common.OpenStackClient._validate_auth_params({
            'auth_url': 'url',
            'password': 'pass',
            'username': 'user',
            'tenant_name': 'tenant',
        })

    def test__validate_auth_params_v3(self):
        common.OpenStackClient._validate_auth_params({
            'auth_url': 'url',
            'password': 'pass',
            'username': 'user',
            'project_id': 'project_test',
            'user_domain_name': 'user_domain',
        })

    def test__validate_auth_params_v3_mod(self):
        common.OpenStackClient._validate_auth_params({
            'auth_url': 'url',
            'password': 'pass',
            'username': 'user',
            'user_domain_name': 'user_domain',
            'project_name': 'project_test_name',
            'project_domain_name': 'project_domain',
        })

    def test__validate_auth_params_skip_insecure(self):
        common.OpenStackClient._validate_auth_params({
            'auth_url': 'url',
            'password': 'pass',
            'username': 'user',
            'user_domain_name': 'user_domain',
            'project_name': 'project_test_name',
            'project_domain_name': 'project_domain',
            'insecure': True
        })

    def test__split_config(self):
        auth = {'auth_url': 'url', 'password': 'pass'}
        misc = {'misc1': 'val1', 'misc2': 'val2'}
        all = dict(auth)
        all.update(misc)

        a, m = common.OpenStackClient._split_config(all)

        self.assertEqual(auth, a)
        self.assertEqual(misc, m)

    @mock.patch.object(common, 'loading')
    @mock.patch.object(common, 'session')
    def test__authenticate_secure(self, mock_session, mock_loading):
        auth_params = {'k1': 'v1'}
        common.OpenStackClient._authenticate(auth_params)
        loader = mock_loading.get_plugin_loader.return_value
        loader.load_from_options.assert_called_once_with(k1='v1')
        auth = loader.load_from_options.return_value
        mock_session.Session.assert_called_once_with(auth=auth, verify=True)

    @mock.patch.object(common, 'loading')
    @mock.patch.object(common, 'session')
    def test__authenticate_secure_explicit(self, mock_session, mock_loading):
        auth_params = {'k1': 'v1', 'insecure': False}
        common.OpenStackClient._authenticate(auth_params)
        loader = mock_loading.get_plugin_loader.return_value
        loader.load_from_options.assert_called_once_with(k1='v1')
        auth = loader.load_from_options.return_value
        mock_session.Session.assert_called_once_with(auth=auth, verify=True)

    @mock.patch.object(common, 'loading')
    @mock.patch.object(common, 'session')
    def test__authenticate_insecure(self, mock_session, mock_loading):
        auth_params = {'k1': 'v1', 'insecure': True}
        common.OpenStackClient._authenticate(auth_params)
        loader = mock_loading.get_plugin_loader.return_value
        loader.load_from_options.assert_called_once_with(k1='v1')
        auth = loader.load_from_options.return_value
        mock_session.Session.assert_called_once_with(auth=auth, verify=False)

    @mock.patch.object(common, 'loading')
    @mock.patch.object(common, 'session')
    def test__authenticate_secure_misc(self, mock_session, mock_loading):
        params = {'k1': 'v1'}
        tests = ('', 'a', [], {}, set(), 4, 0, -1, 3.14, 0.0, None)
        for test in tests:
            auth_params = params.copy()
            auth_params['insecure'] = test

            common.OpenStackClient._authenticate(auth_params)
            loader = mock_loading.get_plugin_loader.return_value
            loader.load_from_options.assert_called_with(**params)
            auth = loader.load_from_options.return_value
            mock_session.Session.assert_called_with(auth=auth, verify=True)

    @mock.patch.object(common, 'cinder_client')
    def test_cinder_client_get_name_from_resource(self, cc_mock):
        ccws = common.CinderClientWithSugar()
        mock_volume = mock.Mock()

        self.assertIs(
            mock_volume.name,
            ccws.get_name_from_resource(mock_volume))


class ClientsConfigTest(unittest.TestCase):

    def setUp(self):
        file = tempfile.NamedTemporaryFile(delete=False)
        json.dump(self.get_file_cfg(), file)
        file.close()
        self.addCleanup(os.unlink, file.name)

        env_cfg = self.get_env_cfg()
        env_cfg[common.Config.OPENSTACK_CONFIG_PATH_ENV_VAR] = file.name
        mock.patch.dict('os.environ', env_cfg, clear=True).start()

        self.loading = mock.patch.object(common, 'loading').start()
        self.session = mock.patch.object(common, 'session').start()
        self.nova = mock.patch.object(common, 'nova_client').start()
        self.neutron = mock.patch.object(common, 'neutron_client').start()
        self.cinder = mock.patch.object(common, 'cinder_client').start()
        self.addCleanup(mock.patch.stopall)

        self.loader = self.loading.get_plugin_loader.return_value
        self.auth = self.loader.load_from_options.return_value


class CustomConfigFromInputs(ClientsConfigTest):

    def get_file_cfg(self):
        return {
            'username': 'file-username',
            'password': 'file-password',
            'tenant_name': 'file-tenant-name',
            'custom_configuration': {
                'nova_client': {
                    'username': 'custom-username',
                    'password': 'custom-password',
                    'tenant_name': 'custom-tenant-name'
                },
            }
        }

    def get_inputs_cfg(self):
        return {
            'auth_url': 'envar-auth-url',
            'username': 'inputs-username',
            'custom_configuration': {
                'neutron_client': {
                    'password': 'inputs-custom-password'
                },
                'cinder_client': {
                    'password': 'inputs-custom-password',
                    'auth_url': 'inputs-custom-auth-url',
                    'extra_key': 'extra-value'
                },
            }
        }

    def get_env_cfg(self):
        return {
            'OS_USERNAME': 'envar-username',
            'OS_PASSWORD': 'envar-password',
            'OS_TENANT_NAME': 'envar-tenant-name',
            'OS_AUTH_URL': 'envar-auth-url',
            common.Config.OPENSTACK_CONFIG_PATH_ENV_VAR: file.name
        }

    def test_nova(self):
        common.NovaClientWithSugar(config=self.get_inputs_cfg())
        self.loader.load_from_options.assert_called_once_with(
            username='inputs-username',
            password='file-password',
            tenant_name='file-tenant-name',
            auth_url='envar-auth-url'
        )
        self.session.Session.assert_called_with(auth=self.auth, verify=True)
        self.nova.Client.assert_called_once_with(
            '2', session=self.session.Session.return_value)

    def test_neutron(self):
        common.NeutronClientWithSugar(config=self.get_inputs_cfg())
        self.loader.load_from_options.assert_called_once_with(
            username='inputs-username',
            password='inputs-custom-password',
            tenant_name='file-tenant-name',
            auth_url='envar-auth-url'
        )
        self.session.Session.assert_called_with(auth=self.auth, verify=True)
        self.neutron.Client.assert_called_once_with(
            session=self.session.Session.return_value)

    def test_cinder(self):
        common.CinderClientWithSugar(config=self.get_inputs_cfg())
        self.loader.load_from_options.assert_called_once_with(
            username='inputs-username',
            password='inputs-custom-password',
            tenant_name='file-tenant-name',
            auth_url='inputs-custom-auth-url'
        )
        self.session.Session.assert_called_with(auth=self.auth, verify=True)
        self.cinder.Client.assert_called_once_with(
            '2', session=self.session.Session.return_value,
            extra_key='extra-value')


class CustomConfigFromFile(ClientsConfigTest):

    def get_file_cfg(self):
        return {
            'username': 'file-username',
            'password': 'file-password',
            'tenant_name': 'file-tenant-name',
            'custom_configuration': {
                'nova_client': {
                    'username': 'custom-username',
                    'password': 'custom-password',
                    'tenant_name': 'custom-tenant-name'
                },
            }
        }

    def get_inputs_cfg(self):
        return {
            'auth_url': 'envar-auth-url',
            'username': 'inputs-username',
        }

    def get_env_cfg(self):
        return {
            'OS_USERNAME': 'envar-username',
            'OS_PASSWORD': 'envar-password',
            'OS_TENANT_NAME': 'envar-tenant-name',
            'OS_AUTH_URL': 'envar-auth-url',
            common.Config.OPENSTACK_CONFIG_PATH_ENV_VAR: file.name
        }

    def test_nova(self):
        common.NovaClientWithSugar(config=self.get_inputs_cfg())
        self.loader.load_from_options.assert_called_once_with(
            username='custom-username',
            password='custom-password',
            tenant_name='custom-tenant-name',
            auth_url='envar-auth-url'
        )
        self.session.Session.assert_called_with(auth=self.auth, verify=True)
        self.nova.Client.assert_called_once_with(
            '2', session=self.session.Session.return_value)

    def test_neutron(self):
        common.NeutronClientWithSugar(config=self.get_inputs_cfg())
        self.loader.load_from_options.assert_called_once_with(
            username='inputs-username',
            password='file-password',
            tenant_name='file-tenant-name',
            auth_url='envar-auth-url'
        )
        self.session.Session.assert_called_with(auth=self.auth, verify=True)
        self.neutron.Client.assert_called_once_with(
            session=self.session.Session.return_value)

    def test_cinder(self):
        common.CinderClientWithSugar(config=self.get_inputs_cfg())
        self.loader.load_from_options.assert_called_once_with(
            username='inputs-username',
            password='file-password',
            tenant_name='file-tenant-name',
            auth_url='envar-auth-url'
        )
        self.session.Session.assert_called_with(auth=self.auth, verify=True)
        self.cinder.Client.assert_called_once_with(
            '2', session=self.session.Session.return_value)


class PutClientInKwTests(unittest.TestCase):

    def test_override_prop_empty_ctx(self):
        props = {}
        ctx = MockCloudifyContext(node_id='a20846', properties=props)
        kwargs = {
            'ctx': ctx,
            'openstack_config': {
                'p1': 'v1'
            }
        }
        expected_cfg = kwargs['openstack_config']

        client_class = mock.MagicMock()
        common._put_client_in_kw('mock_client', client_class, kwargs)
        client_class.assert_called_once_with(config=expected_cfg)

    def test_override_prop_nonempty_ctx(self):
        props = {
            'openstack_config': {
                'p1': 'u1',
                'p2': 'u2'
            }
        }
        props_copy = props.copy()
        ctx = MockCloudifyContext(node_id='a20846', properties=props)
        kwargs = {
            'ctx': ctx,
            'openstack_config': {
                'p1': 'v1',
                'p3': 'v3'
            }
        }
        expected_cfg = {
            'p1': 'v1',
            'p2': 'u2',
            'p3': 'v3'
        }

        client_class = mock.MagicMock()
        common._put_client_in_kw('mock_client', client_class, kwargs)
        client_class.assert_called_once_with(config=expected_cfg)
        # Making sure that _put_client_in_kw will not modify
        # 'openstack_config' property of a node.
        self.assertEqual(props_copy, ctx.node.properties)

    def test_override_runtime_prop(self):
        props = {
            'openstack_config': {
                'p1': 'u1',
                'p2': 'u2'
            }
        }
        runtime_props = {
            'openstack_config': {
                'p1': 'u3'
            }
        }
        props_copy = props.copy()
        runtime_props_copy = runtime_props.copy()
        ctx = MockCloudifyContext(node_id='a20847', properties=props,
                                  runtime_properties=runtime_props)
        kwargs = {
            'ctx': ctx
        }
        expected_cfg = {
            'p1': 'u3',
            'p2': 'u2'
        }
        client_class = mock.MagicMock()
        common._put_client_in_kw('mock_client', client_class, kwargs)
        client_class.assert_called_once_with(config=expected_cfg)
        self.assertEqual(props_copy, ctx.node.properties)
        self.assertEqual(runtime_props_copy, ctx.instance.runtime_properties)


class ResourceQuotaTests(unittest.TestCase):

    def _test_quota_validation(self, amount, quota, failure_expected):
        ctx = MockCloudifyContext(node_id='node_id', properties={})
        client = mock.MagicMock()

        def mock_cosmo_list(_):
            return [x for x in range(0, amount)]
        client.cosmo_list = mock_cosmo_list

        def mock_get_quota(_):
            return quota
        client.get_quota = mock_get_quota

        if failure_expected:
            self.assertRaisesRegexp(
                NonRecoverableError,
                'cannot be created due to quota limitations',
                common.validate_resource,
                ctx=ctx, sugared_client=client,
                openstack_type='openstack_type')
        else:
            common.validate_resource(
                ctx=ctx, sugared_client=client,
                openstack_type='openstack_type')

    def test_equals_quotas(self):
        self._test_quota_validation(3, 3, True)

    def test_exceeded_quota(self):
        self._test_quota_validation(5, 3, True)

    def test_infinite_quota(self):
        self._test_quota_validation(5, -1, False)


class UseExternalResourceTests(unittest.TestCase):

    def _test_use_external_resource(self,
                                    is_external,
                                    create_if_missing,
                                    exists):
        properties = {'create_if_missing': create_if_missing,
                      'use_external_resource': is_external,
                      'resource_id': 'resource_id'}
        client_mock = mock.MagicMock()
        os_type = 'test'

        def _raise_error(*_):
            raise NonRecoverableError('Error')

        def _return_something(*_):
            return mock.MagicMock()

        return_value = _return_something if exists else _raise_error
        if exists:
            properties.update({'resource_id': 'rid'})

        node_context = MockCloudifyContext(node_id='a20847',
                                           properties=properties)
        with mock.patch(
                'openstack_plugin_common._get_resource_by_name_or_id_from_ctx',
                new=return_value):
            return common.use_external_resource(node_context,
                                                client_mock, os_type)

    def test_use_existing_resource(self):
        self.assertIsNotNone(self._test_use_external_resource(True, True,
                                                              True))
        self.assertIsNotNone(self._test_use_external_resource(True, False,
                                                              True))

    def test_create_resource(self):
        self.assertIsNone(self._test_use_external_resource(False, True, False))
        self.assertIsNone(self._test_use_external_resource(False, False,
                                                           False))
        self.assertIsNone(self._test_use_external_resource(True, True, False))

    def test_raise_error(self):
        # If exists and shouldn't it is checked in resource
        # validation so below scenario is not tested here
        self.assertRaises(NonRecoverableError,
                          self._test_use_external_resource,
                          is_external=True,
                          create_if_missing=False,
                          exists=False)


class ValidateResourceTests(unittest.TestCase):

    def _test_validate_resource(self,
                                is_external,
                                create_if_missing,
                                exists,
                                client_mock_provided=None):
        properties = {'create_if_missing': create_if_missing,
                      'use_external_resource': is_external,
                      'resource_id': 'resource_id'}
        client_mock = client_mock_provided or mock.MagicMock()
        os_type = 'test'

        def _raise_error(*_):
            raise NonRecoverableError('Error')

        def _return_something(*_):
            return mock.MagicMock()
        return_value = _return_something if exists else _raise_error
        if exists:
            properties.update({'resource_id': 'rid'})

        node_context = MockCloudifyContext(node_id='a20847',
                                           properties=properties)
        with mock.patch(
                'openstack_plugin_common._get_resource_by_name_or_id_from_ctx',
                new=return_value):
            return common.validate_resource(node_context, client_mock, os_type)

    def test_use_existing_resource(self):
        self._test_validate_resource(True, True, True)
        self._test_validate_resource(True, False, True)

    def test_create_resource(self):
        client_mock = mock.MagicMock()
        client_mock.cosmo_list.return_value = ['a', 'b', 'c']
        client_mock.get_quota.return_value = 5
        self._test_validate_resource(False, True, False, client_mock)
        self._test_validate_resource(False, False, False, client_mock)
        self._test_validate_resource(True, True, False, client_mock)

    def test_raise_error(self):
        # If exists and shouldn't it is checked in resource
        # validation so below scenario is not tested here
        self.assertRaises(NonRecoverableError,
                          self._test_validate_resource,
                          is_external=True,
                          create_if_missing=False,
                          exists=False)

    def test_raise_quota_error(self):
        client_mock = mock.MagicMock()
        client_mock.cosmo_list.return_value = ['a', 'b', 'c']
        client_mock.get_quota.return_value = 3
        self.assertRaises(NonRecoverableError,
                          self._test_validate_resource,
                          is_external=True,
                          create_if_missing=True,
                          exists=False,
                          client_mock_provided=client_mock)
