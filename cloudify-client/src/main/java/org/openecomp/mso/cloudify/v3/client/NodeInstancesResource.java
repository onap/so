package org.openecomp.mso.cloudify.v3.client;

import org.openecomp.mso.cloudify.v3.model.UpdateNodeInstanceParams;
import org.openecomp.mso.cloudify.v3.model.NodeInstance;
import org.openecomp.mso.cloudify.v3.model.NodeInstances;
import org.openecomp.mso.cloudify.base.client.Entity;
import org.openecomp.mso.cloudify.base.client.HttpMethod;
import org.openecomp.mso.cloudify.base.client.CloudifyClient;
import org.openecomp.mso.cloudify.base.client.CloudifyRequest;

public class NodeInstancesResource {

    private final CloudifyClient client;

    public NodeInstancesResource(CloudifyClient client) {
        this.client = client;
    }

    public ListNodeInstances list() {
        return new ListNodeInstances();
    }

    public GetNodeInstance byId(String id) {
        return new GetNodeInstance(id);
    }

    public UpdateNodeInstance update(String id, UpdateNodeInstanceParams params) {
        return new UpdateNodeInstance(id, params);
    }


    public class GetNodeInstance extends CloudifyRequest<NodeInstance> {
        public GetNodeInstance (String id) {
            super(client, HttpMethod.GET, "/api/v3/node-instances/" + id, null, NodeInstance.class);
        }
    }

    public class ListNodeInstances extends CloudifyRequest<NodeInstances> {
        public ListNodeInstances() {
            super(client, HttpMethod.GET, "/api/v3/node-instances", null, NodeInstances.class);
       }
    }

    public class UpdateNodeInstance extends CloudifyRequest<NodeInstance> {
        public UpdateNodeInstance(String nodeInstanceId, UpdateNodeInstanceParams body) {
            super(client, HttpMethod.PATCH, "/api/v3/node-instances/" + nodeInstanceId, Entity.json(body), NodeInstance.class);
        }
    }

}
