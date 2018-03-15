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

from setuptools import setup


setup(
    zip_safe=True,
    name='${PROJECT_NAME}',
    version='${VERSION}',
    author='idanmo',
    author_email='idan@gigaspaces.com',
    packages=[
        'openstack_plugin_common',
        'nova_plugin',
        'neutron_plugin',
        'cinder_plugin',
        'glance_plugin',
        'keystone_plugin'
    ],
    license='LICENSE',
    description='ONAP plugin for multivim infrastructure.',
    install_requires=[
        'cloudify-plugins-common>=3.3.1',
        'keystoneauth1>=2.16.0,<3',
        'python-novaclient==7.0.0',
        'python-keystoneclient==3.5.0',
        'python-neutronclient==6.0.0',
        'python-cinderclient==1.9.0',
        'python-glanceclient==2.5.0',
        'IPy==0.81'
    ]
)
