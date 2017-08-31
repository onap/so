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

import time

from cloudify import ctx
from cloudify.decorators import operation
from cloudify import exceptions as cfy_exc

from openstack_plugin_common import (delete_resource_and_runtime_properties,
                                     with_cinder_client,
                                     get_resource_id,
                                     transform_resource_name,
                                     use_external_resource,
                                     validate_resource,
                                     COMMON_RUNTIME_PROPERTIES_KEYS,
                                     OPENSTACK_AZ_PROPERTY,
                                     OPENSTACK_ID_PROPERTY,
                                     OPENSTACK_TYPE_PROPERTY,
                                     OPENSTACK_NAME_PROPERTY)
from glance_plugin.image import handle_image_from_relationship

VOLUME_STATUS_CREATING = 'creating'
VOLUME_STATUS_DELETING = 'deleting'
VOLUME_STATUS_AVAILABLE = 'available'
VOLUME_STATUS_IN_USE = 'in-use'
VOLUME_STATUS_ERROR = 'error'
VOLUME_STATUS_ERROR_DELETING = 'error_deleting'
VOLUME_ERROR_STATUSES = (VOLUME_STATUS_ERROR, VOLUME_STATUS_ERROR_DELETING)

# Note: The 'device_name' property should actually be a property of the
# relationship between a server and a volume; It'll move to that
# relationship type once relationship properties are better supported.
DEVICE_NAME_PROPERTY = 'device_name'

VOLUME_OPENSTACK_TYPE = 'volume'

RUNTIME_PROPERTIES_KEYS = COMMON_RUNTIME_PROPERTIES_KEYS


@operation
@with_cinder_client
def create(cinder_client, status_attempts, status_timeout, args, **kwargs):

    if use_external_resource(ctx, cinder_client, VOLUME_OPENSTACK_TYPE,
                             'name'):
        return

    name = get_resource_id(ctx, VOLUME_OPENSTACK_TYPE)
    volume_dict = {'name': name}
    volume_dict.update(ctx.node.properties['volume'], **args)
    handle_image_from_relationship(volume_dict, 'imageRef', ctx)
    volume_dict['name'] = transform_resource_name(
        ctx, volume_dict['name'])

    v = cinder_client.volumes.create(**volume_dict)

    ctx.instance.runtime_properties[OPENSTACK_ID_PROPERTY] = v.id
    ctx.instance.runtime_properties[OPENSTACK_TYPE_PROPERTY] = \
        VOLUME_OPENSTACK_TYPE
    ctx.instance.runtime_properties[OPENSTACK_NAME_PROPERTY] = \
        volume_dict['name']
    wait_until_status(cinder_client=cinder_client,
                      volume_id=v.id,
                      status=VOLUME_STATUS_AVAILABLE,
                      num_tries=status_attempts,
                      timeout=status_timeout,
                      )
    ctx.instance.runtime_properties[OPENSTACK_AZ_PROPERTY] = \
        v.availability_zone


@operation
@with_cinder_client
def delete(cinder_client, **kwargs):
    delete_resource_and_runtime_properties(ctx, cinder_client,
                                           RUNTIME_PROPERTIES_KEYS)


@with_cinder_client
def wait_until_status(cinder_client, volume_id, status, num_tries,
                      timeout):
    for _ in range(num_tries):
        volume = cinder_client.volumes.get(volume_id)

        if volume.status in VOLUME_ERROR_STATUSES:
            raise cfy_exc.NonRecoverableError(
                "Volume {0} is in error state".format(volume_id))

        if volume.status == status:
            return volume, True
        time.sleep(timeout)

    ctx.logger.warning("Volume {0} current state: '{1}', "
                       "expected state: '{2}'".format(volume_id,
                                                      volume.status,
                                                      status))
    return volume, False


@with_cinder_client
def get_attachment(cinder_client, volume_id, server_id):
    volume = cinder_client.volumes.get(volume_id)
    for attachment in volume.attachments:
        if attachment['server_id'] == server_id:
            return attachment


@operation
@with_cinder_client
def creation_validation(cinder_client, **kwargs):
    validate_resource(ctx, cinder_client, VOLUME_OPENSTACK_TYPE,
                      'name')
