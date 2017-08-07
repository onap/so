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

from flask import Flask, render_template
from aria.exceptions import AriaException

version_id = "0.1"
route_base = "/api/" + version_id + "/"
app = Flask("onap-aria-rest")

@app.route("/")
def index():
  return render_template('index.html')


@app.route(route_base + "templates/", methods = ['GET'])
def list_templates():

@app.route(route_base + "templates/<template_id>", methods = ['POST'])
def install_template( template_id ):

  # GET CSAR FROM SDC

  # DEPLOY CSAR

  # UPDATE A&AI?

  return "template {} instantiated"

@app.route(route_base + "templates/<template_id>", methods = ['DELETE'])
def delete_template( template_id ):

  # RUN UNINSTALL

  # DELETE TEMPLATE

  # UPDATE A&AI?

  return "template {} deleted"

if __name__ == "__main__":
  app.run()
