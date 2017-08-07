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

import mock
import os
import tempfile
import unittest

import glance_plugin
from glance_plugin import image

from cloudify.mocks import MockCloudifyContext
from cloudify.test_utils import workflow_test
from cloudify.exceptions import NonRecoverableError


def ctx_mock(image_dict):
    return MockCloudifyContext(
        node_id='d',
        properties=image_dict)


class TestCheckImage(unittest.TestCase):

    @mock.patch('glance_plugin.image.ctx',
                ctx_mock({'image': {}}))
    def test_check_image_no_file_no_url(self):
        # Test if it throws exception no file & no url
        self.assertRaises(NonRecoverableError,
                          image._validate_image)

    @mock.patch('glance_plugin.image.ctx',
                ctx_mock({'image_url': 'test-url', 'image': {'data': '.'}}))
    def test_check_image_and_url(self):
        # Test if it throws exception file & url
        self.assertRaises(NonRecoverableError,
                          image._validate_image)

    @mock.patch('glance_plugin.image.ctx',
                ctx_mock({'image_url': 'test-url', 'image': {}}))
    def test_check_image_url(self):
        # test if it passes no file & url
        http_connection_mock = mock.MagicMock()
        http_connection_mock.return_value.getresponse.return_value.status = 200
        with mock.patch('httplib.HTTPConnection', http_connection_mock):
            glance_plugin.image._validate_image()

    def test_check_image_file(self):
        # test if it passes file & no url
        image_file_path = tempfile.mkstemp()[1]
        with mock.patch('glance_plugin.image.ctx',
                        ctx_mock({'image': {'data': image_file_path}})):
            glance_plugin.image._validate_image()

    @mock.patch('glance_plugin.image.ctx',
                ctx_mock({'image': {'data': '/test/path'}}))
    # test when open file throws IO error
    def test_check_image_bad_file(self):
        open_name = '%s.open' % __name__
        with mock.patch(open_name, create=True) as mock_open:
            mock_open.side_effect = [mock_open(read_data='Data').return_value]
            self.assertRaises(NonRecoverableError,
                              glance_plugin.image._validate_image)

    @mock.patch('glance_plugin.image.ctx',
                ctx_mock({'image_url': '?', 'image': {}}))
    # test when bad url
    def test_check_image_bad_url(self):
        http_connection_mock = mock.MagicMock()
        http_connection_mock.return_value.getresponse.return_value.status = 400
        with mock.patch('httplib.HTTPConnection', http_connection_mock):
            self.assertRaises(NonRecoverableError,
                              glance_plugin.image._validate_image)


class TestValidateProperties(unittest.TestCase):

    @mock.patch('glance_plugin.image.ctx',
                ctx_mock({'image': {'container_format': 'bare'}}))
    def test_check_image_container_format_no_disk_format(self):
        # Test if it throws exception no file & no url
        self.assertRaises(NonRecoverableError,
                          image._validate_image_dictionary)

    @mock.patch('glance_plugin.image.ctx',
                ctx_mock({'image': {'disk_format': 'qcow2'}}))
    def test_check_image_no_container_format_disk_format(self):
        # Test if it throws exception no container_format & disk_format
        self.assertRaises(NonRecoverableError,
                          image._validate_image_dictionary)

    @mock.patch('glance_plugin.image.ctx',
                ctx_mock({'image': {}}))
    def test_check_image_no_container_format_no_disk_format(self):
        # Test if it throws exception no container_format & no disk_format
        self.assertRaises(NonRecoverableError,
                          image._validate_image_dictionary)

    @mock.patch('glance_plugin.image.ctx',
                ctx_mock(
                    {'image':
                        {'container_format': 'bare',
                         'disk_format': 'qcow2'}}))
    def test_check_image_container_format_disk_format(self):
        # Test if it do not throw exception container_format & disk_format
        image._validate_image_dictionary()


class TestStartImage(unittest.TestCase):
    blueprint_path = os.path.join('resources',
                                  'test-image-start.yaml')

    @mock.patch('glance_plugin.image.create')
    @workflow_test(blueprint_path, copy_plugin_yaml=True)
    def test_image_lifecycle_start(self, cfy_local, *_):
        test_vars = {
            'counter': 0,
            'image': mock.MagicMock()
        }

        def _mock_get_image_by_ctx(*_):
            i = test_vars['image']
            if test_vars['counter'] == 0:
                i.status = 'different image status'
            else:
                i.status = glance_plugin.image.IMAGE_STATUS_ACTIVE
            test_vars['counter'] += 1
            return i

        with mock.patch('openstack_plugin_common.GlanceClient'):
            with mock.patch('glance_plugin.image._get_image_by_ctx',
                            side_effect=_mock_get_image_by_ctx):
                cfy_local.execute('install', task_retries=3)

        self.assertEqual(2, test_vars['counter'])
        self.assertEqual(0, test_vars['image'].start.call_count)
