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
