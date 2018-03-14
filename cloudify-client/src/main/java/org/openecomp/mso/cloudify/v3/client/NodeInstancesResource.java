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
