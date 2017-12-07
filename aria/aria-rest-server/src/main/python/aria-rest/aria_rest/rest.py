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


import os
from flask import Flask, request, jsonify
from flask_autodoc.autodoc import Autodoc
from aria import install_aria_extensions
from aria.parser import consumption
from aria.utils import formatting, collections
from aria.cli.core import aria
from aria.cli import utils
from aria.exceptions import ParsingError, DependentServicesError
from aria.core import Core
from aria.cli import service_template_utils
from aria.storage import exceptions as storage_exceptions
from aria.utils import threading
from aria.orchestrator.workflow_runner import WorkflowRunner
from aria.orchestrator.workflows.executor.dry import DryExecutor
import util
import tempfile
import shutil

version_id = "0.1"
route_base = "/api/" + version_id + "/"
app = Flask("onap-aria-rest")
auto = Autodoc(app)

# TODO Garbage collect this dict somehow
execution_state = util.SafeDict()


def main():
    install_aria_extensions()
    app.run(host='0.0.0.0', port=5000, threaded=True)


@app.route("/")
@app.route("/api")
@app.route("/docs")
def index():
    return auto.html()


###
# TEMPLATES
###

# add template
@app.route(route_base + "templates/<template_name>", methods=['PUT'])
@auto.doc()
@aria.pass_model_storage
@aria.pass_resource_storage
@aria.pass_plugin_manager
@aria.pass_logger
def install_template(template_name, model_storage, resource_storage,
                     plugin_manager, logger):

    """
    installs a template in Aria storage

    3 modes possible:

      1. PUT JSON body which points to a CSAR URL.  Content-type must be
         application/json. PUT data is a JSON object/map with the following
         keys.:
          * service_template_path (required): URL to CSAR
          * service_template_filename (optional): service template file.

      2. PUT with service template file body.  Content-type must be
         text/plain.

      3. PUT with binary CSAR body.  Content-type must be application/zip.
         Optional query string arg "template_filename" can indicate the
         service template filename in the CSAR.  Defaults to
         "service-template.yaml".
    """

    service_template_path = None
    service_template_filename = "service-template.yaml"

    rtype = "unknown"
    if request.is_json:
        rtype = "json"
    elif request.headers['Content-Type'] == "application/zip":
        rtype = "zip"
        suffix = ".csar"
    elif request.headers['Content-Type'] == "text/plain":
        rtype = "yaml"
        suffix = ".yaml"

    if rtype == "zip" or rtype == "yaml":
        with tempfile.NamedTemporaryFile(prefix = "ariatmp_",
                                         suffix = suffix,
                                         delete = False) as f:
            f.write(request.data)
            service_template_path = f.name
        if request.headers['Content-Type'] == "application/zip":
            if "template_filename" in request.args:
                service_template_filename = request.args["template_filename"]

    elif rtype == "json":

        body = request.json

        # Check body
        if "service_template_path" in body:
            service_template_path = body["service_template_path"]
        else:
            return "request body missing service_template_path", 501

        if "service_template_filename" in body:
            service_template_filename = body["service_template_filename"]
        else:
            service_template_filename = "service-template.yaml"

    else:
        return "Unrecognized content type",400

    service_template_file_path = service_template_utils.get(
        service_template_path, service_template_filename)

    core = Core(model_storage, resource_storage, plugin_manager)

    try:
        core.create_service_template(service_template_file_path,
                                     os.path.dirname(service_template_path),
                                     template_name)
    except storage_exceptions.StorageError as e:
        logger.error("storage exception")
        utils.check_overriding_storage_exceptions(
            e, 'service template', template_name)
        return e.message, 500
    except Exception as e:
        logger.error("catchall exception")
        return e.message, 500
    finally:
        # cleanup
        if rtype == "zip" or rtype == "yaml":
            os.remove(service_template_path)
        if rtype == "zip":
            shutil.rmtree(os.path.dirname(service_template_file_path))

    return "service template installed", 200

# validate template
@app.route(route_base + "templates", methods=['POST'])
@auto.doc()
@aria.pass_model_storage
@aria.pass_resource_storage
@aria.pass_plugin_manager
@aria.pass_logger
def validate_template(model_storage, resource_storage, plugin_manager, logger):
    """
    Validates a TOSCA template
    """
    body = request.json

    # Check body
    if "service_template_path" in body:
        service_template_path = body["service_template_path"]
    else:
        return "request body missing service_template_path", 501
    if "service_template_filename" in body:
        service_template_filename = body["service_template_filename"]
    else:
        service_template_filename = "service-template.yaml"

    service_template_path = service_template_utils.get(
        service_template_path, service_template_filename)

    core = Core(model_storage, resource_storage, plugin_manager)
    try:
        context = core.validate_service_template(service_template_path)
    except ParsingError as e:
        return e.message, 400

    logger.info('Service template {} validated'.format(service_template_path))
    return "", 200


# delete template
@app.route(route_base + "templates/<template_id>", methods=['DELETE'])
@auto.doc()
@aria.pass_model_storage
@aria.pass_resource_storage
@aria.pass_plugin_manager
@aria.pass_logger
def delete_template(
        template_id,
        model_storage,
        resource_storage,
        plugin_manager,
        logger):
    """
    Deletes a template from Aria storage
    """

    logger.info('Deleting service template {}'.format(template_id))
    core = Core(model_storage, resource_storage, plugin_manager)
    try:
        core.delete_service_template(template_id)
    except DependentServicesError as e:
        logger.error("dependent services error")
        return e.message, 400
    except Exception as e:
        logger.error("failed")
        return "Failed to delete template", 500

    logger.info('Service template {} deleted'.format(template_id))
    return "", 200


# get template json
@app.route(route_base + "templates/<template_id>/json", methods=['GET'])
@auto.doc()
@aria.pass_model_storage
@aria.pass_logger
def get_template_json(template_id, model_storage, logger):
    """ get JSON representation of template """
    template = model_storage.service_template.get(template_id)
    consumption.ConsumptionContext()
    body = formatting.json_dumps(collections.prune(template.as_raw))
    return body


# list templates
@app.route(route_base + "templates", methods=['GET'])
@auto.doc()
@aria.pass_model_storage
@aria.pass_logger
def list_templates(model_storage, logger):
    """
    Lists templates installed in Aria storage
    """
    list = model_storage.service_template.list()
    templates = []
    for item in list:
        templates.append({"name": item.name,
                          "id": item.id,
                          "description": item.description
                          })
    return jsonify(templates)


# list nodes
@app.route(route_base + "templates/<template_id>/nodes", methods=['GET'])
@auto.doc()
@aria.pass_model_storage
@aria.pass_logger
def list_nodes_by_template(template_id, model_storage, logger):
    """
    Lists node templates in specified Aria template
    """
    service_template = model_storage.service_template.get(template_id)
    filters = dict(service_template=service_template)
    nodes = model_storage.node_template.list(filters=filters)
    nodelist = []

    for node in nodes:
        nodelist.append({
            "id": node.id,
            "name": node.name,
            "description": node.description,
            "service_template_id": service_template.id,
            "type_name": node.type_name
        })
    return jsonify(nodelist), 200


# show node details
@app.route(route_base + "nodes/<node_id>", methods=['GET'])
@auto.doc()
@aria.pass_model_storage
@aria.pass_logger
def get_node(node_id, model_storage, logger):
    """
    Get node details
    """
    node_template = model_storage.node_template.get(node_id)
    service_template = model_storage.service_template.get_by_name(
        node_template.service_template_name)
    retmap = {}
    retmap['id'] = node_id
    retmap['name'] = node_template.name
    retmap['description'] = node_template.description
    retmap['service_template_id'] = service_template.id
    retmap['type_name'] = node_template.type_name
    return jsonify(retmap), 200

###
# SERVICES
###


# list services
@app.route(route_base + "services", methods=['GET'])
@auto.doc()
@aria.pass_model_storage
@aria.pass_logger
def list_services(model_storage, logger):
    """
    Lists all services
    """
    services_list = model_storage.service.list()
    outlist = []
    for service in services_list:
        outlist.append({"id": service.id,
                        "description": service.description,
                        "name": service.name,
                        "service_template": service.service_template.name,
                        "created": service.created_at,
                        "updated": service.updated_at})
    return jsonify(outlist), 200


# show service
@app.route(route_base + "services/<service_id>", methods=['GET'])
def show_service(service_id):
    """
    Returns details for specified servie
    """
    return "not implemented", 501


# get service outputs
@app.route(route_base + "services/<service_id>/outputs", methods=['GET'])
@auto.doc()
@aria.pass_model_storage
@aria.pass_logger
def get_service_outputs(service_id, model_storage, logger):
    """
    Gets outputs for specified service
    """
    service = model_storage.service.get(service_id)
    outlist = []
    for output_name, output in service.outputs.iteritems():
        outlist.append({"name": output_name, "description": output.description,
                        "value": output.value})
    return jsonify(outlist)


# get service inputs
@app.route(route_base + "services/<service_id>/inputs", methods=['GET'])
@auto.doc()
@aria.pass_model_storage
@aria.pass_logger
def get_service_inputs(service_id, model_storage, logger):
    """
    Gets inputs for specified service
    """
    service = model_storage.service.get(service_id)
    outlist = []
    for input_name, input in service.inputs.iteritems():
        outlist.append({"name": input_name, "description": input.description,
                        "value": input.value})
    return jsonify(outlist)


# create service
@app.route(route_base + "templates/<template_id>/services/<service_name>",
           methods=['POST'])
@auto.doc()
@aria.pass_model_storage
@aria.pass_resource_storage
@aria.pass_plugin_manager
@aria.pass_logger
def create_service(template_id, service_name, model_storage, resource_storage,
                   plugin_manager, logger):
    """
    Creates a service from the specified service template
    """
    body = request.json
    inputs = {}
    if 'inputs' in body:
        inputs = body['inputs']
    core = Core(model_storage, resource_storage, plugin_manager)
    service = core.create_service(template_id, inputs, service_name)

    logger.info("service {} created".format(service.name))
    return "service {} created".format(service.name), 200


# delete service
@app.route(route_base + "services/<service_id>", methods=['DELETE'])
@auto.doc()
@aria.pass_model_storage
@aria.pass_resource_storage
@aria.pass_plugin_manager
@aria.pass_logger
def delete_service(
        service_id,
        model_storage,
        resource_storage,
        plugin_manager,
        logger):
    """
    Deletes the specified servi e
    """
    service = model_storage.service.get(service_id)
    core = Core(model_storage, resource_storage, plugin_manager)
    core.delete_service(service_id, force=True)
    return "service {}  deleted".format(service.id), 200


###
# WORKFLOWS
###


# list workflows
@app.route(route_base + "services/<service_id>/workflows", methods=['GET'])
@auto.doc()
@aria.pass_model_storage
@aria.pass_logger
def list_workflows(service_id, model_storage, logger):
    """
    Lists all defined user workflows for the specified service
    """
    service = model_storage.service.get(service_id)
    workflows = service.workflows.itervalues()
    outlist = []
    for workflow in workflows:
        outlist.append(workflow.name)
    return jsonify(outlist), 200


# show workflow
@app.route(
    route_base +
    "services/<service_id>/workflow/<workflow_name>",
    methods=['GET'])
def show_workflow(service_name, workflow_name):
    """
    Returns details of specified workflow
    """
    return "not implemented", 501

###
# EXECUTIONS
###


# list all executions
@app.route(route_base + "executions", methods=['GET'])
@auto.doc()
@aria.pass_model_storage
@aria.pass_logger
def list_executions(model_storage, logger):
    """
    Return all executions
    """
    elist = model_storage.execution.list()
    outlist = []
    for execution in elist:
        outlist.append(
            {"execution_id": execution.id,
             "workflow_name": execution.workflow_name,
             "service_template_name": execution.service_template_name,
             "service_name": execution.service_name,
             "status": execution.status})
    return jsonify(outlist), 200


# list executions for service
@app.route(route_base + "services/<service_id>/executions", methods=['GET'])
@auto.doc()
@aria.pass_model_storage
@aria.pass_logger
def list_service_executions(service_id, model_storage, logger):
    """
    Return all executions for specified service
    """
    service = model_storage.service.get(service_id)
    elist = model_storage.execution.list(filters=dict(service=service))
    outlist = []
    for execution in elist:
        outlist.append(
            {"execution_id": execution.id,
             "workflow_name": execution.workflow_name,
             "service_template_name": execution.service_template_name,
             "service_name": execution.service_name,
             "status": execution.status})
    return jsonify(outlist), 200


# show execution
@app.route(route_base + "executions/<execution_id>", methods=['GET'])
@auto.doc()
@aria.pass_model_storage
@aria.pass_logger
def show_execution(execution_id, model_storage, logger):
    """
    Return details of specified execution
    """
    try:
        execution = model_storage.execution.get(execution_id)
    except BaseException:
        return "Execution {} not found".format(execution_id), 404

    return jsonify({"execution_id": execution_id,
                    "service_name": execution.service_name,
                    "service_template_name": execution.service_template_name,
                    "workflow_name": execution.workflow_name,
                    "status": execution.status}), 200

# start execution


# TODO allow executors other than default and dry to be used
@app.route(
    route_base +
    "services/<service_id>/executions/<workflow_name>",
    methods=['POST'])
@auto.doc()
@aria.pass_model_storage
@aria.pass_resource_storage
@aria.pass_plugin_manager
@aria.pass_logger
def start_execution(
        service_id,
        workflow_name,
        model_storage,
        resource_storage,
        plugin_manager,
        logger):
    """
    Start an execution for the specified service
    """
    body = request.json
    executor = DryExecutor(
        ) if 'executor' in body and body['executor'] == 'dry' else None

    inputs = body['inputs'] if 'inputs' in body else None
    task_max_attempts = (body['task_max_attempts']
                         if 'task_max_attempts' in body else 30)
    task_retry_interval = (body['task_retry_interval']
                           if 'task_retry_interval' in body else 30)

    runner = WorkflowRunner(model_storage, resource_storage, plugin_manager,
                            service_id=service_id,
                            workflow_name=workflow_name,
                            inputs=inputs,
                            executor=executor,
                            task_max_attempts=task_max_attempts,
                            task_retry_interval=task_retry_interval)

    service = model_storage.service.get(service_id)
    tname = '{}_{}_{}'.format(service.name, workflow_name, runner.execution_id)
    thread = threading.ExceptionThread(target=runner.execute,
                                       name=tname)
    thread.start()
    execution_state[str(runner.execution_id)] = [runner, thread]
    return jsonify({"id": runner.execution_id}), 202


# resume execution
@app.route(route_base + "executions/<execution_id>", methods=['POST'])
@auto.doc()
@aria.pass_model_storage
@aria.pass_resource_storage
@aria.pass_plugin_manager
@aria.pass_logger
def resume_execution(
        execution_id,
        model_storage,
        resource_storage,
        plugin_manager,
        logger):
    """
    Resume the specified execution
    """
    body = request.json
    execution = model_storage.execution.get(execution_id)
    if execution.status != execution.status.CANCELLED:
        return "cancelled execution cannot be resumed", 400
    executor = DryExecutor(
        ) if 'executor' in body and body['executor'] == 'dry' else None
    retry_failed_tasks = body['retry_failed_tasks'] \
        if 'retry_failed_tasks' in body else False

    runner = WorkflowRunner(model_storage, resource_storage, plugin_manager,
                            execution_id=execution_id,
                            executor=executor,
                            retry_failed_tasks=retry_failed_tasks)

    tname = '{}_{}_{}'.format(execution.service.name, execution.workflow_name,
                              runner.execution_id)
    thread = threading.ExceptionThread(target=runner.execute,
                                       name=tname,
                                       daemon=True)
    thread.start()
    execution_state[str(runner.execution_id)] = [runner, thread]
    return jsonify({"id": runner.execution_id}), 202


# cancel execution
@app.route(route_base + "executions/<execution_id>", methods=['DELETE'])
@auto.doc()
@aria.pass_model_storage
@aria.pass_logger
def cancel_execution(execution_id, model_storage, logger):
    """
    Cancel the specified execution
    """
    logger.info("cancelling execution {}".format(execution_id))
    body = request.json

    try:
        execution = model_storage.execution.get(execution_id)
    except BaseException:
        return "Execution {} not found".format(execution_id), 404

    if (not execution.status == execution.PENDING and
            not execution.status == execution.STARTED):
        return "Cancel ignored.  Execution state = {}".format(
            execution.status), 200

    if execution_id not in execution_state:
        logger.error("id {} not found".format(execution_id))
        return "execution id {} not found".format(execution_id), 400

    einfo = execution_state[execution_id]
    runner = einfo[0]
    thread = einfo[1]
    timeout = 30  # seconds to wait for thread death
    if 'timeout' in body:
        timeout = body['timeout']

    runner.cancel()
    while thread.is_alive() and timeout > 0:
        thread.join(1)
        if not thread.is_alive():
            return "execution {} cancelled".format(execution_id), 200
        timeout = timeout - 1
    if timeout == 0:
        return "execution cancel timed out", 500
    return "execution {} cancelled".format(execution_id), 200


if __name__ == "__main__":
    app.run(host='0.0.0.0', port=5000, threaded=True)
