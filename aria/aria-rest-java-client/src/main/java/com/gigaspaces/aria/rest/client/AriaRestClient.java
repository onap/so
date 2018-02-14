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
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.jackson.map.ObjectMapper;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static javax.ws.rs.client.Entity.entity;

/**
 * Created by DeWayne on 7/12/2017.
 */
public class AriaRestClient implements AriaClient {
    private Client client=null;
    private WebTarget base_target=null;

    /**
     * Construct an Aria REST client
     *
     * @param protocol either http or https
     * @param address the IP address or host name
     * @param port the port of the service
     * @param version the api version
     */
    public AriaRestClient(String protocol, String address, int port, String version){
        this.client = ClientBuilder.newBuilder().register(JacksonJsonProvider.class).build();
        base_target = client.target(protocol+"://"+address+":"+port+"/api/"+version);
    }

    /**
     * Installs a service template
     *
     * @param template the template object
     * @throws ValidationException
     * @throws StorageException
     */
    public void install_service_template(ServiceTemplate template) throws ValidationException, StorageException, Exception {

        Response response = base_target.path("templates/"+template.getName()).request(MediaType.APPLICATION_JSON).put(Entity.entity(
                "{\"service-template-path\":\""+template.getURI().toString()+"\""+
                        ",\"service-template-filename\":\""+template.getFilename()+"\"", MediaType.APPLICATION_JSON));

        if(response.getStatus() == 500){
            throw new StorageException(response.readEntity(String.class));
        }
        else if(response.getStatus() == 400){
            throw new ValidationException(response.readEntity(String.class));
        }
        else if(response.getStatus()>199 && response.getStatus() <300){
            return;
        }
        else{
            throw new Exception("Error installing template: "+response.getStatus()+" "+ response.readEntity(String.class));
        }
    }

    public ValidationResult validate_service_template(ServiceTemplate template)throws Exception{
        Response response = base_target.path("templates").request(MediaType.APPLICATION_JSON).post(Entity.entity(
                "{\"service-template-path\":\""+template.getURI().toString()+"\""+
                ",\"service-template-filename\":\""+template.getFilename()+"\"}", MediaType.APPLICATION_JSON));

        ValidationResultImpl result = new ValidationResultImpl();
        if(response.getStatus() >= 200 && response.getStatus() < 300){
            result.setFailed(false);
        }
        else if(response.getStatus()==400){
            result.setFailed(true);
        }
        else{
            throw new Exception("received error response '"+ response.getStatus()+"':"+response.readEntity(String.class));
        }
        return result;

    }

    /**
     *
     * @return a list of service templates
     */
    public List<? extends ServiceTemplate> list_service_templates(){
        List<? extends ServiceTemplate> templates = base_target.path("templates").request(MediaType.APPLICATION_JSON).get(new GenericType<List<ServiceTemplateImpl>>(){});

        return templates;
    }


    /**
     * Deletes the specified template.
     *
     * TODO: Error handling is a little blunt. Need to describe failures better
     *
     * @param template_id the template id to delete
     * @throws IllegalArgumentException thrown when the template can't be deleted
     * @throws Exception other server side errors
     */
    public void delete_service_template(int template_id) throws IllegalArgumentException, Exception{
        Response response = base_target.path("templates/"+template_id).request(MediaType.APPLICATION_JSON).delete();

        if(response.getStatus()>=200 && response.getStatus()<300){
            return;
        }
        else if(response.getStatus()==400){
            throw new IllegalArgumentException("Error deleting template '"+template_id+"'");
        }
        else{
            throw new Exception("Error processing request. Return code = "+response.getStatus());
        }
    }

    /**
     * List the node templates for a given template id
     *
     * @param template_id
     * @return
     */
    public List<? extends NodeTemplate> list_nodes(int template_id) {
        List<? extends NodeTemplate> nodes = base_target.path("templates/"+template_id+"/nodes").request(MediaType.APPLICATION_JSON).get(new GenericType<List<NodeTemplateImpl>>(){});
        return nodes;
    }

    /**
     * Get a specific node by id
     *
     * @param node_id the node id
     * @return
     * @throws IllegalArgumentException
     */
    public NodeTemplate get_node(int node_id) throws IllegalArgumentException {
        NodeTemplate node = base_target.path("nodes/"+node_id).request(MediaType.APPLICATION_JSON).get(NodeTemplateImpl.class);
        return node;
    }

    public List<? extends Service> list_services() {
        List<? extends Service> services = base_target.path("services").request(MediaType.APPLICATION_JSON).get(new GenericType<List<ServiceImpl>>(){});
        return services;
    }

    public Service get_service(int service_id) throws IllegalArgumentException {
        throw new NotImplementedException();
    }

    public List<? extends Output> list_service_outputs(int service_id) throws IllegalArgumentException {
        List<? extends Output> outputs = base_target.path("services").request(MediaType.APPLICATION_JSON).get(new GenericType<List<OutputImpl>>(){});
        return outputs;
    }

    public List<? extends Input> list_service_inputs(int service_id) throws IllegalArgumentException {
        List<? extends Input> inputs = base_target.path("services").request(MediaType.APPLICATION_JSON).get(new GenericType<List<InputImpl>>(){});
        return inputs;
    }

    /**
     * Create a service based on the supplied template
     *
     * @param template_id the template to create the service for
     * @param service_name a name for the service
     * @param inputs an optional list of inputs for the service (can be null)
     * @throws Exception
     */
    public void create_service(int template_id, String service_name, List<Input> inputs) throws Exception {

        String json="{"+inputsToJson(inputs)+"}";

        Response response = base_target.path("templates/"+template_id+"/services/"+service_name).
                request(MediaType.APPLICATION_JSON).post(
                Entity.entity(json, MediaType.APPLICATION_JSON)
        );

        if( response.getStatus()< 200 || response.getStatus()>299){
            throw new Exception("create service failed:"+response.getStatus()+" "+ response.readEntity(String.class));
        }
    }

    public void delete_service(int service_id) throws Exception {
        Response response = base_target.path("services/"+service_id).request(MediaType.APPLICATION_JSON).delete();
        if(!responseOK(response)){
            throw new Exception("delete service failed: "+response.getStatus()+" "+ response.readEntity(String.class));
        }
    }

    /**
     * List user workflows for supplied service
     *
     * @param service_id
     * @return
     * @throws IllegalArgumentException
     */
    public List<? extends Workflow> list_workflows(int service_id) throws IllegalArgumentException {
        List<? extends Workflow> workflows = base_target.path("services/"+service_id+"/workflows").request(MediaType.APPLICATION_JSON).get(new GenericType<List<WorkflowImpl>>(){});
        return workflows;
    }

    public Workflow get_workflow(int workflow_id) throws IllegalArgumentException {
        throw new NotImplementedException();
    }

    /**
     * List all executions
     *
     * @return
     * @throws Exception
     */
    public List<? extends Execution> list_executions() throws Exception {
        List<? extends Execution> executions = base_target.path("executions").request(MediaType.APPLICATION_JSON).get(new GenericType<List<ExecutionImpl>>(){});
        return executions;
    }

    /**
     * List executions for specified service
     *
     * @param service_id
     * @return
     * @throws Exception
     */
    public List<? extends Execution> list_executions(int service_id) throws Exception {
        List<? extends Execution> executions = base_target.path("services/"+service_id+"/executions").request(MediaType.APPLICATION_JSON).get(new GenericType<List<ExecutionImpl>>(){});
        return executions;
    }

    /**
     * Get details about a specified execution
     *
     * @param execution_id
     * @return
     * @throws IllegalArgumentException
     */
    public Execution get_execution(int execution_id) throws IllegalArgumentException {
        Execution execution = base_target.path("executions/"+execution_id).request(MediaType.APPLICATION_JSON).get(ExecutionImpl.class);
        return execution;
    }

    /**
     * Start an execution for the specified service
     *
     * @param service_id the service to run the execution for
     * @param workflow_name the name of the workflow to execute
     * @param details details controlling execution operation
     * @return the execution id
     * @throws Exception
     */
    public int start_execution(int service_id, String workflow_name, ExecutionDetails details) throws Exception {
        StringBuilder json=new StringBuilder("{");
        if(details.getExecutor().length()>0){
            json.append("\"executor\":\"").append(details.getExecutor()).append("\",");
        }
        if(details.getInputs()!=null){
            json.append(inputsToJson(details.getInputs()));
        }
        json.append("\"task_max_attempts\":").append(details.getTaskMaxAttempts()).append(",");
        json.append("\"task_retry_interval\":").append(details.getTaskRetryInterval()).append("}");

        System.out.println("JSON="+json.toString());

        Response response = base_target.path("services/"+service_id+"/executions/"+workflow_name).request(MediaType.APPLICATION_JSON).
                post(Entity.entity(json.toString(), MediaType.APPLICATION_JSON));

        if(!responseOK(response)){
            throw new Exception("start execution failed: "+response.getStatus()+" "+response.readEntity(String.class));
        }

        ObjectMapper mapper = new ObjectMapper(new JsonFactory());
        JsonNode rootNode = mapper.readTree(response.readEntity(String.class));
        int id=rootNode.get("id").asInt(-1);
        return id;
    }

    public void resume_execution(int execution_id, ExecutionDetails details) throws IllegalArgumentException {
        StringBuilder json=new StringBuilder("{");
        if(details.getExecutor().length()>0){
            json.append("\"executor\":\"").append(details.getExecutor()).append("\",");
        }
        json.append("\"retry_failed_tasks\":").append(details.isRetry_failed_tasks()).append("}");
        Response response = base_target.path("executions/"+execution_id).request(MediaType.APPLICATION_JSON).
                post(Entity.entity(json.toString(), MediaType.APPLICATION_JSON));
    }

    public void cancel_execution(int execution_id) throws Exception {
        Response response = base_target.path("executions/"+execution_id).request(MediaType.APPLICATION_JSON).delete();
        if(!responseOK(response)){
            throw new Exception("delete service failed: "+response.getStatus()+" "+ response.readEntity(String.class));
        }
    }

    /**
     * -----
     * ----- PRIVATE METHODS
     * -----
     */

    private boolean responseOK(Response response){
        return response.getStatus()>199 && response.getStatus()<300;
    }

    private String inputsToJson(List<Input> inputs){
        if(inputs==null)return null;

        StringBuilder sb=new StringBuilder("\"inputs\":{");
        for(Input input:inputs){
            sb.append("\"").append(input.getName()).append("\":\"").append(input.getValue()).append("\",");
        }
        if(inputs.size()>0)sb.deleteCharAt(sb.length()-1); //trim comma

        return sb.toString();
    }
}
