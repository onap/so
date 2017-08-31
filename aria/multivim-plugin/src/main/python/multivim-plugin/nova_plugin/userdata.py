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

import requests

from cloudify import compute
from cloudify import exceptions
from cloudify import ctx


def handle_userdata(server):

    existing_userdata = server.get('userdata')
    install_agent_userdata = ctx.agent.init_script()

    if not (existing_userdata or install_agent_userdata):
        return

    if isinstance(existing_userdata, dict):
        ud_type = existing_userdata['type']
        if ud_type not in userdata_handlers:
            raise exceptions.NonRecoverableError(
                "Invalid type '{0}' for server userdata)".format(ud_type))
        existing_userdata = userdata_handlers[ud_type](existing_userdata)

    if not existing_userdata:
        final_userdata = install_agent_userdata
    elif not install_agent_userdata:
        final_userdata = existing_userdata
    else:
        final_userdata = compute.create_multi_mimetype_userdata(
            [existing_userdata, install_agent_userdata])
    server['userdata'] = final_userdata


userdata_handlers = {
    'http': lambda params: requests.get(params['url']).text
}
