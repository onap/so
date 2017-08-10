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
import httplib
from urlparse import urlparse

from cloudify import ctx
from cloudify.decorators import operation
from cloudify.exceptions import NonRecoverableError

from openstack_plugin_common import (
    with_glance_client,
    get_resource_id,
    use_external_resource,
    get_openstack_ids_of_connected_nodes_by_openstack_type,
    delete_resource_and_runtime_properties,
    validate_resource,
    COMMON_RUNTIME_PROPERTIES_KEYS,
    OPENSTACK_ID_PROPERTY,
    OPENSTACK_TYPE_PROPERTY,
    OPENSTACK_NAME_PROPERTY)


IMAGE_OPENSTACK_TYPE = 'image'
IMAGE_STATUS_ACTIVE = 'active'

RUNTIME_PROPERTIES_KEYS = COMMON_RUNTIME_PROPERTIES_KEYS
REQUIRED_PROPERTIES = ['container_format', 'disk_format']


@operation
@with_glance_client
def create(glance_client, **kwargs):
    if use_external_resource(ctx, glance_client, IMAGE_OPENSTACK_TYPE):
        return

    img_dict = {
        'name': get_resource_id(ctx, IMAGE_OPENSTACK_TYPE)
    }
    _validate_image_dictionary()
    img_properties = ctx.node.properties['image']
    img_dict.update({key: value for key, value in img_properties.iteritems()
                    if key != 'data'})
    img = glance_client.images.create(**img_dict)
    img_path = img_properties.get('data', '')
    img_url = ctx.node.properties.get('image_url')
    try:
        _validate_image()
        if img_path:
            with open(img_path, 'rb') as image_file:
                glance_client.images.upload(
                    image_id=img.id,
                    image_data=image_file)
        elif img_url:
            img = glance_client.images.add_location(img.id, img_url, {})

    except:
        _remove_protected(glance_client)
        glance_client.images.delete(image_id=img.id)
        raise

    ctx.instance.runtime_properties[OPENSTACK_ID_PROPERTY] = img.id
    ctx.instance.runtime_properties[OPENSTACK_TYPE_PROPERTY] = \
        IMAGE_OPENSTACK_TYPE
    ctx.instance.runtime_properties[OPENSTACK_NAME_PROPERTY] = img.name


def _get_image_by_ctx(glance_client, ctx):
    return glance_client.images.get(
        image_id=ctx.instance.runtime_properties[OPENSTACK_ID_PROPERTY])


@operation
@with_glance_client
def start(glance_client, start_retry_interval, **kwargs):
    img = _get_image_by_ctx(glance_client, ctx)
    if img.status != IMAGE_STATUS_ACTIVE:
        return ctx.operation.retry(
            message='Waiting for image to get uploaded',
            retry_after=start_retry_interval)


@operation
@with_glance_client
def delete(glance_client, **kwargs):
    _remove_protected(glance_client)
    delete_resource_and_runtime_properties(ctx, glance_client,
                                           RUNTIME_PROPERTIES_KEYS)


@operation
@with_glance_client
def creation_validation(glance_client, **kwargs):
    validate_resource(ctx, glance_client, IMAGE_OPENSTACK_TYPE)
    _validate_image_dictionary()
    _validate_image()


def _validate_image_dictionary():
    img = ctx.node.properties['image']
    missing = ''
    try:
        for prop in REQUIRED_PROPERTIES:
            if prop not in img:
                missing += '{0} '.format(prop)
    except TypeError:
        missing = ' '.join(REQUIRED_PROPERTIES)
    if missing:
        raise NonRecoverableError('Required properties are missing: {'
                                  '0}. Please update your image '
                                  'dictionary.'.format(missing))


def _validate_image():
    img = ctx.node.properties['image']
    img_path = img.get('data')
    img_url = ctx.node.properties.get('image_url')
    if not img_url and not img_path:
        raise NonRecoverableError('Neither image url nor image path was '
                                  'provided')
    if img_url and img_path:
        raise NonRecoverableError('Multiple image sources provided')
    if img_url:
        _check_url(img_url)
    if img_path:
        _check_path()


def _check_url(url):
    p = urlparse(url)
    conn = httplib.HTTPConnection(p.netloc)
    conn.request('HEAD', p.path)
    resp = conn.getresponse()
    if resp.status >= 400:
        raise NonRecoverableError('Invalid image URL')


def _check_path():
    img = ctx.node.properties['image']
    img_path = img.get('data')
    try:
        with open(img_path, 'rb'):
            pass
    except TypeError:
        if not img.get('url'):
            raise NonRecoverableError('No path or url provided')
    except IOError:
        raise NonRecoverableError(
            'Unable to open image file with path: "{}"'.format(img_path))


def _remove_protected(glance_client):
    if use_external_resource(ctx, glance_client, IMAGE_OPENSTACK_TYPE):
        return

    is_protected = ctx.node.properties['image'].get('protected', False)
    if is_protected:
        img_id = ctx.instance.runtime_properties[OPENSTACK_ID_PROPERTY]
        glance_client.images.update(img_id, protected=False)


def handle_image_from_relationship(obj_dict, property_name_to_put, ctx):
    images = get_openstack_ids_of_connected_nodes_by_openstack_type(
        ctx, IMAGE_OPENSTACK_TYPE)
    if images:
        obj_dict.update({property_name_to_put: images[0]})
