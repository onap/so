package org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by 10112215 on 2017/9/20.
 */
public class RpcNetworkTopologyOperationInputEntity {
    public NetworkTopologyOperationInputEntity getInput() {
        return input;
    }

    public void setInput(NetworkTopologyOperationInputEntity input) {
        this.input = input;
    }

    @JsonProperty("input")
    private NetworkTopologyOperationInputEntity input = null;
}
