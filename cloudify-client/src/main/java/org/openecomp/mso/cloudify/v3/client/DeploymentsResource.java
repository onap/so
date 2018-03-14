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

import org.openecomp.mso.cloudify.v3.model.CreateDeploymentParams;
import org.openecomp.mso.cloudify.v3.model.Deployment;
import org.openecomp.mso.cloudify.v3.model.DeploymentOutputs;
import org.openecomp.mso.cloudify.v3.model.Deployments;
import org.openecomp.mso.cloudify.base.client.Entity;
import org.openecomp.mso.cloudify.base.client.HttpMethod;
import org.openecomp.mso.cloudify.base.client.CloudifyClient;
import org.openecomp.mso.cloudify.base.client.CloudifyRequest;

public class DeploymentsResource {

    private final CloudifyClient client;

    public DeploymentsResource(CloudifyClient client) {
        this.client = client;
    }

    public CreateDeployment create(String deploymentId, CreateDeploymentParams body) {
        return new CreateDeployment(deploymentId, body);
    }

    public ListDeployments list() {
        return new ListDeployments();
    }

    public GetDeployment byId(String id) {
        return new GetDeployment(id);
    }

    public GetDeploymentOutputs outputsById(String id) {
        return new GetDeploymentOutputs(id);
    }

    public DeleteDeployment deleteByName(String name) {
        return new DeleteDeployment(name);
    }

    public class CreateDeployment extends CloudifyRequest<Deployment> {
        public CreateDeployment(String deploymentId, CreateDeploymentParams body) {
            super(client, HttpMethod.PUT, "/api/v3/deployments/" + deploymentId, Entity.json(body), Deployment.class);
        }
    }

    public class DeleteDeployment extends CloudifyRequest<Deployment> {
        public DeleteDeployment(String deploymentId) {
            super(client, HttpMethod.DELETE, "/api/v3/deployments/" + deploymentId, null, Deployment.class);
        }
    }

    public class GetDeployment extends CloudifyRequest<Deployment> {
        public GetDeployment(String id) {
            super(client, HttpMethod.GET, "/api/v3/deployments/" + id, null, Deployment.class);
        }
    }

    public class GetDeploymentOutputs extends CloudifyRequest<DeploymentOutputs> {
        public GetDeploymentOutputs(String id) {
            super(client, HttpMethod.GET, "/api/v3/deployments/" + id + "/outputs", null, DeploymentOutputs.class);
        }
    }

    public class ListDeployments extends CloudifyRequest<Deployments> {
        public ListDeployments() {
            super(client, HttpMethod.GET, "/api/v3/deployments", null, Deployments.class);
       }
    }

}
