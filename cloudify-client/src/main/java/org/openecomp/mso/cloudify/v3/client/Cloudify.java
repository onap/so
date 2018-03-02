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

import org.openecomp.mso.cloudify.base.client.CloudifyClient;
import org.openecomp.mso.cloudify.base.client.CloudifyClientConnector;

/**
 * Reference: http://docs.getcloudify.org/api/v3/
 */
public class Cloudify extends CloudifyClient {

    private final DeploymentsResource deployments;
    private final BlueprintsResource blueprints;
    private final TokensResource tokens;
    private final NodeInstancesResource nodeInstances;
    private final ExecutionsResource executions;

/* Not supporting dynamic connectors
    public Cloudify(String endpoint, CloudifyClientConnector connector) {
        super(endpoint, connector);
        deployments = new DeploymentsResource(this);
        blueprints = new BlueprintsResource(this);
        nodeInstances = new NodeInstancesResource(this);
        tokens = new TokensResource(this);
    }
*/
    public Cloudify(String endpoint, String tenant) {
        super(endpoint, tenant);
        deployments = new DeploymentsResource(this);
        blueprints = new BlueprintsResource(this);
        nodeInstances = new NodeInstancesResource(this);
        executions = new ExecutionsResource(this);
        tokens = new TokensResource(this);
    }

    public Cloudify(String endpoint) {
    	super(endpoint);
        deployments = new DeploymentsResource(this);
        blueprints = new BlueprintsResource(this);
        nodeInstances = new NodeInstancesResource(this);
        executions = new ExecutionsResource(this);
        tokens = new TokensResource(this);
    }

    public DeploymentsResource deployments() {
        return this.deployments;
    }

    public BlueprintsResource blueprints() {
        return this.blueprints;
    }

    public NodeInstancesResource nodeInstances() {
        return this.nodeInstances;
    }

    public ExecutionsResource executions() {
        return this.executions;
    }

    public TokensResource tokens() {
        return this.tokens;
    }
}
