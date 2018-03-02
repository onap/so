/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.mso.cloudify.v3.client;

import org.openecomp.mso.cloudify.v3.model.CancelExecutionParams;
import org.openecomp.mso.cloudify.v3.model.Execution;
import org.openecomp.mso.cloudify.v3.model.Executions;
import org.openecomp.mso.cloudify.v3.model.StartExecutionParams;
import org.openecomp.mso.cloudify.v3.model.UpdateExecutionParams;
import org.openecomp.mso.cloudify.base.client.Entity;
import org.openecomp.mso.cloudify.base.client.HttpMethod;
import org.openecomp.mso.cloudify.base.client.CloudifyClient;
import org.openecomp.mso.cloudify.base.client.CloudifyRequest;

public class ExecutionsResource {

    private final CloudifyClient client;

    public ExecutionsResource(CloudifyClient client) {
        this.client = client;
    }

    public ListExecutions list() {
        return new ListExecutions(null);
    }

    public ListExecutions listSorted (String sortBy) {
        return new ListExecutions("?_sort=" + sortBy);
    }
    
    // Return a filtered list.
    // The filter parameter should be a query string of filter criteria (without leading "?")
    public ListExecutions listFiltered (String filter, String sortBy) {
    	String listParams = "?" + filter;
    	if (sortBy != null)  listParams += "&_sort=" + sortBy;
        return new ListExecutions(listParams);
    }
    
    public GetExecution byId(String id) {
        return new GetExecution(id);
    }

    public StartExecution start(StartExecutionParams params) {
        return new StartExecution(params);
    }
    
    public UpdateExecution updateStatus(String id, String status) {
    	UpdateExecutionParams params = new UpdateExecutionParams();
    	params.setStatus(status);
        return new UpdateExecution(id, params);
    }

    public CancelExecution cancel(String executionId, CancelExecutionParams params) {
        return new CancelExecution(executionId, params);
    }
    

    public class GetExecution extends CloudifyRequest<Execution> {
        public GetExecution (String id) {
            super(client, HttpMethod.GET, "/api/v3/executions/" + id, null, Execution.class);
        }
    }

    public class ListExecutions extends CloudifyRequest<Executions> {
        public ListExecutions(String listParams) {
            super(client, HttpMethod.GET, "/api/v3/executions" + ((listParams!=null) ? listParams : ""), null, Executions.class);
       }
    }

    public class StartExecution extends CloudifyRequest<Execution> {
        public StartExecution(StartExecutionParams body) {
            super(client, HttpMethod.POST, "/api/v3/executions", Entity.json(body), Execution.class);
        }
    }

    public class UpdateExecution extends CloudifyRequest<Execution> {
        public UpdateExecution(String executionId, UpdateExecutionParams body) {
            super(client, HttpMethod.PATCH, "/api/v3/executions/" + executionId, Entity.json(body), Execution.class);
        }
    }

    public class CancelExecution extends CloudifyRequest<Execution> {
        public CancelExecution(String executionId, CancelExecutionParams body) {
            super(client, HttpMethod.POST, "/api/v3/executions/" + executionId, Entity.json(body), Execution.class);
        }
    }

}
