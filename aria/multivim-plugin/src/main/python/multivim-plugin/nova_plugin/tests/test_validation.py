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

import os
from os import path
import tempfile
import shutil

import unittest
import mock

from cloudify.test_utils import workflow_test
from nova_plugin.keypair import creation_validation
from cloudify.exceptions import NonRecoverableError

PRIVATE_KEY_NAME = 'private_key'


class TestValidation(unittest.TestCase):

    blueprint_path = path.join('resources',
                               'test-keypair-validation-blueprint.yaml')

    def setUp(self):
        _, fp = tempfile.mkstemp()
        self.private_key = fp
        _, fp = tempfile.mkstemp()
        self.not_readable_private_key = fp
        os.chmod(self.not_readable_private_key, 0o200)
        self.temp_dir = tempfile.mkdtemp()
        self.not_writable_temp_dir_r = tempfile.mkdtemp()
        os.chmod(self.not_writable_temp_dir_r, 0o400)
        self.not_writable_temp_dir_rx = tempfile.mkdtemp()
        os.chmod(self.not_writable_temp_dir_rx, 0o500)
        self.not_writable_temp_dir_rw = tempfile.mkdtemp()
        os.chmod(self.not_writable_temp_dir_rw, 0o600)

    def tearDown(self):
        if self.private_key:
            os.remove(self.private_key)

        if self.not_readable_private_key:
            os.remove(self.not_readable_private_key)

        shutil.rmtree(self.not_writable_temp_dir_r, ignore_errors=True)
        shutil.rmtree(self.not_writable_temp_dir_rx, ignore_errors=True)
        shutil.rmtree(self.not_writable_temp_dir_rw, ignore_errors=True)
        shutil.rmtree(self.temp_dir, ignore_errors=True)

    def new_keypair_create(self, *args, **kwargs):
        creation_validation(*args, **kwargs)

    def new_keypair_create_with_exception(self, *args, **kwargs):
        self.assertRaises(NonRecoverableError, creation_validation,
                          *args, **kwargs)

    def get_keypair_inputs_private_key(self, is_external, **kwargs):
        return {
            'private_key': self.private_key,
            'is_keypair_external': is_external
        }

    def get_keypair_inputs_not_readable_private_key(self,
                                                    is_external, **kwargs):
        return {
            'private_key': self.not_readable_private_key,
            'is_keypair_external': is_external
        }

    def get_keypair_inputs_not_writable_dir_r(self, is_external, **kwargs):
        return {
            'private_key': path.join(self.not_writable_temp_dir_r,
                                     PRIVATE_KEY_NAME),
            'is_keypair_external': is_external
        }

    def get_keypair_inputs_not_writable_dir_rx(self, is_external, **kwargs):
        return {
            'private_key': path.join(self.not_writable_temp_dir_rx,
                                     PRIVATE_KEY_NAME),
            'is_keypair_external': is_external
        }

    def get_keypair_inputs_not_writable_dir_rw(self, is_external, **kwargs):
        return {
            'private_key': path.join(self.not_writable_temp_dir_rw,
                                     PRIVATE_KEY_NAME),
            'is_keypair_external': is_external
        }

    def get_keypair_inputs_temp_dir(self, is_external, **kwargs):
        return {
            'private_key': path.join(self.temp_dir, PRIVATE_KEY_NAME),
            'is_keypair_external': is_external
        }

    @workflow_test(blueprint_path, inputs={
        'private_key': '',
        'is_keypair_external': False
    })
    @mock.patch('nova_plugin.keypair.validate_resource')
    def test_keypair_valid_config(self, cfy_local, *args):

        with mock.patch('nova_plugin.keypair.create',
                        new=self.new_keypair_create):
            cfy_local.execute('install', task_retries=0)

    @workflow_test(blueprint_path, inputs='get_keypair_inputs_private_key',
                   input_func_kwargs={'is_external': True})
    @mock.patch('nova_plugin.keypair.validate_resource')
    def test_keypair_valid_config_external(self, cfy_local, *args):

        with mock.patch('nova_plugin.keypair.create',
                        new=self.new_keypair_create):
            cfy_local.execute('install', task_retries=0)

    @workflow_test(blueprint_path, inputs='get_keypair_inputs_temp_dir',
                   input_func_kwargs={'is_external': True})
    @mock.patch('nova_plugin.keypair.validate_resource')
    def test_keypair_no_private_key(self, cfy_local, *args):

        with mock.patch('nova_plugin.keypair.create',
                        new=self.new_keypair_create_with_exception):
            cfy_local.execute('install', task_retries=0)

    @workflow_test(blueprint_path, inputs='get_keypair_inputs_private_key',
                   input_func_kwargs={'is_external': False})
    @mock.patch('nova_plugin.keypair.validate_resource')
    def test_keypair_local_and_exists(self, cfy_local, *args):

        with mock.patch('nova_plugin.keypair.create',
                        new=self.new_keypair_create_with_exception):
            cfy_local.execute('install', task_retries=0)

    @workflow_test(blueprint_path, inputs='get_keypair_inputs_temp_dir',
                   input_func_kwargs={'is_external': False})
    @mock.patch('nova_plugin.keypair.validate_resource')
    def test_keypair_local_temp_dir(self, cfy_local, *args):

        with mock.patch('nova_plugin.keypair.create',
                        new=self.new_keypair_create):
            cfy_local.execute('install', task_retries=0)

    @workflow_test(blueprint_path,
                   inputs='get_keypair_inputs_not_writable_dir_r',
                   input_func_kwargs={'is_external': False})
    @mock.patch('nova_plugin.keypair.validate_resource')
    def test_keypair_local_non_writable_dir_r(self, cfy_local, *args):

        with mock.patch('nova_plugin.keypair.create',
                        new=self.new_keypair_create_with_exception):
            cfy_local.execute('install', task_retries=0)

    @workflow_test(blueprint_path,
                   inputs='get_keypair_inputs_not_writable_dir_rx',
                   input_func_kwargs={'is_external': False})
    @mock.patch('nova_plugin.keypair.validate_resource')
    def test_keypair_local_non_writable_dir_rx(self, cfy_local, *args):

        with mock.patch('nova_plugin.keypair.create',
                        new=self.new_keypair_create_with_exception):
            cfy_local.execute('install', task_retries=0)

    @workflow_test(blueprint_path,
                   inputs='get_keypair_inputs_not_writable_dir_rw',
                   input_func_kwargs={'is_external': False})
    @mock.patch('nova_plugin.keypair.validate_resource')
    def test_keypair_local_non_writable_dir_rw(self, cfy_local, *args):

        with mock.patch('nova_plugin.keypair.create',
                        new=self.new_keypair_create_with_exception):
            cfy_local.execute('install', task_retries=0)

    @workflow_test(blueprint_path,
                   inputs='get_keypair_inputs_not_readable_private_key',
                   input_func_kwargs={'is_external': True})
    @mock.patch('nova_plugin.keypair.validate_resource')
    def test_keypair_not_readable_private_key(self, cfy_local, *args):

        with mock.patch('nova_plugin.keypair.create',
                        new=self.new_keypair_create_with_exception):
            cfy_local.execute('install', task_retries=0)
