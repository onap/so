#
# ============LICENSE_START===================================================
# Copyright (c) 2017 Cloudify.co.  All rights reserved.
# ===================================================================
# Licensed under the Apache License, Version 2.0 (the "License"); you may not
# use this file except in compliance with the License. You may obtain a copy
# of the License at
#
#        http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations under
# the License.
# ============LICENSE_END====================================================
#


from setuptools import setup

setup(
    zip_safe=True,
    name='aria-rest',
    version='0.1',
    author='dewayne',
    author_email='dewayne@cloudify.co',
    packages=[
        'aria_rest'
    ],
    entry_points = {
      'console_scripts' : ['aria-rest=aria_rest.rest:main']
    },
    license='LICENSE',
    description='Aria REST API for ONAP',
    install_requires=[
        'distribute',
        'Flask==0.12.2',
        'flask-autodoc==0.1.2',
        'apache-ariatosca==0.1.1'
    ]
)
