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
