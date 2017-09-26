package org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by 10112215 on 2017/9/26.
 */
public class RpcServiceTopologyOperationOutputEntity {
    @JsonProperty("output")
    private ServiceTopologyOperationOutputEntity output;

    public ServiceTopologyOperationOutputEntity getOutput() {
        return output;
    }

    public void setOutput(ServiceTopologyOperationOutputEntity output) {
        this.output = output;
    }
}
