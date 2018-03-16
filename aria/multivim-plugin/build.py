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

import subprocess
import os
import sys
import glob
import xml.etree.ElementTree as etree

# create and enter venv
def create_venv( name):
    if subprocess.call("virtualenv {}".format(name), shell = True):
        raise Exception("virtualenv create failed")
    ret = subprocess.call(". {}/bin/activate && python {} run". \
                    format(name,__file__), shell = True)
    sys.exit(ret)

def init_venv():
    subprocess.call("pip install -U pip", shell = True)
    subprocess.call("pip install -U setuptools", shell = True)
    subprocess.call("pip install wheel", shell = True)
    subprocess.call("pip install twine", shell = True)


if len(sys.argv) == 1:
    create_venv ("mavenvenv")
else:
    init_venv()

    if os.environ['MVN_PHASE'] == 'package':
        wheelname = os.environ['WHEEL_NAME']
        inputdir = os.environ['INPUT_DIR']
        outputdir = os.environ['OUTPUT_DIR']
        savedir = os.getcwd()
        os.chdir(inputdir)

        if subprocess.call( [ "python",
                             "setup.py",
                             "bdist_wheel",
                             "-d",
                             outputdir
                             ]):
            sys.stderr("wheel create failed")
            sys.exit(1)
        f = glob.glob(outputdir+"/*.whl")[0]
        os.rename(f , outputdir+"/"+ wheelname)

    elif os.environ['MVN_PHASE'] == 'deploy':

        it = etree.iterparse(os.environ['SETTINGS_FILE'])
        for _, el in it:
            el.tag = el.tag.split('}', 1)[1]  # strip namespace
        settings = it.root

        username = settings.find('.//server[id="{}"]/username'.format(
                                os.environ['PYPI_SERVERID'])).text
        password = settings.find('.//server[id="{}"]/password'.format(
                                os.environ['PYPI_SERVERID'])).text

        try:
            if subprocess.call( [ "twine",
                             "upload",
                             "--username",
                             username,
                             "--password",
                             password,
                             "--repository-url",
                             os.environ["PYPI_SERVER_BASEURL"],
                             os.environ["WHEEL_PATH"]
                             ] ):
                sys.stderr.write("pypi upload failed")
                sys.exit(1)
        finally:
            subprocess.call("rm -rf mavenvenv", shell = True)

        sys.exit(0)
    else:
        sys.stderr.write("Unrecognized phase '{}'\n".format(
            os.environ('MVN_PHASE')))
        sys.exit(1)
