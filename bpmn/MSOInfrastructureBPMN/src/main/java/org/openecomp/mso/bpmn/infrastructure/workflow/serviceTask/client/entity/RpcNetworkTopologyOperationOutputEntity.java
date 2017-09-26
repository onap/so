package org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by 10112215 on 2017/9/20.
 */
public class RpcNetworkTopologyOperationOutputEntity {
    public NetworkTopologyOperationOutputEntity getOutput() {
        return output;
    }

    public void setOutput(NetworkTopologyOperationOutputEntity output) {
        this.output = output;
    }

    @JsonProperty("output")
    private NetworkTopologyOperationOutputEntity output;
}
