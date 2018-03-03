/*
 * ============LICENSE_START===================================================
 * Copyright (c) 2017 Cloudify.co.  All rights reserved.
 * ===================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * ============LICENSE_END====================================================
*/
package com.gigaspaces.aria.rest.client;

import com.gigaspaces.aria.rest.client.exceptions.StorageException;
import com.gigaspaces.aria.rest.client.exceptions.ValidationException;

import java.util.List;

/**
 * Created by DeWayne on 7/12/2017.
 */
public interface AriaClient {

    /**
     * Installs a service template
     *
     * @param template
     * @throws ValidationException
     * @throws StorageException
     */
    public void install_service_template(ServiceTemplate template)throws ValidationException, StorageException, Exception;

    /**
     * Validate a service template
     * @param template
     * @return
     */
    public ValidationResult validate_service_template(ServiceTemplate template)throws Exception;

    /**
     * Fetch a list of stored service templates
     *
     * @return
     */
    public List<? extends ServiceTemplate> list_service_templates();

    /**
     * Delete an existing template
     *
     * @param template_id
     * @throws IllegalArgumentException
     */
    public void delete_service_template(int template_id) throws IllegalArgumentException, Exception;

    /**
     * Returns a list of node templates for a given service template
     * @param template_id
     * @return
     */
    List<? extends NodeTemplate> list_nodes(int template_id);

    /**
     * Fetch a given node template
     *
     * @param node_id
     * @return
     * @throws IllegalArgumentException
     */
    public NodeTemplate get_node( int node_id) throws IllegalArgumentException;

    /**
     * List all services
     *
     * @return
     */
    public List<? extends Service> list_services();

    /**
     * Fetch the specified service
     *
     * @param service_id
     * @return
     * @throws IllegalArgumentException
     */
    public Service get_service(int service_id) throws IllegalArgumentException;

    /**
     * Fetch the outputs of the specified service
     *
     * @param service_id
     * @return
     * @throws IllegalArgumentException
     */
    public List<? extends Output> list_service_outputs(int service_id) throws IllegalArgumentException;

    /**
     * Fetch the inputs of the specified service
     *
     * @param service_id
     * @return
     * @throws IllegalArgumentException
     */
    public List<? extends Input> list_service_inputs(int service_id) throws IllegalArgumentException;

    /**
     * Create a service
     *
     * @param template_id
     * @param service_name
     * @param inputs
     * @throws Exception
     */
    public void create_service(int template_id, String service_name, List<Input> inputs)throws Exception;

    /**
     * Delete the specified service
     *
     * @param service_id
     * @throws IllegalArgumentException
     */
    public void delete_service(int service_id)throws Exception;

    /**
     * List workflows for the provided service
     *
     * @param service_id
     * @return
     * @throws IllegalArgumentException
     */
    public List<? extends Workflow> list_workflows(int service_id)throws IllegalArgumentException;

    /**
     * Fetch the specified workflow
     *
     * @param workflow_id
     * @return the requested Workflow
     * @throws IllegalArgumentException when the workflow_id doesn't exist
     */
    public Workflow get_workflow(int workflow_id)throws IllegalArgumentException;

    /**
     * List all executions
     *
     * @return
     * @throws Exception
     */
    public List<? extends Execution> list_executions()throws Exception;

    /**
     * List executions for provided service
     *
     * @param service_id
     * @return
     * @throws Exception
     */
    public List<? extends Execution> list_executions(int service_id)throws Exception;

    /**
     * Fetch the specified execution
     *
     * @param execution_id
     * @return
     * @throws IllegalArgumentException
     */
    public Execution get_execution(int execution_id)throws IllegalArgumentException;

    /**
     * Starts an execution
     *
     * @param service_id
     * @param workflow_name
     * @param details
     * @return the execution id
     * @throws Exception
     */
    public int start_execution(int service_id, String workflow_name, ExecutionDetails details)throws Exception;

    /**
     * Resumes an interrupted execution
     *
     * @param execution_id
     * @param details
     * @throws IllegalArgumentException
     */
    public void resume_execution(int execution_id, ExecutionDetails details)throws IllegalArgumentException;

    /**
     * Cancels the specified execution
     *
     * @param execution_id
     * @throws IllegalArgumentException
     */
    public void cancel_execution(int execution_id)throws Exception;
}
